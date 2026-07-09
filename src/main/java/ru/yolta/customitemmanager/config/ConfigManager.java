package ru.yolta.customitemmanager.config;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import ru.yolta.customitemmanager.CustomItemManager;
import ru.yolta.customitemmanager.utils.Logger;

public final class ConfigManager {

    private static final String GENERAL_CONFIG_FILE_NAME = "config.yml";
    private static final String MESSAGE_CONFIG_FILE_NAME = "messages.yml";
    private final GeneralConfig generalConfig;
    private final MessageConfig messageConfig;

    public ConfigManager(@NotNull CustomItemManager plugin) {
        Logger.debug(this, "Initializing...");

        final File generalConfigFile = new File(plugin.getDataFolder(), GENERAL_CONFIG_FILE_NAME);
        ensureFileExists(plugin, generalConfigFile);
        this.generalConfig = GeneralConfig.parseGeneralConfig(this, generalConfigFile, getFileConfig(generalConfigFile));

        final File messageConfigFile = new File(plugin.getDataFolder(), MESSAGE_CONFIG_FILE_NAME);
        ensureFileExists(plugin, messageConfigFile);
        this.messageConfig = MessageConfig.parseMessageConfig(this, messageConfigFile, getFileConfig(messageConfigFile));

        Logger.debug(this, "Initialized successfully.");
    }

    void saveConfig(@NotNull File file, @NotNull FileConfiguration fileConfig) {
        try {
            fileConfig.save(file);
        } catch (IOException e) {
            Logger.error(MessageConfig.class, "Failed to save config.");
        }
    }

    private FileConfiguration getFileConfig(File file) {
        return YamlConfiguration.loadConfiguration(file);
    }

    private void ensureFileExists(CustomItemManager plugin, File file) {
        if (!file.exists()) {
            plugin.saveResource(file.getName(), false);
        }
    }

    public @NotNull GeneralConfig getGeneralConfig() {
        return generalConfig;
    }

    public @NotNull MessageConfig getMessageConfig() {
        return messageConfig;
    }
}
