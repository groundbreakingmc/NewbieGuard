package groundbreaking.newbieguard.command;

import groundbreaking.newbieguard.NewbieGuard;
import groundbreaking.newbieguard.database.DatabaseHandler;
import groundbreaking.newbieguard.listeners.ChatMessagesListener;
import groundbreaking.newbieguard.listeners.CommandsListeners;
import groundbreaking.newbieguard.utils.PlaceholdersUtil;
import groundbreaking.newbieguard.utils.UpdatesChecker;
import groundbreaking.newbieguard.utils.config.ConfigValues;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class CommandHandler implements CommandExecutor, TabCompleter {

    private final NewbieGuard plugin;
    private final ConfigValues configValues;

    private boolean waitConfirm = false;

    public CommandHandler(final NewbieGuard plugin) {
        this.plugin = plugin;
        this.configValues = plugin.getConfigValues();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            this.usageError(sender);
            return true;
        }

        final String input = args[0].toLowerCase();
        if (!this.hasAnyPermission(sender)) {
            return noPermission((Player) sender);
        }

        return switch (input) {
            case "help" -> this.help(sender);
            case "reload" -> this.reload(sender);
            case "removemessages" -> this.removeMessages(sender, args);
            case "removecommands" -> this.removeCommands(sender, args);
            case "cleardb" -> this.clearDb(sender);
            case "confirm" -> this.confirm(sender);
            case "update" -> this.update(sender);
            default -> this.usageError(sender);
        };
    }

    private boolean noPermission(final Player sender) {
        final String message = this.configValues.getNoPermMessage();
        final String formattedMessage = PlaceholdersUtil.parse(sender, message);
        sender.sendMessage(formattedMessage);
        return true;
    }

    public boolean reload(final CommandSender sender) {
        final long reloadStartTime = System.currentTimeMillis();

        this.plugin.getDatabaseHandler().close();
        this.plugin.reload();

        final long reloadFinishTime = System.currentTimeMillis();
        final String timeLeft = String.valueOf(reloadFinishTime - reloadStartTime);
        final String message = this.configValues.getReloadMessage().replace("%time%", timeLeft);
        final String formattedMessage = PlaceholdersUtil.parse(sender, message);
        sender.sendMessage(formattedMessage);
        return true;
    }

    public boolean help(final CommandSender sender) {
        final String message = this.configValues.getHelpMessage();
        final String formattedMessage = PlaceholdersUtil.parse(sender, message);
        sender.sendMessage(formattedMessage);
        return true;
    }

    public boolean removeMessages(final CommandSender sender, final String[] args) {
        if (args.length < 2) {
            this.usageError(sender);
            return true;
        }

        final Player target = this.plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            final String message = this.configValues.getPlayerNotFound().replace("{player}", args[1]);
            final String formattedMessage = PlaceholdersUtil.parse(sender, message);
            sender.sendMessage(formattedMessage);
            return true;
        }

        final UUID targetUUID = target.getUniqueId();
        ChatMessagesListener.MESSAGES.add(targetUUID);
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
            final DatabaseHandler databaseHandler = this.plugin.getDatabaseHandler();
            databaseHandler.executeUpdateQuery(targetUUID, DatabaseHandler.REMOVE_PLAYER_FROM_CHAT);
        });

        final String message = this.configValues.getRemovedFromMessagesMessage().replace("{player}", args[1]);
        final String formattedMessage = PlaceholdersUtil.parse(sender, message);
        sender.sendMessage(formattedMessage);
        return true;
    }

    public boolean removeCommands(final CommandSender sender, final String[] args) {
        if (args.length < 2) {
            this.usageError(sender);
            return true;
        }

        final Player target = this.plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            final String message = this.configValues.getPlayerNotFound().replace("{player}", args[1]);
            final String formattedMessage = PlaceholdersUtil.parse(sender, message);
            sender.sendMessage(formattedMessage);
            return true;
        }

        final UUID targetUUID = target.getUniqueId();
        CommandsListeners.COMMANDS.add(targetUUID);
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
            final DatabaseHandler databaseHandler = this.plugin.getDatabaseHandler();
            databaseHandler.executeUpdateQuery(targetUUID, DatabaseHandler.REMOVE_PLAYER_FROM_COMMANDS);
        });

        final String message = this.configValues.getRemovedFromCommandsMessage().replace("{player}", args[1]);
        final String formattedMessage = PlaceholdersUtil.parse(sender, message);
        sender.sendMessage(formattedMessage);
        return true;
    }

    private boolean clearDb(final CommandSender sender) {
        if (!(sender instanceof ConsoleCommandSender)) {
            if (this.hasAnyPermission(sender)) {
                sender.sendMessage("§4[NewbieGuard] §cThis command can be executed only from console!");
            } else {
                this.noPermission((Player) sender);
            }
        }

        if (waitConfirm) {
            sender.sendMessage("§4[NewbieGuard] §cYou have already executed this command! Use: \"§4/newbieguard confirm§c\" to confirm remove!");
            return true;
        }

        sender.sendMessage("§4[NewbieGuard] §c=================================================================");
        sender.sendMessage("§4[NewbieGuard] §c| Are you sure you want to clear the database of verified players");
        sender.sendMessage("§4[NewbieGuard] §c| This action is irreversible!");
        sender.sendMessage("§4[NewbieGuard] §c|");
        sender.sendMessage("§4[NewbieGuard] §c| If yes, execute \"§4/newbieguard confirm§c\"");

        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () ->
                        this.waitConfirm = false,
                400L
        );

        return true;
    }

    private boolean confirm(final CommandSender sender) {
        if (!(sender instanceof ConsoleCommandSender)) {
            if (this.hasAnyPermission(sender)) {
                sender.sendMessage("§4[NewbieGuard] §cThis command can be executed only from console!");
            } else {
                this.noPermission((Player) sender);
            }
        }

        if (!this.waitConfirm) {
            sender.sendMessage("§4[NewbieGuard] &cFirst you have to execute \"§4/newbieguard cleardb§c\"!");
            return true;
        }

        this.plugin.getDatabaseHandler().clearTables();

        for (final Player player : Bukkit.getOnlinePlayers()) {
            final UUID playerUUID = player.getUniqueId();
            ChatMessagesListener.MESSAGES.add(playerUUID);
            CommandsListeners.COMMANDS.add(playerUUID);
        }
        this.waitConfirm = false;

        sender.sendMessage("§4[NewbieGuard] §cDatabase cleared successfully.");
        return true;
    }

    public boolean update(final CommandSender sender) {
        if (!(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage("§4[NewbieGuard] §cThis command can only be executed only by the console!");
            return true;
        }

        if (!UpdatesChecker.hasUpdate()) {
            sender.sendMessage("§4[NewbieGuard] §cNothing to update!");
            return true;
        }

        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> new UpdatesChecker(plugin).downloadJar());
        return true;
    }

    public boolean usageError(final CommandSender sender) {
        final String message = this.configValues.getUsageErrorMessage();
        final String formattedMessage = PlaceholdersUtil.parse(sender, message);
        sender.sendMessage(formattedMessage);
        return true;
    }

    private boolean hasAnyPermission(final CommandSender sender) {
        return sender instanceof ConsoleCommandSender
                || sender.hasPermission("newbieguard.command.reload")
                || sender.hasPermission("newbieguard.command.help")
                || sender.hasPermission("newbieguard.command.removemessages")
                || sender.hasPermission("newbieguard.command.removecommands");
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        final List<String> list = new ArrayList<>();

        if (sender instanceof ConsoleCommandSender) {
            list.add(this.waitConfirm ? "confirm" : "cleardb");
            if (UpdatesChecker.hasUpdate()) {
                list.add("update");
            }
        }

        if (sender.hasPermission("newbieguard.command.reload")) {
            list.add("reload");
        }
        if (sender.hasPermission("newbieguard.command.help")) {
            list.add("help");
        }
        if (sender.hasPermission("newbieguard.command.removemessages")) {
            list.add("removemessages");
        }
        if (sender.hasPermission("newbieguard.command.removecommands")) {
            list.add("removecommands");
        }

        return list;
    }
}
