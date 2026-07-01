package ru.vladimir.itemmanager;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import ru.vladimir.itemmanager.api.ItemManagerApi;
import ru.vladimir.itemmanager.command.CommandService;
import ru.vladimir.itemmanager.command.ItemManagerCommand;
import ru.vladimir.itemmanager.config.ConfigManager;
import ru.vladimir.itemmanager.storage.CustomItemBuilder;
import ru.vladimir.itemmanager.storage.CustomItemStorage;
import ru.vladimir.itemmanager.utils.Logger;

public final class ItemManager extends JavaPlugin {
    private static ItemManagerApi api;
    
    @Override
    public void onEnable() {
        Logger.info(this, "Loading up...");

        final ConfigManager configManager = new ConfigManager(this);

        Logger.setLevel(configManager.getGeneralConfig().loggingLevel());

        final CustomItemStorage itemStorage = new CustomItemStorage(this);
        final CustomItemBuilder itemBuilder = new CustomItemBuilder(itemStorage);

        api = new ItemManagerApi(this, itemStorage, itemBuilder);

        final CommandService commandService = new CommandService(configManager.getMessageConfig());
        final ItemManagerCommand commandHandler = new ItemManagerCommand(commandService, configManager.getMessageConfig());

        final PluginCommand command = this.getCommand("itemmanager");
        if (command == null) throw new IllegalStateException("Command 'itemmanager' not found in plugin.yml");

        command.setExecutor(commandHandler);
        command.setTabCompleter(commandHandler);

        Logger.info(this, "Loaded successfully.");
    }

    public void onReload() {
        onDisable();
        onEnable();
    }

    @Override
    public void onDisable() {
        api = null;
    }

    public static @NotNull ItemManagerApi getApi() {
        if (api == null) 
            throw new IllegalStateException("Attempted to get API before it was initialized.");
        return api;
    }
}
