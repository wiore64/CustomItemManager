package ru.vladimir.itemmanager.command;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.NotNull;

import ru.vladimir.itemmanager.utils.Logger;

public final class CommandService {
    private static CommandService instance;
    private Map<String, SubCommandWrapper> subCommandRegistry;

    private CommandService() {}

    public static @NotNull CommandService getInstance() {
        if (instance == null)
            throw new IllegalStateException("Attempted to get instance before it was initialized.");
        return instance;
    }

    public static void init() {
        if (instance != null) {
            Logger.warn(instance, "Attempted to initialize an instance while it is already initialized.");
            return;
        }

        instance = new CommandService();

        instance.subCommandRegistry = new ConcurrentHashMap<>();
        instance.registerSubCommands();

        Logger.info(instance, "Successfully initialized.");
    }

    public static void destroy() {
        if (instance == null) {
            Logger.warn(CommandService.class, "Attempted to destroy an instance while there is none.");
            return;
        }

        instance = null;

        Logger.info(CommandService.class, "Successfully destroyed.");
    }

    private void registerSubCommands() {
        // Here we register subcommands via the registerSubCommand method.
    }

    private void registerSubCommand(Iterable<String> aliases, SubCommandWrapper wrapper) {
        for (final String alias : aliases) {
            final boolean isAdded = subCommandRegistry.putIfAbsent(alias, wrapper) == null;
            if (isAdded)
                Logger.debug(this, "Registered subcommand with alias: %s.".formatted(alias));
            else
                Logger.warn(this, "Attempted to register subcommand with alias: %s, but it is already registered.".formatted(alias));
        }
    }

    Optional<SubCommandWrapper> getWrapperForAlias(String alias) {
        return Optional.ofNullable(subCommandRegistry.get(alias));
    }
}
