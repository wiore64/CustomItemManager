package ru.vladimir.itemmanager.config;

import org.jetbrains.annotations.NotNull;

import ru.vladimir.itemmanager.utils.Logger;

public final class ConfigManager {

    private static ConfigManager instance;
    private Config config;
    private Messages messages;

    private ConfigManager() {}

    public static void init() {
        if (instance != null) {
            Logger.warn(instance, "Attempted to initialize an instance while it is already initialized.");
            return;
        }

        instance = new ConfigManager();

        instance.config = instance.readConfig();
        instance.messages = instance.readMessages();

        Logger.debug(instance, "Initialized successfully.");
    }

    public static void destroy() {
        if (instance == null) {
            Logger.warn(ConfigManager.class, "Attempted to destroy an instance while there is none.");
            return;
        }

        instance = null;

        Logger.debug(ConfigManager.class, "Destroyed successfully.");
    }

    private Config readConfig() {
        // Here we read config.
        return null;
    }

    private Messages readMessages() {
        // Here we read messages.
        return null;
    }

    public static @NotNull ConfigManager getInstance() {
        if (instance == null)
            throw new IllegalStateException("Attempted to get instance before it was initialized.");
        return instance;
    }

    public @NotNull Config getConfig() {
        if (instance.config == null)
            throw new IllegalStateException("Attempted to get config before it was initialized.");
        return instance.config;
    }

    public @NotNull Messages getMessages() {
        if (instance.messages == null)
            throw new IllegalStateException("Attempted to get messages before they were initialized.");
        return instance.messages;
    }
}