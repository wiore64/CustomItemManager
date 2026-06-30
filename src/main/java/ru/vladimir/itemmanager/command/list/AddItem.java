package ru.vladimir.itemmanager.command.list;

import java.util.List;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ru.vladimir.itemmanager.ItemManager;
import ru.vladimir.itemmanager.command.SubCommand;
import ru.vladimir.itemmanager.config.ConfigManager;
import ru.vladimir.itemmanager.utils.Messager;

public class AddItem implements SubCommand {

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (!(sender instanceof final Player player)) {
            Messager.sendMessage(sender, ConfigManager.getInstance().getMessages().playerOnlyCommand());
            return;
        }
        
        if (args.length != 2) {
            Messager.sendMessage(sender, ConfigManager.getInstance().getMessages().invalidArguments(), Map.of("USAGE", "/itemmanager add <name>"));
            return;
        }

        final ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item.getType().isAir()) {
            Messager.sendMessage(sender, ConfigManager.getInstance().getMessages().mustHoldItem());
            return;
        }

        final String itemName = args[1];

        final boolean success = ItemManager.getApi().registerCustomItem(itemName, item);

        if (success) {
            Messager.sendMessage(sender, ConfigManager.getInstance().getMessages().itemRegistered(), Map.of("ITEM", itemName));
        } else {
            Messager.sendMessage(sender, ConfigManager.getInstance().getMessages().itemAlreadyRegistered(), Map.of("ITEM", itemName));
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        return List.of();
    }
}
