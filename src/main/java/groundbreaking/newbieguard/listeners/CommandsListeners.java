package groundbreaking.newbieguard.listeners;

import groundbreaking.newbieguard.NewbieGuard;
import groundbreaking.newbieguard.database.AbstractDB;
import groundbreaking.newbieguard.utils.TimeFormatter;
import groundbreaking.newbieguard.utils.commands.BlackList;
import groundbreaking.newbieguard.utils.commands.IMode;
import groundbreaking.newbieguard.utils.commands.WhiteList;
import groundbreaking.newbieguard.utils.time.FirstEntryCounter;
import groundbreaking.newbieguard.utils.time.ITimeCounter;
import groundbreaking.newbieguard.utils.time.OnlineCounter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import groundbreaking.newbieguard.utils.config.ConfigValues;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public final class CommandsListeners implements Listener {

    private final ConfigValues configValues;
    private final AbstractDB db;

    private final TimeFormatter timeFormatter = new TimeFormatter();
    private static ITimeCounter timeCounter;
    private static IMode mode;

    public CommandsListeners(NewbieGuard plugin) {
        this.configValues = plugin.getConfigValues();
        this.db = plugin.getConnectionHandler();
        setTimeCounter(plugin);
        setMode(plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommandSendLowest(final PlayerCommandPreprocessEvent event) {
        if (configValues.isCommandsUsePriorityLowest() && isValid(event)) {
            processEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onCommandSendLow(final PlayerCommandPreprocessEvent event) {
        if (configValues.isCommandsUsePriorityLow() && isValid(event)) {
            processEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCommandSendNormal(final PlayerCommandPreprocessEvent event) {
        if (configValues.isCommandsUsePriorityNormal() && isValid(event)) {
            processEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCommandSendHigh(final PlayerCommandPreprocessEvent event) {
        if (configValues.isCommandsUsePriorityHigh() && isValid(event)) {
            processEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommandSendHighest(final PlayerCommandPreprocessEvent event) {
        if (configValues.isCommandsUsePriorityHighest() && isValid(event)) {
            processEvent(event);
        }
    }

    public boolean isValid(final PlayerCommandPreprocessEvent event) {
        return configValues.isCommandsIgnoreCancelled()
                ? configValues.isCommandsSendCheckEnabled()
                : configValues.isCommandsSendCheckEnabled()
                && !event.isCancelled();
    }
    
    public void processEvent(final PlayerCommandPreprocessEvent event) {
        final Player player = event.getPlayer();
        if (player.hasPermission("newbieguard.bypass.commands") || db.commandsDatabaseHasPlayer(player)) {
            return;
        }

        final long time = timeCounter.count(player);
        if (time <= configValues.getNeedTimePlayedToUseCommands()) {
            final String sentCommand = event.getMessage();
            for (String blockedCommand : configValues.getBlockedCommands()) {
                if (mode.check(sentCommand, blockedCommand)) {
                    event.setCancelled(true);

                    final long leftTime = configValues.getNeedTimePlayedToUseCommands() - time;
                    send(player, leftTime);
                }
            }
        } else {
            db.addPlayerCommandsDatabase(player);
        }
    }

    private void send(Player player, long time) {
        final String formattedTime = timeFormatter.getTime(time);
        player.sendMessage(configValues.getCommandUseDenyMessages().replace("%time%", formattedTime));

        if (!configValues.isCommandUseDenyTitleEnabled()) {
            final Title title = Title.title(
                    Component.text(configValues.getCommandUseDenyTitle().replace("%time%", formattedTime)),
                    Component.text(configValues.getCommandUseDenySubTitle().replace("%time%", formattedTime)),
                    configValues.getCommandUseTitleTimes());
            player.showTitle(title);
        }

        if (!configValues.isCommandUseDenySoundEnabled()) {
            player.playSound(player.getLocation(),
                    configValues.getCommandUseDenySound(),
                    configValues.getCommandUseSoundVolume(),
                    configValues.getCommandUseSoundPitch());
        }
    }

    public static void setTimeCounter(NewbieGuard plugin) {
        timeCounter = plugin.getConfig().getBoolean("settings.commands-use.count-time-from-first-join", false)
                ? new FirstEntryCounter()
                : new OnlineCounter();
    }

    public static void setMode(NewbieGuard plugin) {
        mode = plugin.getConfig().getBoolean("settings.commands-use.use-whitelist", false)
                ? new WhiteList()
                : new BlackList();
    }
}
