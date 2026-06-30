package ru.vladimir.itemmanager.storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import com.google.common.collect.Multimap;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import ru.vladimir.itemmanager.ItemManager;
import ru.vladimir.itemmanager.utils.Logger;

public final class CustomItemStorage {

    private static final MiniMessage MINI_MESSAGE_PARSER = MiniMessage.miniMessage();
    private static final String FILE_STORAGE_NAME = "items.yml";
    private final ItemManager plugin;
    private final Map<String, byte[]> itemRegistry;

    public CustomItemStorage(@NotNull ItemManager plugin) {
        this.plugin = plugin;
        this.itemRegistry = new ConcurrentHashMap<>();

        refreshItemRegistry(getItemConfigFile(), getItemConfig());
    }

    private void refreshItemRegistry(File file, FileConfiguration itemConfig) {
        itemRegistry.clear();
        
        final Set<String> itemIds = itemConfig.getKeys(false);
        if (itemIds.isEmpty()) return;

        for (final String itemId : itemIds) {
            
            final ConfigurationSection section = itemConfig.getConfigurationSection(itemId);
            if (section == null) {
                Logger.warn(this, "Item ID '%s' is not a configuration section.".formatted(itemId));
                continue;
            }

            final byte[] parsedItemData = deserializeItemEntryIntoBytes(itemId, section);
            if (parsedItemData == null) {
                Logger.warn(this, "Item ID '%s' failed to be parsed.".formatted(itemId));
                continue;
            }

            itemRegistry.put(itemId, parsedItemData);
        }
    }

    private boolean appendItemToStorage(File file, FileConfiguration itemConfig, String itemId, ItemStack item) {
        refreshItemRegistry(file, itemConfig);

        if (itemConfig.contains(itemId)) return false;

        final ConfigurationSection sectionToCopyFrom = itemConfig.createSection(itemId);
        serializeItemIntoSection(sectionToCopyFrom, item);

        final Set<String> sectionToCopyFromKeys = sectionToCopyFrom.getKeys(true);

        if (sectionToCopyFromKeys.isEmpty()) {
            Logger.warn(this, "Failed to serialize '%s' into section.".formatted(itemId));
            return false;
        }

        final ConfigurationSection sectionToCopyTo = itemConfig.createSection(itemId);

        for (final String key : sectionToCopyFromKeys) {
            final var value = sectionToCopyFrom.get(key);
            sectionToCopyTo.set(key, value);
        }

        saveItemConfig(file, itemConfig);

        refreshItemRegistry(file, itemConfig);

        return true;
    }

    private boolean removeItemFromStorage(File file, FileConfiguration itemConfig, String itemId) {
        refreshItemRegistry(file, itemConfig);

        if (!itemConfig.contains(itemId)) return false;
        
        itemConfig.set(itemId, null);

        saveItemConfig(file, itemConfig);

        refreshItemRegistry(file, itemConfig);

        return true;
    }

    private File getItemConfigFile() {
        return new File(plugin.getDataFolder(), FILE_STORAGE_NAME);
    }

    private FileConfiguration getItemConfig() {
        return YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), FILE_STORAGE_NAME));
    }

    private void saveItemConfig(File file, FileConfiguration config) {
        if (!file.exists()) {
            plugin.saveResource(FILE_STORAGE_NAME, false);
            Logger.info(this, "'%s' was not found. A new version has been created.".formatted(FILE_STORAGE_NAME));
        }

        try {
            config.save(file);
        } catch (IOException e) {
            Logger.error(this, "Failed to save file configuration to '%s'.".formatted(FILE_STORAGE_NAME), e);
        }
    }

    public boolean registerCustomItem(@NotNull String itemId, @NotNull ItemStack item) {
        if (isCustomItem(itemId)) return false;
        
        return appendItemToStorage(getItemConfigFile(), getItemConfig(), itemId, item);
    }

    public boolean unregisterCustomItem(@NotNull String itemId) {
        if (!isCustomItem(itemId)) return false;

        return removeItemFromStorage(getItemConfigFile(), getItemConfig(), itemId);
    }

    public boolean isCustomItem(@NotNull String itemId) {
        return itemRegistry.containsKey(itemId);
    }

    @NotNull Optional<ItemStack> getCustomItem(@NotNull String itemId) {
        if (!isCustomItem(itemId)) return Optional.empty();

        return Optional.ofNullable(ItemStack.deserializeBytes(itemRegistry.get(itemId)));
    }

    public @NotNull @Unmodifiable Set<String> getCustomItemIds() {
        return Set.copyOf(itemRegistry.keySet());
    }

    private byte[] deserializeItemEntryIntoBytes(String itemId, ConfigurationSection itemEntry) {
        final String materialName = itemEntry.getString("material");
        if (materialName == null) {
            Logger.warn(this, "Failed to parse item entry '%s': Material name not found.".formatted(itemId));
            return null;
        }

        final Material material;

        try {
            material = Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            Logger.warn(this, "Failed to parse item's entry '%s': Invalid material name '%s'.".formatted(itemId, materialName));
            return null;
        }

        final String rawDisplayName = itemEntry.getString("name");
        if (rawDisplayName == null) {
            Logger.warn(this, "Failed to parse item's entry '%s': Item name not found.".formatted(itemId));
            return null;
        }

        final Component displayName = MINI_MESSAGE_PARSER.deserialize(rawDisplayName);

        final List<String> rawLore = itemEntry.getStringList("lore");
        final List<Component> lore = new ArrayList<>();

        for (final String rawLine : rawLore) {
            lore.add(MINI_MESSAGE_PARSER.deserialize(rawLine));
        }

        final List<?> rawEnchantments = itemEntry.getMapList("enchantments");
        if (rawEnchantments == null) {
            Logger.warn(this, "Failed to parse item's entry '%s': Enchantments not found.".formatted(itemId));
            return null;
        }

        final List<EnchantmentEntry> enchantments = new ArrayList<>();
        for (final Object entry : rawEnchantments) {

            if (!(entry instanceof final Map<?, ?> properties)) {
                Logger.warn(this, "Failed to parse enchantment's entry of item's entry '%s': '%s' is not entry.".formatted(itemId, entry));
                continue;
            }

            if (properties.size() != 2) {
                Logger.warn(this, "Failed to parse enchantment's entry of item's entry '%s': Must contain key and level, but has '%s' instead.".formatted(itemId, properties));
                continue;
            }

            final String enchantmentKey = String.valueOf(properties.get("key")).strip().toLowerCase();
            final int enchantmentLevel;

            try {
                enchantmentLevel = (int) properties.get("level");
            } catch (ClassCastException e) {
                Logger.warn(this, "Failed to parse enchantment's entry '%s' of item's entry '%s': Invalid level of '%s'.".formatted(enchantmentKey, itemId, properties.get("level")));
                continue;
            }

            enchantments.add(new EnchantmentEntry(enchantmentKey, enchantmentLevel));
        }

        final List<?> rawAttributes = itemEntry.getMapList("attributes");
        if (rawAttributes == null) {
            Logger.warn(this, "Failed to parse item's entry '%s': Attributes not found.".formatted(itemId));
            return null;
        }

        final List<AttributeEntry> attributes = new ArrayList<>();
        for (final Object entry : rawAttributes) {

            if (!(entry instanceof final Map<?, ?> properties)) {
                Logger.warn(this, "Failed to parse attribute entry of item's entry '%s': '%s' is not entry.".formatted(itemId, entry));
                continue;
            }

            if (properties.size() != 3) {
                Logger.warn(this, "Failed to parse antribute entry of item's entry '%s': It must contain attribute, modifier, level, but it has '%s' instead.".formatted(properties));
                continue;
            }

            final String attributeKey = String.valueOf(properties.get("key")).strip().toLowerCase();
            // Operation operation, double modifier
        }

        // Effects, attributes, keys

        final ItemStack item = ItemStack.of(Material.DIRT);
        final ItemMeta itemMeta = item.getItemMeta();

        itemMeta.displayName(displayName);
        itemMeta.lore(lore);

        for (final EnchantmentEntry entry : enchantments) {

            final Enchantment enchantment = RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.ENCHANTMENT)
                .get(new NamespacedKey("minecraft", entry.key().strip().toLowerCase()));

            if (enchantment == null) {
                Logger.warn(this, "Failed to parse an enchantment '%s' of item's entry '%s': Invalid enchantment.".formatted(entry.key().strip().toLowerCase(), itemId));
                continue;
            }

            item.addUnsafeEnchantment(enchantment, entry.level());
        }

        for (final AttributeEntry entry : attributes) {

            final String attributeKey = entry.key().strip().toLowerCase();
            final Attribute attribute = RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.ATTRIBUTE)
                .get(new NamespacedKey("minecraft", attributeKey));

            for (final AttributeModifierEntry modifierEntry : entry.modifiers()) {
                final String modifierOperationName = modifierEntry.operation().strip().toUpperCase();
                final Operation modifierOperation;

                try {
                    modifierOperation = Operation.valueOf(modifierOperationName);
                } catch (IllegalArgumentException e) {
                    Logger.warn(this, "Failed to parse attribute modifier of attribute's entry '%s' of item's entry '%s': Invalid operation name '%s'.".formatted(attributeKey, itemId, modifierOperationName));
                    continue;
                }

                final String slotGroupName = modifierEntry.slot() != null ? modifierEntry.slot().strip().toUpperCase() : null;
                EquipmentSlotGroup slotGroup = null;

                if (slotGroupName != null) {
                    try {
                        slotGroup = EquipmentSlotGroup.getByName(slotGroupName);
                    } catch (IllegalArgumentException e) {
                        Logger.warn(this, "Failed to parse attribute modifier of attribute's entry '%s' of item's entry '%s': Invalid equipment slot name '%s'.".formatted(attributeKey, itemId, slotGroupName));
                        continue;
                    }
                }

                final AttributeModifier modifier;

                if (slotGroup == null) {
                    modifier = new AttributeModifier(new NamespacedKey(plugin, UUID.randomUUID().toString()), modifierEntry.amount(), modifierOperation);
                } else {
                    modifier = new AttributeModifier(new NamespacedKey(plugin, UUID.randomUUID().toString()), modifierEntry.amount(), modifierOperation, slotGroup);
                }

                itemMeta.addAttributeModifier(attribute, modifier);
            }
        }

        item.setItemMeta(itemMeta);

        return item.serializeAsBytes();
    }

    private void serializeItemIntoSection(ConfigurationSection section, ItemStack item) {
        final Component displayName = item.displayName();
        final String rawDisplayName = MINI_MESSAGE_PARSER.serialize(displayName);

        final List<Component> lore = item.lore();
        final List<String> rawLore = new ArrayList<>();

        if (lore != null && !lore.isEmpty()) {
            for (final Component line : lore) {
                rawLore.add(MINI_MESSAGE_PARSER.serialize(line));
            }
        }

        final Map<Enchantment, Integer> enchantments = item.getEnchantments();
        final List<EnchantmentEntry> rawEnchantments = new ArrayList<>();

        if (enchantments != null && !enchantments.isEmpty()) {
            for (final var entry : enchantments.entrySet()) {

                // Enchantment -> NamespacedKey -> "namespace:key"
                rawEnchantments.add(new EnchantmentEntry(entry.getKey().getKey().getKey(), entry.getValue()));
            }
        }

        final ItemMeta itemMeta = item.getItemMeta();

        final Multimap<Attribute, AttributeModifier> attributes = itemMeta.getAttributeModifiers();
        final List<AttributeEntry> rawAttributes = new ArrayList<>();

        for (final var entry : attributes.entries()) {
            
        }

        // Effects, attributes, keys

        section.set("name", rawDisplayName);
        section.set("lore", rawLore);
        section.set("enchantments", rawEnchantments);
    }

    private record EnchantmentEntry(String key, int level) {}
    private record AttributeEntry(String key, List<AttributeModifierEntry> modifiers) {}
    private record AttributeModifierEntry(String slot, String operation, double amount) {}
}
