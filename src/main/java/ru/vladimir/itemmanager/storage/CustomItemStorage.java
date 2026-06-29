package ru.vladimir.itemmanager.storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

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

            final byte[] parsedItemData = deserializeSectionIntoBytes(section);
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

    private byte[] deserializeSectionIntoBytes(ConfigurationSection section) {
        final String displayNameRaw = section.getString("name");
        final Component displayName = MINI_MESSAGE_PARSER.deserialize(displayNameRaw);

        final List<String> loreRaw = section.getStringList("lore");
        final List<Component> lore = new ArrayList<>();

        if (loreRaw.size() > 0) {
            for (final String lineRaw : loreRaw) {
                lore.add(MINI_MESSAGE_PARSER.deserialize(lineRaw));
            }
        }

        // Enchantments, effects, attributes, keys

        final ItemStack item = ItemStack.of(Material.DIRT);
        final ItemMeta itemMeta = item.getItemMeta();

        itemMeta.displayName(displayName);
        itemMeta.lore(lore);

        item.setItemMeta(itemMeta);

        return item.serializeAsBytes();
    }

    private void serializeItemIntoSection(ConfigurationSection section, ItemStack item) {
        final Component displayName = item.displayName();
        final String displayNameRaw = MINI_MESSAGE_PARSER.serialize(displayName);

        final List<Component> lore = item.lore();
        final List<String> loreRaw = new ArrayList<>();

        if (lore.size() > 0) {
            for (final Component line : lore) {
                loreRaw.add(MINI_MESSAGE_PARSER.serialize(line));
            }
        }

        // Enchantments, effects, attributes, keys

        section.set("name", displayNameRaw);
        section.set("lore", loreRaw);
    }
}
