package ru.vladimir.itemmanager.storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
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

        if (!new File(plugin.getDataFolder(), "items.yml").exists()) {
            Logger.info(this, "items.yml not found. Creating a new one...");
            plugin.saveResource("items.yml", false);
        }

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
            final Object value = sectionToCopyFrom.get(key);
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

        return Optional.of(ItemStack.deserializeBytes(itemRegistry.get(itemId)));
    }

    public @NotNull @Unmodifiable Set<String> getCustomItemIds() {
        return Set.copyOf(itemRegistry.keySet());
    }

    private byte[] deserializeItemEntryIntoBytes(String itemId, ConfigurationSection itemEntry) {
        final String materialName = itemEntry.getString("material");
        if (materialName == null) {
            Logger.warn(this, "Failed to parse '%s': No material name.".formatted(itemId));
            return null;
        }

        final Material material;

        try {
            material = Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            Logger.warn(this, "Failed to parse '%s': Bad material name '%s'.".formatted(itemId, materialName));
            return null;
        }

        final String rawDisplayName = itemEntry.getString("name");
        if (rawDisplayName == null) {
            Logger.warn(this, "Failed to parse '%s': No item name.".formatted(itemId));
            return null;
        }

        final Component displayName = MINI_MESSAGE_PARSER.deserialize(rawDisplayName);

        final List<?> rawLore = itemEntry.getList("lore");
        if (rawLore == null) {
            Logger.warn(this, "Failed to parse '%s': No lore.".formatted(itemId));
            return null;
        }

        final List<Component> lore = new ArrayList<>();

        for (final Object rawSupposedLine : rawLore) {

            if (!(rawSupposedLine instanceof final String rawLine)) {
                Logger.warn(this, "Failed to parse line of lore of '%s': Not a string '%s'.".formatted(itemId, rawSupposedLine));
                continue;
            }

            lore.add(MINI_MESSAGE_PARSER.deserialize(rawLine));
        }

        final List<?> rawEnchantments = itemEntry.getList("enchantments");
        if (rawEnchantments == null) {
            Logger.warn(this, "Failed to parse '%s': Enchantments not found.".formatted(itemId));
            return null;
        }

        final Set<EnchantmentEntry> enchantments = new HashSet<>();
        final Set<String> addedEnchantments = new HashSet<>();
    
        for (final Object rawEnchantment : rawEnchantments) {
            
            if (!(rawEnchantment instanceof final Map<?, ?> entry)) {
                Logger.warn(this, "Failed to parse enchantment of '%s': '%s' is not entry.".formatted(itemId, rawEnchantment));
                continue;
            }

            if (entry.size() != 2) {
                Logger.warn(this, "Failed to parse enchantment of '%s': '%s' is not valid entry.".formatted(itemId, entry));
                continue;
            }

            final String key = String.valueOf(entry.get("name"));

            if (addedEnchantments.contains(key)) {
                Logger.warn(this, "Failed to parse enchantment '%s' of '%s': A duplicate.".formatted(key, itemId));
                continue;
            }

            final Object supposedLevel = entry.get("level");
            final short level;

            try {
                level = Short.parseShort(String.valueOf(supposedLevel));
            } catch (NumberFormatException e) {
                Logger.warn(this, "Failed to parse %s having class %s".formatted(supposedLevel, supposedLevel.getClass().getSimpleName()));

//                Logger.warn(this, "Failed to parse enchantment '%s' of '%s': Invalid level '%s'.".formatted(key, itemId, supposedLevel));
                continue;
            }

            if (level < 0 || level > 255) {
                Logger.warn(this, "Enchantment '%s' of '%s' has a level beyond the cap (%s). It may cause inconsistent results.".formatted(key, itemId, supposedLevel));
            }

            enchantments.add(new EnchantmentEntry(key, level));
            addedEnchantments.add(key);
        }

        final List<?> rawAttributes = itemEntry.getList("attributes");
        if (rawAttributes == null) {
            Logger.warn(this, "Failed to parse '%s': Attributes not found.".formatted(itemId));
            return null;
        }

        final Set<AttributeEntry> attributes = new HashSet<>();
        final Set<String> addedAttributes = new HashSet<>();

        for (final Object rawAttribute : rawAttributes) {

            if (!(rawAttribute instanceof final Map<?, ?> entry)) {
                Logger.warn(this, "Failed to parse attribute of '%s': '%s' is not entry.".formatted(itemId, rawAttribute));
                continue;
            }

            if (entry.size() != 2) {
                Logger.warn(this, "Failed to parse attribute of '%s': '%s' is not valid entry.".formatted(itemId, entry));
                continue;
            }

            final String key = String.valueOf(entry.get("name"));
            
            if (addedAttributes.contains(key)) {
                Logger.warn(this, "Failed to parse attribute '%s' of '%s': A duplicate.".formatted(key, itemId));
                continue;
            }

            final Object supposedRawModifiers = entry.get("modifiers");

            if (!(supposedRawModifiers instanceof final List<?> rawModifiers)) {
                Logger.warn(this, "Failed to parse attribute '%s' of '%s': '%s' is invalid modifiers.".formatted(key, itemId, supposedRawModifiers));
                continue;
            }

            final List<AttributeModifierEntry> modifiers = new ArrayList<>();

            for (final Object rawModifier : rawModifiers) {

                if (!(rawModifier instanceof final Map<?, ?> modifierEntry)) {
                    Logger.warn(this, "Failed to parse attribute modifier of '%s' of '%s': '%s' is not entry.".formatted(key, itemId, rawModifier));
                    continue;
                }

                if (modifierEntry.size() < 2 || modifierEntry.size() > 3) {
                    Logger.warn(this, "Failed to parse attribute modifier of '%s' of '%s': '%s' is not valid entry.".formatted(key, itemId, modifierEntry));
                    continue;
                }

                final Object supposedAmount = modifierEntry.get("amount");
                final double amount;

                try {
                    amount = (double) supposedAmount;
                } catch (IllegalArgumentException e) {
                    Logger.warn(this, "Failed to parse attribute modifier of '%s' of '%s': '%s' is not valid amount.".formatted(key, itemId, supposedAmount));
                    continue;
                }

                final Object supposedSlotGroupName = modifierEntry.get("slot");
                final String slotGroupName = supposedSlotGroupName == null ? null : String.valueOf(supposedSlotGroupName);

                modifiers.add(new AttributeModifierEntry(
                    slotGroupName, 
                    String.valueOf(modifierEntry.get("operation")), 
                    amount
                ));
            }

            attributes.add(new AttributeEntry(key, modifiers));
            addedAttributes.add(key);
        }

        final List<?> rawKeys = itemEntry.getList("keys");
        if (rawKeys == null) {
            Logger.warn(this, "Failed to parse '%s': Keys not found.".formatted(itemId));
            return null;
        }

        final Set<NamespacedKey> keys = new HashSet<>();
        final Set<String> addedKeys = new HashSet<>();

        for (final Object supposedRawKey : rawKeys) {

            if (!(supposedRawKey instanceof final String rawKey)) {
                Logger.warn(this, "Failed to parse key of '%s': '%s' is not key.".formatted(itemId, supposedRawKey));
                continue;
            }

            if (addedKeys.contains(rawKey)) {
                Logger.warn(this, "Failed to parse key '%s' of '%s': A duplicate.".formatted(rawKey, itemId));
                continue;
            }

            final String[] splitKey = rawKey.split(":");

            if (splitKey.length > 2) {
                Logger.warn(this, "Failed to parse key '%s' of '%s': Invalid format.".formatted(rawKey, itemId));
                continue;
            }

            if (splitKey.length == 1) {
                keys.add(new NamespacedKey(plugin, splitKey[0]));
            } else {
                keys.add(new NamespacedKey(splitKey[0], splitKey[1]));
            }

            addedKeys.add(rawKey);
        }

        final ItemStack item = ItemStack.of(material);
        final ItemMeta itemMeta = item.getItemMeta();

        itemMeta.displayName(displayName);
        itemMeta.lore(lore);

        for (final EnchantmentEntry entry : enchantments) {

            final Enchantment enchantment = RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.ENCHANTMENT)
                .get(new NamespacedKey("minecraft", entry.key()));

            if (enchantment == null) {
                Logger.warn(this, "Failed to parse enchantment '%s' of '%s': Invalid enchantment.".formatted(entry.key(), itemId));
                continue;
            }

            itemMeta.addEnchant(enchantment, entry.level(), true);
        }

        for (final AttributeEntry entry : attributes) {

            final String key = entry.key();
            final Attribute attribute = RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.ATTRIBUTE)
                .get(new NamespacedKey("minecraft", key));

            if (attribute == null) {
                Logger.warn(this, "Failed to parse attribute '%s' of '%s': Invalid attribute.".formatted(key, itemId));
                continue;
            }

            for (final AttributeModifierEntry modifierEntry : entry.modifiers()) {

                final String modifierOperationName = modifierEntry.operationName();
                final Operation modifierOperation;

                try {
                    modifierOperation = Operation.valueOf(modifierOperationName);
                } catch (IllegalArgumentException e) {
                    Logger.warn(this, "Failed to parse modifier of attribute '%s' of '%s': '%s' is not valid operation.".formatted(key, itemId, modifierOperationName));
                    continue;
                }

                final String slotGroupName = modifierEntry.slotGroupName();
                final EquipmentSlotGroup slotGroup = slotGroupName == null ? null : EquipmentSlotGroup.getByName(slotGroupName);

                if (slotGroup == null) {
                    itemMeta.addAttributeModifier(attribute, new AttributeModifier(new NamespacedKey(plugin, UUID.randomUUID().toString()), modifierEntry.amount(), modifierOperation));
                } else {
                    itemMeta.addAttributeModifier(attribute, new AttributeModifier(new NamespacedKey(plugin, UUID.randomUUID().toString()), modifierEntry.amount(), modifierOperation, slotGroup));
                }
            }
        }

        final PersistentDataContainer container = itemMeta.getPersistentDataContainer();

        for (final NamespacedKey key : keys) {
            container.set(key, PersistentDataType.BOOLEAN, true);
        }

        item.setItemMeta(itemMeta);

        Logger.info(this, "Raw enchantments?: %s".formatted(enchantments));
        Logger.info(this, "Enchantments: %s and %s".formatted(itemMeta.getEnchants(), item.getEnchantments()));

        return item.serializeAsBytes();
    }

    private void serializeItemIntoSection(ConfigurationSection section, ItemStack item) {
        final Material material = item.getType();
        final String materialName = material.name();

        final Component displayName = item.displayName();

        // Should do it differently. Right now it converts it as [name]
        final String rawDisplayName = PlainTextComponentSerializer.plainText().serialize(displayName);

        final List<Component> lore = item.lore();
        final List<String> rawLore = new ArrayList<>();

        if (lore != null && !lore.isEmpty()) {
            for (final Component line : lore) {

                // Likewise. Change.
                rawLore.add(PlainTextComponentSerializer.plainText().serialize(line));
            }
        }

        section.set("material", materialName);
        section.set("name", rawDisplayName);
        section.set("lore", rawLore);

        final ItemMeta itemMeta = item.getItemMeta();

        if (itemMeta == null) {
            section.set("enchantments", List.of());
            section.set("attributes", List.of());
            section.set("keys", List.of());
            return;
        }

        final Map<Enchantment, Integer> enchantments = itemMeta.getEnchants();
        final List<EnchantmentEntry> rawEnchantments = new ArrayList<>();

        if (!enchantments.isEmpty()) {
            for (final var entry : enchantments.entrySet()) {

                // Enchantment -> NamespacedKey -> "namespace:key"
                rawEnchantments.add(new EnchantmentEntry(
                        entry.getKey().getKey().getKey(),
                        entry.getValue())
                );
            }
        }

        final Multimap<Attribute, AttributeModifier> attributes = itemMeta.getAttributeModifiers();
        final List<AttributeEntry> rawAttributes = new ArrayList<>();

        if (attributes != null) {
            for (final var entry : attributes.entries()) {
                // Do stuff.
            }
        }

        final PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        final Set<String> rawKeys = new HashSet<>();

        for (final NamespacedKey key : container.getKeys()) {
            // add keys
        }

        section.set("enchantments", rawEnchantments);
        section.set("attributes", rawAttributes);
        section.set("keys", rawKeys);
    }

    private record EnchantmentEntry(String key, int level) {
        private EnchantmentEntry(String key, int level) {
            this.key = key.strip().toLowerCase();
            this.level = level;
        }
    }

    private record AttributeEntry(String key, List<AttributeModifierEntry> modifiers) {
        private AttributeEntry(String key, List<AttributeModifierEntry> modifiers) {
            this.key = key.strip().toLowerCase();
            this.modifiers = modifiers;
        }
    }
    private record AttributeModifierEntry(String slotGroupName, String operationName, double amount) {
        private AttributeModifierEntry(String slotGroupName, String operationName, double amount) {
            this.slotGroupName = slotGroupName == null ? null : slotGroupName.strip().toUpperCase();
            this.operationName = operationName.strip().toUpperCase();
            this.amount = amount;
        }
    }
}
