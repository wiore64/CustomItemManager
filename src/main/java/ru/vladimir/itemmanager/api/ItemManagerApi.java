package ru.vladimir.itemmanager.api;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import ru.vladimir.itemmanager.ItemManager;
import ru.vladimir.itemmanager.storage.CustomItem;

public final class ItemManagerApi {
    private final ItemManager plugin;

    public ItemManagerApi(@NotNull ItemManager plugin) {
        this.plugin = plugin;
    }
    
    public void reloadConfig() {
        plugin.onReload();
    }

    public boolean registerCustomItem(@NotNull String itemId, @NotNull ItemStack itemStack) {}
    public boolean registerCustomItem(@NotNull String itemId, @NotNull Byte itemData) {}
    public boolean unregisterCustomItem(@NotNull String itemId) {}
    public boolean isCustomItem(@NotNull String itemId) {}
    public @NotNull Optional<CustomItem> getCustomItem(@NotNull String itemId) {}
    public @NotNull Optional<ItemStack> getCustomItemAsItemStack(@NotNull String itemId) {}
    public @NotNull Optional<Byte> getCustomItemAsByte(@NotNull String itemId) {}
    public @NotNull @Unmodifiable Set<String> getAllCustomItemIds() {}
    public @NotNull @Unmodifiable Map<String, Byte> getAllCustomItemsAsBytes() {}
}
