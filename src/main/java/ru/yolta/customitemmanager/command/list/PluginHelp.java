package ru.yolta.customitemmanager.command.list;

import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import ru.yolta.customitemmanager.command.SubCommand;
import ru.yolta.customitemmanager.config.MessageConfig;
import ru.yolta.customitemmanager.utils.Messenger;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PluginHelp implements SubCommand {

    private static final Set<String> ALIASES = Set.of("help");
    private static final Permission PERMISSION = new Permission("customitemmanager.command.help");
    private final MessageConfig.SharedCmdSection sharedMessages;
    private final MessageConfig.HelpPluginCmdSection cmdMessages;

    public PluginHelp(
            @NotNull MessageConfig.SharedCmdSection sharedMessages,
            @NotNull MessageConfig.HelpPluginCmdSection cmdMessages
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
        if (args.length != 1) {
            Messenger.sendMessage(sender, sharedMessages.invalidArguments(), Map.of("USAGE", "/cim help"));
            return;
        }

        Messenger.sendMessage(sender, cmdMessages.pluginHelp());
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        return List.of();
    }
}
