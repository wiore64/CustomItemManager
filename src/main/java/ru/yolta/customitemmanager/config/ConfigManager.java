package ru.yolta.customitemmanager.config;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import ru.yolta.customitemmanager.CustomItemManager;
import ru.yolta.customitemmanager.utils.Logger;

public final class ConfigManager {

    private static final String MESSAGE_CONFIG_FILE_NAME = "messages.yml";
    private final CustomItemManager plugin;
    private final MessageConfig messageConfig;

    public ConfigManager(@NotNull CustomItemManager plugin) {
        Logger.debug(this, "Initializing...");

        this.plugin = plugin;

        final File messageConfigFile = new File(plugin.getDataFolder(), MESSAGE_CONFIG_FILE_NAME);
        ensureFileExists(messageConfigFile, false);
        this.messageConfig = MessageConfig.parseMessageConfig(this, messageConfigFile, getFileConfig(messageConfigFile));

        GuideFiles.saveGuides(this);

        Logger.debug(this, "Initialized successfully.");
    }

    void saveConfig(@NotNull File file, @NotNull FileConfiguration fileConfig) {
        try {
            fileConfig.save(file);
        } catch (IOException e) {
            Logger.error(this, "Failed to save config.", e);
        }
    }

    void saveFile(@NotNull String path) {
        plugin.saveResource(path, true);
    }

    private void ensureFileExists(File file, boolean shouldReplace) {
        if (!file.exists()) {
            Logger.warn(this, "File '{}' not found. Creating it now.", file.getName());
            saveFile(file.getName());
        }
    }

    private FileConfiguration getFileConfig(File file) {
        return YamlConfiguration.loadConfiguration(file);
    }

    public @NotNull MessageConfig getMessageConfig() {
        return messageConfig;
    }
}
