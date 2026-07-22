package ru.yolta.customitemmanager.command.list;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import ru.yolta.customitemmanager.CustomItemManager;
import ru.yolta.customitemmanager.command.SubCommand;
import ru.yolta.customitemmanager.config.MessageConfig;
import ru.yolta.customitemmanager.utils.Messenger;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class GiveItem implements SubCommand {

    private static final Set<String> ALIASES = Set.of("give");
    private static final Permission PERMISSION = new Permission("customitemmanager.command.give");
    private final MessageConfig.SharedCmdSection sharedMessages;
    private final MessageConfig.GiveItemCmdSection cmdMessages;

    public GiveItem(
            @NotNull MessageConfig.SharedCmdSection sharedMessages,
            @NotNull MessageConfig.GiveItemCmdSection cmdMessages
    ) {
        this.sharedMessages = sharedMessages;
        this.cmdMessages = cmdMessages;
    }

    public static @NotNull @Unmodifiable Set<String> getAliases() {
        return ALIASES;
    }

    public static @NotNull Permission getPermission() {
        return PERMISSION;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (args.length < 3 || args.length > 4) {
            Messenger.sendMessage(sender, sharedMessages.invalidArguments(), Map.of("USAGE", "/cim give <player> <name> [amount]"));
            return;
        }

        final String targetPlayerName = args[1];
        final Player targetPlayer = Bukkit.getPlayer(targetPlayerName);

        if (targetPlayer == null) {
            Messenger.sendMessage(sender, cmdMessages.playerNotFound(), Map.of("PLAYER", targetPlayerName));
            return;
        }

        final String itemName = args[2];
        final Optional<ItemStack> optionalItem = CustomItemManager.getApi().getCustomItem(itemName);

        if (optionalItem.isEmpty()) {
            Messenger.sendMessage(sender, cmdMessages.itemNotFound(), Map.of("ITEM", itemName));
            return;
        }

        final ItemStack item = optionalItem.get();

        int itemAmount = 1;

        if (args.length == 4) {
            try {
                itemAmount = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                Messenger.sendMessage(sender, cmdMessages.invalidItemAmount(), Map.of("AMOUNT", args[3]));
                return;
            }
        }

        item.setAmount(itemAmount);

        final var itemsNotFitted = targetPlayer.getInventory().addItem(item);

        for (final var entry : itemsNotFitted.entrySet()) {
            targetPlayer.getWorld().dropItemNaturally(targetPlayer.getLocation(), entry.getValue());
        }

        Messenger.sendMessage(sender, cmdMessages.itemGiven(), Map.of("PLAYER", targetPlayerName, "ITEM", itemName, "AMOUNT", String.valueOf(itemAmount)));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (args.length == 2)
            return null; // Let Bukkit handle player name suggestions

        if (args.length == 3)
            return List.copyOf(CustomItemManager.getApi().getAllCustomItemIds());

        return List.of();
    }
}
