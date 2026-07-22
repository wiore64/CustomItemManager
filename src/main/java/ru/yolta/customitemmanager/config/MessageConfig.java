package ru.yolta.customitemmanager.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import ru.yolta.customitemmanager.utils.Logger;

import java.io.File;
import java.util.Map;

// todo: Before update, consider when sections are not found, and migrating to new config.
public record MessageConfig(
        @NotNull String prefix,
        @NotNull MainCmdSection mainCmd,
        @NotNull SharedCmdSection sharedCmd,
        @NotNull AddItemCmdSection addItemCmd,
        @NotNull RemoveItemCmdSection removeItemCmd,
        @NotNull GiveItemCmdSection giveItemCmd,
        @NotNull ReloadPluginCmdSection reloadPluginCmd,
        @NotNull HelpPluginCmdSection helpPluginCmd,
        @NotNull ManageItemCmdSection manageItemCmd
) {
    private static final String LOG_NAME = "MessageConfig";
    private static boolean shouldSaveConfig = false;

    static @NotNull MessageConfig parseMessageConfig(
            @NotNull ConfigManager manager,
            @NotNull File file,
            @NotNull FileConfiguration fileConfig
    ) {
        final int configVersion = fileConfig.getInt("config-version", 2);

        if (configVersion != 2) {
            Logger.info(LOG_NAME, "New config version found. Yours will be safely archived.");
            // todo
            // get old values, insert to the new ones
            // rename old config to old config, and save new config
        }

        ConfigurationSection messages = fileConfig.getConfigurationSection("messages");

        if (messages == null) {
            Logger.warn(LOG_NAME, "Failed to parse section 'messages': Not found.");

            shouldSaveConfig = true;
            messages = fileConfig.createSection("messages");
        }

        final MessageConfig config = new MessageConfig(
                getValue(messages, "prefix", "<gradient:#F47854:#B67E54>[CIM]:</gradient>"),
                parseMainCmd(messages),
                parseSharedCmd(messages),
                parseAddItemCmd(messages),
                parseRemoveItemCmd(messages),
                parseGiveItemCmd(messages),
                parseReloadPluginCmd(messages),
                parseHelpPluginCmd(messages),
                parseManageItemCmd(messages)
        );

        if (shouldSaveConfig) {
            shouldSaveConfig = false;
            manager.saveConfig(file, fileConfig);
        }

        return config;
    }

    private static MainCmdSection parseMainCmd(ConfigurationSection parent) {
        final ConfigurationSection section = getSection(parent, "main-cmd");

        return new MainCmdSection(
                getValue(section, "no-permission", MainCmdSection.MAIN_DEFAULTS.get("no-permission")),
                getValue(section, "plugin-description", MainCmdSection.MAIN_DEFAULTS.get("plugin-description")),
                getValue(section, "invalid-command", MainCmdSection.MAIN_DEFAULTS.get("invalid-command"))
        );
    }

    private static SharedCmdSection parseSharedCmd(ConfigurationSection parent) {
        final ConfigurationSection section = getSection(parent, "shared-cmd");

        return new SharedCmdSection(
                getValue(section, "prefix", SharedCmdSection.SHARED_DEFAULTS.get("prefix")),
                getValue(section, "player-only-command", SharedCmdSection.SHARED_DEFAULTS.get("player-only-command")),
                getValue(section, "invalid-arguments", SharedCmdSection.SHARED_DEFAULTS.get("invalid-arguments")),
                getValue(section, "must-hold-item", SharedCmdSection.SHARED_DEFAULTS.get("must-hold-item"))
        );
    }

    private static AddItemCmdSection parseAddItemCmd(ConfigurationSection parent) {
        final ConfigurationSection section = getSection(parent, "add-item-cmd");

        return new AddItemCmdSection(
                getValue(section, "item-registered", AddItemCmdSection.ADD_ITEM_DEFAULTS.get("item-registered")),
                getValue(section, "item-already-registered", AddItemCmdSection.ADD_ITEM_DEFAULTS.get("item-already-registered"))
        );
    }

    private static RemoveItemCmdSection parseRemoveItemCmd(ConfigurationSection parent) {
        final ConfigurationSection section = getSection(parent, "remove-item-cmd");

        return new RemoveItemCmdSection(
                getValue(section, "item-unregistered", RemoveItemCmdSection.REMOVE_ITEM_DEFAULTS.get("item-unregistered")),
                getValue(section, "item-not-registered", RemoveItemCmdSection.REMOVE_ITEM_DEFAULTS.get("item-not-registered"))
        );
    }

    private static GiveItemCmdSection parseGiveItemCmd(ConfigurationSection parent) {
        final ConfigurationSection section = getSection(parent, "give-item-cmd");

        return new GiveItemCmdSection(
                getValue(section, "player-not-found", GiveItemCmdSection.GIVE_ITEM_DEFAULTS.get("player-not-found")),
                getValue(section, "item-not-found", GiveItemCmdSection.GIVE_ITEM_DEFAULTS.get("item-not-found")),
                getValue(section, "invalid-item-amount", GiveItemCmdSection.GIVE_ITEM_DEFAULTS.get("invalid-item-amount")),
                getValue(section, "item-given", GiveItemCmdSection.GIVE_ITEM_DEFAULTS.get("item-given"))
        );
    }

    private static ReloadPluginCmdSection parseReloadPluginCmd(ConfigurationSection parent) {
        final ConfigurationSection section = getSection(parent, "reload-plugin-cmd");

        return new ReloadPluginCmdSection(
                getValue(section, "plugin-reloaded", ReloadPluginCmdSection.RELOAD_DEFAULTS.get("plugin-reloaded"))
        );
    }

    private static HelpPluginCmdSection parseHelpPluginCmd(ConfigurationSection parent) {
        final ConfigurationSection section = getSection(parent, "help-plugin-cmd");

        return new HelpPluginCmdSection(
                getValue(section, "plugin-help", HelpPluginCmdSection.HELP_DEFAULTS.get("plugin-help"))
        );
    }

    private static ManageItemCmdSection parseManageItemCmd(ConfigurationSection parent) {
        final ConfigurationSection section = getSection(parent, "manage-item-cmd");

        return new ManageItemCmdSection(
                getValue(section, "name-updated", ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("name-updated")),
                getValue(section, "name-cleared", ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("name-cleared")),

                getValue(section, "lore-line-appended", ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("lore-line-appended")),
                getValue(section, "lore-bad-index", ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("lore-bad-index")),
                getValue(section, "lore-large-index", ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("lore-large-index")),
                getValue(section, "lore-line-updated", ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("lore-line-updated")),
                getValue(section, "lore-line-removed", ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("lore-line-removed")),
                getValue(section, "lore-cleared", ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("lore-cleared")),

                getValue(section, "model-updated", ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("model-updated")),
                getValue(section, "model-cleared", ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("model-cleared")),
                getValue(section, "invalid-model-id", ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("invalid-model-id")),

                getValue(section, "unknown-enchantment", ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("unknown-enchantment")),
                getValue(section, "invalid-enchantment-level", ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("invalid-enchantment-level")),
                getValue(section, "out-of-bounds-enchantment-level", ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("out-of-bounds-enchantment-level")),
                getValue(section, "enchantment-already-present", ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("enchantment-already-present")),
                getValue(section, "enchantment-not-present", ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("enchantment-not-present")),
                getValue(section, "enchantment-updated", ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("enchantment-updated")),
                getValue(section, "enchantment-removed", ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("enchantment-removed")),
                getValue(section, "enchantments-cleared", ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("enchantments-cleared")),

                getValue(section, "invalid-key", ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("invalid-key")),
                getValue(section, "duplicate-key", ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("duplicate-key")),
                getValue(section, "key-not-found", ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("key-not-found")),
                getValue(section, "key-added", ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("key-added")),
                getValue(section, "key-removed", ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("key-removed"))
        );
    }

    private static ConfigurationSection getSection(
            ConfigurationSection parent,
            String key
    ) {
        ConfigurationSection section = parent.getConfigurationSection(key);

        if (section == null) {
            Logger.warn(LOG_NAME, "Failed to parse section '{}': Not found.", key);

            shouldSaveConfig = true;
            section = parent.createSection(key);
        }

        return section;
    }

    private static String getValue(
            ConfigurationSection section,
            String key,
            String defValue
    ) {
        final String value = section.getString(key);

        if (value == null) {
            Logger.warn(LOG_NAME, "Failed to parse '{}': Not found.", key);

            shouldSaveConfig = true;
            section.set(key, defValue);

            return defValue;
        }

        return value;
    }

    public record MainCmdSection(
            @NotNull String noPermission,
            @NotNull String pluginDescription,
            @NotNull String invalidCommand
    ) {
        private static final Map<String, String> MAIN_DEFAULTS = Map.of(
                "no-permission", "<red>You do not have permission to run this command!</red>",
                "plugin-description", "A <gold>powerful and centralized</gold> custom <aqua>item management</aqua> plugin.",
                "invalid-command", "<red>Unknown command.<red> Use <gold>/cim help.</gold>"
        );

        private static MainCmdSection def() {
            return new MainCmdSection(
                    MainCmdSection.MAIN_DEFAULTS.get("no-permission"),
                    MainCmdSection.MAIN_DEFAULTS.get("plugin-description"),
                    MainCmdSection.MAIN_DEFAULTS.get("invalid-command")
            );
        }
    }

    public record SharedCmdSection(
            @NotNull String pluginPrefix,
            @NotNull String playerOnlyCommand,
            @NotNull String invalidArguments,
            @NotNull String mustHoldItem
    ) {
        private static final Map<String, String> SHARED_DEFAULTS = Map.of(
                "prefix", "<gradient:#F47854:#B67E54>[CIM]:</gradient>",
                "player-only-command", "<red>This command can only be used by players.</red>",
                "invalid-arguments", "<red>Invalid arguments.</red> Usage: <gold>{USAGE}</gold>",
                "must-hold-item", "<gold>You must be holding an item.</gold>"
        );

        private static SharedCmdSection def() {
            return new SharedCmdSection(
                    SharedCmdSection.SHARED_DEFAULTS.get("prefix"),
                    SharedCmdSection.SHARED_DEFAULTS.get("player-only-command"),
                    SharedCmdSection.SHARED_DEFAULTS.get("invalid-arguments"),
                    SharedCmdSection.SHARED_DEFAULTS.get("must-hold-item")
            );
        }
    }

    public record AddItemCmdSection(
            @NotNull String itemRegistered,
            @NotNull String itemAlreadyRegistered
    ) {
        private static final Map<String, String> ADD_ITEM_DEFAULTS = Map.of(
                "item-registered", "<green>Item {ITEM} registered.</green>",
                "item-already-registered", "<gold>Item {ITEM} is already registered.</gold>"
        );

        private static AddItemCmdSection def() {
            return new AddItemCmdSection(
                    AddItemCmdSection.ADD_ITEM_DEFAULTS.get("item-registered"),
                    AddItemCmdSection.ADD_ITEM_DEFAULTS.get("item-already-registered")
            );
        }
    }

    public record RemoveItemCmdSection(
            @NotNull String itemUnregistered,
            @NotNull String itemNotRegistered
    ) {
        private static final Map<String, String> REMOVE_ITEM_DEFAULTS = Map.of(
                "item-unregistered", "<green>Item {ITEM} unregistered.</green>",
                "item-not-registered", "<red>Item {ITEM} is not registered.</red>"
        );

        private static RemoveItemCmdSection def() {
            return new RemoveItemCmdSection(
                    RemoveItemCmdSection.REMOVE_ITEM_DEFAULTS.get("item-unregistered"),
                    RemoveItemCmdSection.REMOVE_ITEM_DEFAULTS.get("item-not-registered")
            );
        }
    }

    public record GiveItemCmdSection(
            @NotNull String playerNotFound,
            @NotNull String itemNotFound,
            @NotNull String invalidItemAmount,
            @NotNull String itemGiven
    ) {
        private static final Map<String, String> GIVE_ITEM_DEFAULTS = Map.of(
                "player-not-found", "<gold>Player {PLAYER} was not found.</gold>",
                "item-not-found", "<gold>Item {ITEM} was not found.</gold>",
                "invalid-item-amount", "<gold>Invalid amount {AMOUNT}. Must be a positive integer.</gold>",
                "item-given", "<green>Gave {AMOUNT}x {ITEM} to {PLAYER}.</green>"
        );

        private static GiveItemCmdSection def() {
            return new GiveItemCmdSection(
                    GiveItemCmdSection.GIVE_ITEM_DEFAULTS.get("player-not-found"),
                    GiveItemCmdSection.GIVE_ITEM_DEFAULTS.get("item-not-found"),
                    GiveItemCmdSection.GIVE_ITEM_DEFAULTS.get("invalid-item-amount"),
                    GiveItemCmdSection.GIVE_ITEM_DEFAULTS.get("item-given")
            );
        }
    }

    public record ReloadPluginCmdSection(
            @NotNull String pluginReloaded
    ) {
        private static final Map<String, String> RELOAD_DEFAULTS = Map.of(
                "plugin-reloaded", "<green>Plugin reloaded.</green>"
        );

        private static ReloadPluginCmdSection def() {
            return new ReloadPluginCmdSection(
                    ReloadPluginCmdSection.RELOAD_DEFAULTS.get("plugin-reloaded")
            );
        }
    }

    public record HelpPluginCmdSection(
            @NotNull String pluginHelp
    ) {
        private static final Map<String, String> HELP_DEFAULTS = Map.of(
                "plugin-help", "List of commands: <gold>\n/cim add <key>: Adds a new item held in your hand with key serving as identifier\n/cim remove <key>: Removes the specified item\n/cim give <player> <key> [amount]: Gives item to player in the specified amount\n/cim reload: Reloads the plugin\n/cim help: Reveals this message"
        );

        private static HelpPluginCmdSection def() {
            return new HelpPluginCmdSection(
                    HelpPluginCmdSection.HELP_DEFAULTS.get("plugin-help")
            );
        }
    }

    public record ManageItemCmdSection(
            @NotNull String itemCmdNameUpdated,
            @NotNull String itemCmdNameCleared,

            @NotNull String itemCmdLoreLineAppended,
            @NotNull String itemCmdLoreBadIndex,
            @NotNull String itemCmdLoreLargeIndex,
            @NotNull String itemCmdLoreLineUpdated,
            @NotNull String itemCmdLoreLineRemoved,
            @NotNull String itemCmdLoreCleared,

            @NotNull String itemCmdModelUpdated,
            @NotNull String itemCmdModelCleared,
            @NotNull String itemCmdInvalidModelId,

            @NotNull String itemCmdUnknownEnchantment,
            @NotNull String itemCmdInvalidEnchantmentLevel,
            @NotNull String itemCmdOutOfBoundsEnchantmentLevel,
            @NotNull String itemCmdEnchantmentAlreadyPresent,
            @NotNull String itemCmdEnchantmentNotPresent,
            @NotNull String itemCmdEnchantmentUpdated,
            @NotNull String itemCmdEnchantmentRemoved,
            @NotNull String itemCmdEnchantmentsCleared,

            @NotNull String itemCmdInvalidKey,
            @NotNull String itemCmdDuplicateKey,
            @NotNull String itemCmdKeyNotFound,
            @NotNull String itemCmdKeyAdded,
            @NotNull String itemCmdKeyRemoved
    ) {
        private static final Map<String, String> MANAGE_ITEM_DEFAULTS = Map.ofEntries(
                Map.entry("name-updated", "<green>This item's name was updated.</green>"),
                Map.entry("name-cleared", "<green>This item's name was cleared.</green>"),

                Map.entry("lore-line-appended", "<green>This item's lore line was added.</green>"),
                Map.entry("lore-bad-index", "<red>This item's lore line {INDEX} is invalid. Lore lines must be between 1 and {MAX_INDEX}.</red>"),
                Map.entry("lore-large-index", "<red>This item's lore line {INDEX} is invalid. The maximum lore line is {MAX_INDEX}.</red>"),
                Map.entry("lore-line-updated", "<green>This item's lore line was updated.</green>"),
                Map.entry("lore-line-removed", "<green>This item's lore line was removed.</green>"),
                Map.entry("lore-cleared", "<green>This item's lore was cleared.</green>"),

                Map.entry("model-updated", "<green>This item's model was updated.</green>"),
                Map.entry("model-cleared", "<green>This item's model was cleared.</green>"),
                Map.entry("invalid-model-id", "<red>This item's model id {MODEL} is invalid.</red>"),

                Map.entry("unknown-enchantment", "<red>This item does not have the enchantment {ENCHANTMENT}.</red>"),
                Map.entry("invalid-enchantment-level", "<red>Enchantment levels must be between 0 and 255.</red>"),
                Map.entry("out-of-bounds-enchantment-level", "<red>Enchantment level {LEVEL} must be between 0 and 255.</red>"),
                Map.entry("enchantment-already-present", "<red>This item already has the enchantment {ENCHANTMENT} at level {LEVEL}.</red>"),
                Map.entry("enchantment-not-present", "<red>This item does not have the enchantment {ENCHANTMENT}.</red>"),
                Map.entry("enchantment-updated", "<green>This item's enchantment was updated.</green>"),
                Map.entry("enchantment-removed", "<green>This item's enchantment was removed.</green>"),
                Map.entry("enchantments-cleared", "<green>This item's enchantments were cleared.</green>"),

                Map.entry("invalid-key", "<red>This item's key {KEY} is invalid.</red>"),
                Map.entry("duplicate-key", "<red>This item already has the key {KEY}.</red>"),
                Map.entry("key-not-found", "<red>This item's key {KEY} was not found.</red>"),
                Map.entry("key-added", "<green>This item's key was added.</green>"),
                Map.entry("key-removed", "<green>This item's key was removed.</green>")
        );

        private static ManageItemCmdSection def() {
            return new ManageItemCmdSection(
                    ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("name-updated"),
                    ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("name-cleared"),

                    ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("lore-line-appended"),
                    ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("lore-bad-index"),
                    ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("lore-large-index"),
                    ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("lore-line-updated"),
                    ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("lore-line-removed"),
                    ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("lore-cleared"),

                    ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("model-updated"),
                    ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("model-cleared"),
                    ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("invalid-model-id"),

                    ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("unknown-enchantment"),
                    ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("invalid-enchantment-level"),
                    ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("out-of-bounds-enchantment-level"),
                    ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("enchantment-already-present"),
                    ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("enchantment-not-present"),
                    ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("enchantment-updated"),
                    ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("enchantment-removed"),
                    ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("enchantments-cleared"),

                    ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("invalid-key"),
                    ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("duplicate-key"),
                    ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("key-not-found"),
                    ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("key-added"),
                    ManageItemCmdSection.MANAGE_ITEM_DEFAULTS.get("key-removed")
            );
        }
    }
}
