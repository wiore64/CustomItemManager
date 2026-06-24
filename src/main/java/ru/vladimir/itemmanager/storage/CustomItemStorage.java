package ru.vladimir.itemmanager.storage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.NotNull;

import ru.vladimir.itemmanager.utils.Logger;

public final class CustomItemStorage {
    private static CustomItemStorage instance;
    private Map<String, Byte> customItemRegistry;

    public static void init() {
        if (instance != null) {
            Logger.warn(instance, "Attempted to initialize an instance while it is already initialized.");
            return;
        }

        instance = new CustomItemStorage();
        instance.customItemRegistry = new ConcurrentHashMap<>();

        Logger.debug(instance, "Initialized successfully.");
    }

    public static void destroy() {
        if (instance == null) {
            Logger.warn(CustomItemStorage.class, "Attempted to destroy an instance while there is none.");
            return;
        }

        instance = null;

        Logger.debug(CustomItemStorage.class, "Destroyed successfully.");
    }

    public static @NotNull CustomItemStorage getInstance() {
        if (instance == null)
            throw new IllegalStateException("Attempted to get instance before it was initialized.");
        return instance;
    }

    // todo
    public static void registerCustomItem(@NotNull String id, @NotNull Byte data) {
        instance.customItemRegistry.put(id, data);
    }

    public static boolean isCustomItem(@NotNull String id) {
        return instance.customItemRegistry.containsKey(id);
    }
}
