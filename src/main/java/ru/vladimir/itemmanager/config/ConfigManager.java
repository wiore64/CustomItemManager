package ru.vladimir.itemmanager.config;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import ru.vladimir.itemmanager.ItemManager;
import ru.vladimir.itemmanager.utils.Logger;

public final class ConfigManager {

    private static ConfigManager instance;
    private Config config;
    private Messages messages;

    private ConfigManager() {}

    public static @NotNull ConfigManager getInstance() {
        if (instance == null)
            throw new IllegalStateException("Attempted to get instance before it was initialized.");
        return instance;
    }

    public static void init(@NotNull ItemManager plugin) {
        if (instance != null) {
            Logger.warn(instance, "Attempted to initialize an instance while it is already initialized.");
            return;
        }

        instance = new ConfigManager();

        final File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            Logger.info(instance, "Config file is not found. Creating a new one...");
            plugin.saveResource("config.yml", false);
        }

        final File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            Logger.info(instance, "Messages file is not found. Creating a new one...");
            plugin.saveResource("messages.yml", false);
        }

        instance.config = instance.readConfig(YamlConfiguration.loadConfiguration(configFile));
        instance.messages = instance.readMessages(YamlConfiguration.loadConfiguration(messagesFile));

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

    private Config readConfig(FileConfiguration config) {
        final String levelName = config.getString("logging-level", "INFO");
        Level level = Level.INFO;

        try {
            level = Level.parse(levelName);
        } catch (IllegalArgumentException e) {
            Logger.warn(this, "Invalid logging level in config: %s. Defaulting to INFO.".formatted(levelName));
        }

        return new Config(level);
    }

    private Messages readMessages(FileConfiguration config) {
        return new Messages(
            config.getString("no-permission", "You do not have permission to do this."),
            config.getString("plugin-description", "A powerful item management plugin."),
            config.getString("invalid-command", "Unknown command. Use /itemmanager help."),
            config.getString("player-only-command", "This command can only be used by players."),
            config.getString("invalid-arguments", "Invalid arguments. Usage: {USAGE}"),
            config.getString("must-hold-item", "You must be holding an item."),
            config.getString("item-registered", "Item {ITEM} registered."),
            config.getString("item-already-registered", "Item {ITEM} is already registered."),
            config.getString("player-not-found", "Player {PLAYER} was not found."),
            config.getString("item-not-found", "Item {ITEM} was not found."),
            config.getString("item-given", "Gave {AMOUNT}x {ITEM} to {PLAYER}."),
            config.getString("invalid-amount", "Invalid amount {AMOUNT}. Must be a positive integer."),
            config.getString("item-list", "Registered items: {ITEMS}"),
            config.getString("plugin-reloaded", "Plugin reloaded."),
            config.getString("item-unregistered", "Item {ITEM} unregistered."),
            config.getString("plugin-help", "To do... (if you see it, I forgot to write it... lol)")
        );
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
