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

public final class ItemManager extends JavaPlugin {
    
    private static ItemManager instance;
    private static ItemManagerApi api;
    
    @Override
    public void onEnable() {
        instance = this;

        ConfigManager.init(this);

        final CustomItemStorage itemStorage = new CustomItemStorage(this);
        final CustomItemBuilder itemBuilder = new CustomItemBuilder(itemStorage);

        api = new ItemManagerApi(this, itemStorage, itemBuilder);

        CommandService.init();

        final PluginCommand command = this.getCommand("itemmanager");
        if (command == null) throw new IllegalStateException("Command 'itemmanager' not found in plugin.yml");
        
        final ItemManagerCommand commandHandler = new ItemManagerCommand();
        command.setExecutor(commandHandler);
        command.setTabCompleter(commandHandler);
    }

    public void onReload() {
        onDisable();
        onEnable();
    }

    @Override
    public void onDisable() {
        CommandService.destroy();
        api = null;
        ConfigManager.destroy();
        instance = null;
    }

    public static @NotNull ItemManager getInstance() {
        if (instance == null) 
            throw new IllegalStateException("Attempted to get instance before it was initialized.");
        return instance;
    }

    public static @NotNull ItemManagerApi getApi() {
        if (api == null) 
            throw new IllegalStateException("Attempted to get API before it was available.");
        return api;
    }
}
