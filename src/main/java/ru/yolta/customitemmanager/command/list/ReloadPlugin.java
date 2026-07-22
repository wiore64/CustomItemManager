package ru.yolta.customitemmanager.command.list;

import org.bukkit.command.CommandSender;
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
import java.util.Set;

public final class ReloadPlugin implements SubCommand {

    private static final Set<String> ALIASES = Set.of("reload");
    private static final Permission PERMISSION = new Permission("customitemmanager.command.reload");
    private final MessageConfig.SharedCmdSection sharedMessages;
    private final MessageConfig.ReloadPluginCmdSection cmdMessages;

    public ReloadPlugin(
            @NotNull MessageConfig.SharedCmdSection sharedMessages,
            @NotNull MessageConfig.ReloadPluginCmdSection cmdMessages
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
            Messenger.sendMessage(sender, sharedMessages.invalidArguments(), Map.of("USAGE", "/cim reload"));
            return;
        }

        CustomItemManager.getApi().reloadPlugin();

        Messenger.sendMessage(sender, cmdMessages.pluginReloaded());
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        return List.of();
    }
}
