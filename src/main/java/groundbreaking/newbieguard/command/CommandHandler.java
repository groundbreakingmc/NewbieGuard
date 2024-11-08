package groundbreaking.newbieguard.command;

import groundbreaking.newbieguard.NewbieGuard;
import groundbreaking.newbieguard.listeners.ChatMessagesListener;
import groundbreaking.newbieguard.listeners.CommandsListeners;
import groundbreaking.newbieguard.utils.PlaceholdersUtil;
import groundbreaking.newbieguard.utils.config.ConfigValues;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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

        if (args.length > 1) {
            this.usageError(sender);
            return true;
        }

        final String input = args[0].toLowerCase();

        if (!sender.hasPermission("newbieguard." + input)) {
            final String message = this.configValues.getNoPermMessage();
            final String formattedMessage = PlaceholdersUtil.parse(sender, message);
            sender.sendMessage(formattedMessage);
            return true;
        }

        switch(input) {
            case "help" -> this.help(sender);
            case "reload" -> this.reload(sender);
            case "removemessages" -> this.removeMessages(sender, args);
            case "removecommands" -> this.removeCommands(sender, args);
            case "cleardb" -> {
                if (sender instanceof ConsoleCommandSender) {
                    sender.sendMessage("[NewbieGuard] §c=================================================================");
                    sender.sendMessage("[NewbieGuard] §c| Are you sure you want to clear the database of verified players");
                    sender.sendMessage("[NewbieGuard] §c| This action is irreversible!");
                    sender.sendMessage("[NewbieGuard] §c|");
                    sender.sendMessage("[NewbieGuard] §c| If yes, execute \"/newbieguard\"");
                    this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () ->
                            this.waitConfirm = false, 300L);
                }
                else if (this.hasAnyPermission(sender)) {
                    sender.sendMessage("§cThis command can be executed only from console!");
                }
            }
            case "confirm" -> {
                if (sender instanceof ConsoleCommandSender) {
                    if (!this.waitConfirm) {
                        sender.sendMessage("§c[NewbieGuard] First you have to execute \"/newbieguard cleardb\"!");
                    }
                    this.plugin.getDatabaseHandler().clear();
                    for (final Player player : Bukkit.getOnlinePlayers()) {
                        final String playerName = player.getName();
                        ChatMessagesListener.MESSAGES.add(playerName);
                        CommandsListeners.COMMANDS.add(playerName);
                    }
                    sender.sendMessage("§c[NewbieGuard] Database cleared.");
                    this.waitConfirm = false;
                }
                else if (this.hasAnyPermission(sender)) {
                    sender.sendMessage("§cThis command can be executed only from console!");
                }
            }
            default -> this.usageError(sender);
        }

        return true;
    }

    public void reload(final CommandSender sender) {
        final long reloadStartTime = System.currentTimeMillis();

        this.plugin.getDatabaseHandler().close();
        this.plugin.reload();

        final long reloadFinishTime = System.currentTimeMillis();
        final String timeLeft = String.valueOf(reloadFinishTime - reloadStartTime);
        final String message = this.configValues.getReloadMessage().replace("%time%", timeLeft);
        final String formattedMessage = PlaceholdersUtil.parse(sender, message);
        sender.sendMessage(formattedMessage);
    }

    public void help(final CommandSender sender) {
        final String message = this.configValues.getHelpMessage();
        final String formattedMessage = PlaceholdersUtil.parse(sender, message);
        sender.sendMessage(formattedMessage);
    }

    public void removeMessages(final CommandSender sender, final String[] args) {
        if (args.length < 2) {
            this.usageError(sender);
            return;
        }

        final Player target = this.plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            final String message = this.configValues.getPlayerNotFound().replace("{player}", args[1]);
            final String formattedMessage = PlaceholdersUtil.parse(sender, message);
            sender.sendMessage(formattedMessage);
            return;
        }

        ChatMessagesListener.MESSAGES.add(args[1]);
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () ->
                this.plugin.getDatabaseHandler().removePlayerFromChatTable(args[1])
        );

        final String message = this.configValues.getRemovedFromMessagesMessage().replace("{player}", args[1]);
        final String formattedMessage = PlaceholdersUtil.parse(sender, message);
        sender.sendMessage(formattedMessage);
    }

    public void removeCommands(final CommandSender sender, final String[] args) {
        if (args.length < 2) {
            this.usageError(sender);
            return;
        }

        final Player target = this.plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            final String message = this.configValues.getPlayerNotFound().replace("{player}", args[1]);
            final String formattedMessage = PlaceholdersUtil.parse(sender, message);
            sender.sendMessage(formattedMessage);
            return;
        }

        CommandsListeners.COMMANDS.add(args[1]);
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () ->
                this.plugin.getDatabaseHandler().removePlayerFromCommandsTable(args[1])
        );

        final String message = this.configValues.getRemovedFromCommandsMessage().replace("{player}", args[1]);
        final String formattedMessage = PlaceholdersUtil.parse(sender, message);
        sender.sendMessage(formattedMessage);
    }

    public void usageError(final CommandSender sender) {
        if (this.hasAnyPermission(sender)) {
            final String message = this.configValues.getUsageErrorMessage();
            final String formattedMessage = PlaceholdersUtil.parse(sender, message);
            sender.sendMessage(formattedMessage);
        } else {
            final String message = this.configValues.getNoPermMessage();
            final String formattedMessage = PlaceholdersUtil.parse(sender, message);
            sender.sendMessage(formattedMessage);
        }
    }

    private boolean hasAnyPermission(final CommandSender sender) {
        if (sender instanceof ConsoleCommandSender) {
            return true;
        }

        return sender.hasPermission("newbieguard.command.reload")
                || sender.hasPermission("newbieguard.command.help")
                || sender.hasPermission("newbieguard.command.removechat")
                || sender.hasPermission("newbieguard.command.removecommands");
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        final List<String> list = new ArrayList<>();

        if (sender instanceof ConsoleCommandSender) {
            if (this.waitConfirm) {
                list.add("cleardb");
            } else {
                list.add("confirm");
            }
        }

        if (sender.hasPermission("newbieguard.command.reload")) {
            list.add("reload");
        }
        if (sender.hasPermission("newbieguard.command.help")) {
            list.add("help");
        }
        if (sender.hasPermission("newbieguard.command.removechat")) {
            list.add("removechat");
        }
        if (sender.hasPermission("newbieguard.command.removecommands")) {
            list.add("removecommands");
        }

        return list;
    }
}
