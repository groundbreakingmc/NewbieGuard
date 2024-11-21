package groundbreaking.newbieguard.command;

import groundbreaking.newbieguard.NewbieGuard;
import groundbreaking.newbieguard.utils.PlaceholdersUtil;
import groundbreaking.newbieguard.utils.UpdatesChecker;
import groundbreaking.newbieguard.utils.config.ConfigValues;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class CommandHandler implements CommandExecutor, TabCompleter {

    private final NewbieGuard plugin;
    private final ConfigValues configValues;

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
            case "update" -> this.update(sender);
            case "checkupdate" -> this.checkUpdate(sender);
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

        this.configValues.setupValues();

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

    public boolean checkUpdate(final CommandSender sender) {
        if (!(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage("§4[NewbieGuard] §cThis command can only be executed only by the console!");
            return true;
        }

        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> new UpdatesChecker(plugin).check(true, false));
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
                || sender.hasPermission("newbieguard.command.help");
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        final List<String> list = new ArrayList<>();
        final String input = args[args.length - 1];

        if (args.length == 1) {
            if (sender instanceof ConsoleCommandSender) {
                this.addConsoleTabCompletions(input, list);
            }

            if ("reload".startsWith(input) && sender.hasPermission("newbieguard.command.reload")) {
                list.add("reload");
            }
            if ("help".startsWith(input) && sender.hasPermission("newbieguard.command.help")) {
                list.add("help");
            }
        }

        return list;
    }

    private void addConsoleTabCompletions(final String input, final List<String> list) {
        if (UpdatesChecker.hasUpdate() && "update".startsWith(input)) {
            list.add("update");
        } else if (!UpdatesChecker.hasUpdate() && "checkupdate".startsWith(input)) {
            list.add("checkupdate");
        }
    }
}
