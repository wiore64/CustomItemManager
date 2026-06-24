package ru.vladimir.itemmanager.storage;

import java.util.List;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public record CustomItem(
    @NotNull String id,
    @NotNull Material material, // Material or String?
    @NotNull String name,
    @NotNull List<String> lore
) {}
