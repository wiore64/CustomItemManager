package ru.vladimir.itemmanager.command.list;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ru.vladimir.itemmanager.ItemManager;
import ru.vladimir.itemmanager.command.SubCommand;
import ru.vladimir.itemmanager.config.ConfigManager;
import ru.vladimir.itemmanager.utils.Logger;
import ru.vladimir.itemmanager.utils.Messager;

public class GiveItem implements SubCommand {

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (args.length < 3 || args.length > 4) {
            Messager.sendMessage(sender, ConfigManager.getInstance().getMessages().invalidArguments(), Map.of("USAGE", "/itemmanager give <player> <name> [amount]"));
            return;
        }

        final String targetPlayerName = args[1];
        final Player targetPlayer = sender.getServer().getPlayer(targetPlayerName);

        if (targetPlayer == null) {
            Messager.sendMessage(sender, ConfigManager.getInstance().getMessages().playerNotFound(), Map.of("PLAYER", targetPlayerName));
            return;
        }

        final String itemName = args[2];
        final Optional<ItemStack> optionalItem = ItemManager.getApi().getCustomItem(itemName);

        if (optionalItem.isEmpty()) {
            Messager.sendMessage(sender, ConfigManager.getInstance().getMessages().itemNotFound(), Map.of("ITEM", itemName));
            return;
        }

        final ItemStack item = optionalItem.get();

        int itemAmount = 1;

        if (args.length == 4) {
            try {
                itemAmount = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                Messager.sendMessage(sender, ConfigManager.getInstance().getMessages().invalidAmount(), Map.of("AMOUNT", args[3]));
                return;
            }
        }

        item.setAmount(itemAmount);

        final var itemsNotFitted = targetPlayer.getInventory().addItem(item);

        for (final var entry : itemsNotFitted.entrySet()) {
            targetPlayer.getWorld().dropItemNaturally(targetPlayer.getLocation(), entry.getValue());
        }

        Messager.sendMessage(sender, ConfigManager.getInstance().getMessages().itemGiven(), Map.of("PLAYER", targetPlayerName, "ITEM", itemName, "AMOUNT", String.valueOf(itemAmount)));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (args.length == 2)
            return null; // Let Bukkit handle player name suggestions

        if (args.length == 3)
            return List.copyOf(ItemManager.getApi().getAllCustomItemIds());

        return List.of();
    }
}
