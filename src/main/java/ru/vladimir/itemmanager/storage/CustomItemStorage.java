package ru.vladimir.itemmanager.storage;

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import ru.vladimir.itemmanager.ItemManager;

public final class CustomItemStorage {
    private final Map<String, byte[]> itemRegistry;

    public CustomItemStorage(@NotNull ItemManager plugin) {
        itemRegistry = readUserStorage(YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "items.yml")));
    }

    private Map<String, byte[]> readUserStorage(FileConfiguration config) {
        // Do something.
        return Map.of();
    }

    private void appendItemToUserStorage(String id, ItemStack item) {

    }

    private void removeItemFromUserStorage(String id) {

    }

    private void writePluginStorage() {

    }

    public boolean registerCustomItem(@NotNull String id, @NotNull ItemStack item) {
        if (isCustomItem(id)) return false;
        appendItemToUserStorage(id, item);
        itemRegistry.put(id, item.serializeAsBytes());
        writePluginStorage();
        return true;
    }

    public boolean unregisterCustomItem(@NotNull String id) {
        if (!isCustomItem(id)) return false;
        removeItemFromUserStorage(id);
        itemRegistry.remove(id);
        writePluginStorage();
        return true;
    }

    public boolean isCustomItem(@NotNull String id) {
        return itemRegistry.containsKey(id);
    }

    public Set<String> getCustomItemIds() {
        return Set.copyOf(itemRegistry.keySet());
    }
}
