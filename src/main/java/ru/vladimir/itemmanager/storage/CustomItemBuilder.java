package ru.vladimir.itemmanager.storage;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class CustomItemBuilder {

    private final CustomItemStorage itemStorage;
    private final Map<String, ItemStack> itemCache;

    public CustomItemBuilder(@NotNull CustomItemStorage itemStorage) {
        this.itemStorage = itemStorage;
        this.itemCache = new ConcurrentHashMap<>();
    }
    
    public @NotNull Optional<ItemStack> build(@NotNull String id) {
        if (!itemStorage.isCustomItem(id))
            return Optional.empty();

        if (itemCache.containsKey(id))
            return Optional.of(itemCache.get(id));

        // Get item from the storage.
        return Optional.of(ItemStack.of(Material.DIRT));
    }
}
