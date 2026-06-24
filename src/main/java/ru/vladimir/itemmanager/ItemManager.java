package ru.vladimir.itemmanager;

import org.bukkit.plugin.java.JavaPlugin;

import ru.vladimir.itemmanager.api.ItemManagerApi;

public final class ItemManager extends JavaPlugin {
    private static ItemManagerApi api;
    
    @Override
    public void onEnable() {
        api = new ItemManagerApi(this);
    }

    public void onReload() {

    }

    @Override
    public void onDisable() {
        
    }

    public static ItemManagerApi getApi() {
        if (api == null) 
            throw new IllegalStateException("Attempted to get API before it was available.");
        return api;
    }
}