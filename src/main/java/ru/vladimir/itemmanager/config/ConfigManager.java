package ru.vladimir.itemmanager.config;

import java.io.File;
import java.util.Locale;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import ru.vladimir.itemmanager.ItemManager;
import ru.vladimir.itemmanager.utils.Logger;

public final class ConfigManager {

    private static final String GENERAL_CONFIG_FILE_NAME = "config.yml";
    private static final String MESSAGE_CONFIG_FILE_NAME = "messages.yml";
    private final GeneralConfig generalConfig;
    private final MessageConfig messageConfig;

    public ConfigManager(@NotNull ItemManager plugin) {
        Logger.debug(this, "Initializing...");

        this.generalConfig = parseGeneralConfig(getGeneralFileConfig(plugin));
        this.messageConfig = parseMessageConfig(getMessageFileConfig(plugin));

        Logger.debug(this, "Initialized successfully.");
    }

    private FileConfiguration getGeneralFileConfig(ItemManager plugin) {
        final File configFile = new File(plugin.getDataFolder(), GENERAL_CONFIG_FILE_NAME);

        if (!configFile.exists()) {
            Logger.info(this, "'%s' does not exist. A default one will be created.".formatted(GENERAL_CONFIG_FILE_NAME));
            plugin.saveResource(GENERAL_CONFIG_FILE_NAME, false);
        }

        return YamlConfiguration.loadConfiguration(configFile);
    }

    private FileConfiguration getMessageFileConfig(ItemManager plugin) {
        final File configFile = new File(plugin.getDataFolder(), MESSAGE_CONFIG_FILE_NAME);

        if (!configFile.exists()) {
            Logger.info(this, "'%s' does not exist. A default one will be created.".formatted(MESSAGE_CONFIG_FILE_NAME));
            plugin.saveResource(MESSAGE_CONFIG_FILE_NAME, false);
        }

        return YamlConfiguration.loadConfiguration(configFile);
    }

    private GeneralConfig parseGeneralConfig(FileConfiguration config) {
        String levelName = config.getString("logging-level");

        if (levelName == null) {
            Logger.warn(this, "Failed to parse logging level in '%s': Level not found.".formatted(GENERAL_CONFIG_FILE_NAME));
            return new GeneralConfig(Level.INFO);
        }

        levelName = levelName.strip().toUpperCase(Locale.ROOT);

        try {
            return new GeneralConfig(levelName.equals("DEBUG") ? Level.FINE : Level.parse(levelName));
        } catch (IllegalArgumentException e) {
            Logger.warn(this, "Failed to parse logging level: Invalid level '%s'.".formatted(levelName));
            return new GeneralConfig(Level.INFO);
        }
    }

    private MessageConfig parseMessageConfig(FileConfiguration config) {
        return new MessageConfig(
                getMessage(config, "no-permission"),
                getMessage(config, "plugin-description"),
                getMessage(config, "invalid-command"),
                getMessage(config, "player-only-command"),
                getMessage(config, "invalid-arguments"),
                getMessage(config, "must-hold-item"),
                getMessage(config, "item-registered"),
                getMessage(config, "item-already-registered"),
                getMessage(config, "player-not-found"),
                getMessage(config, "item-not-found"),
                getMessage(config, "item-given"),
                getMessage(config, "invalid-amount"),
                getMessage(config, "item-list"),
                getMessage(config, "plugin-reloaded"),
                getMessage(config, "item-unregistered"),
                getMessage(config, "plugin-help")
        );
    }

    private String getMessage(FileConfiguration config, String key) {
        final String value = config.getString(key);

        if (value == null) {
            Logger.warn(this, "Failed to parse '%s' in '%s': Message not found. Using default.".formatted(key, MESSAGE_CONFIG_FILE_NAME));
            return MessageConfig.DEFAULT_MESSAGES.get(key);
        }

        return value;
    }

    public @NotNull GeneralConfig getGeneralConfig() {
        return generalConfig;
    }

    public @NotNull MessageConfig getMessageConfig() {
        return messageConfig;
    }
}
