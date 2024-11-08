package groundbreaking.newbieguard.listeners;

import groundbreaking.newbieguard.NewbieGuard;
import groundbreaking.newbieguard.database.DatabaseHandler;
import groundbreaking.newbieguard.utils.PlaceholdersUtil;
import groundbreaking.newbieguard.utils.TimeFormatter;
import groundbreaking.newbieguard.utils.commands.BlackList;
import groundbreaking.newbieguard.utils.commands.IMode;
import groundbreaking.newbieguard.utils.commands.WhiteList;
import groundbreaking.newbieguard.utils.config.ConfigValues;
import groundbreaking.newbieguard.utils.time.FirstEntryCounter;
import groundbreaking.newbieguard.utils.time.ITimeCounter;
import groundbreaking.newbieguard.utils.time.OnlineCounter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

public final class CommandsListeners implements Listener {

    private final ConfigValues configValues;
    private final DatabaseHandler database;

    private boolean isRegistered = false;

    private final TimeFormatter timeFormatter = new TimeFormatter();
    private static ITimeCounter timeCounter;
    private static IMode mode;

    public CommandsListeners(final NewbieGuard plugin) {
        this.configValues = plugin.getConfigValues();
        this.database = plugin.getConnectionHandler();

        setTimeCounter(plugin);
        setMode(plugin);
    }

    @EventHandler
    public void onEvent(final PlayerCommandPreprocessEvent event) {
        final Player player = event.getPlayer();
        if (player.hasPermission("newbieguard.bypass.commands") || this.database.commandsDatabaseHasPlayer(player)) {
            return;
        }

        final long playedTime = timeCounter.count(player);
        final long requiredTime = this.configValues.getNeedTimePlayedToUseCommands();
        if (playedTime <= requiredTime) {
            final String sentCommand = event.getMessage();

            final List<String> blockedCommands = this.configValues.getBlockedCommands();
            for (int i = 0; i < blockedCommands.size(); i++) {
                final String blockedCommand = blockedCommands.get(i);
                if (mode.check(sentCommand, blockedCommand)) {
                    event.setCancelled(true);
                    final long leftTime = requiredTime - playedTime;
                    this.send(player, leftTime);
                }
            }
        } else {
            this.database.addPlayerCommandsDatabase(player);
        }
    }

    private void send(final Player player, final long time) {
        final String formattedTime = this.timeFormatter.getTime(time);

        final String message = configValues.getCommandUseCooldownMessages();
        if (!message.isEmpty()) {
            final String formattedMessage = PlaceholdersUtil.parse(player, message.replace("%time%", formattedTime));
            player.sendMessage(formattedMessage);
        }

        if (!this.configValues.isCommandUseDenyTitleEnabled()) {
            final Component titleText = Component.text(this.configValues.getCommandUseDenyTitle().replace("%time%", formattedTime));
            final Component subtitleText = Component.text(this.configValues.getCommandUseDenySubtitle().replace("%time%", formattedTime));
            final Title.Times titleTimes = this.configValues.getCommandUseTitleTimes();

            final Title title = Title.title(titleText, subtitleText, titleTimes);

            player.showTitle(title);
        }

        if (!this.configValues.isCommandUseDenySoundEnabled()) {
            final Location playerLocation = player.getLocation();
            final Sound sound = this.configValues.getCommandUseDenySound();
            final float volume = this.configValues.getCommandUseSoundVolume();
            final float pitch = this.configValues.getCommandUseSoundPitch();

            player.playSound(playerLocation, sound, volume, pitch);
        }
    }

    public static void setTimeCounter(final NewbieGuard plugin) {
        final boolean countFromFirstJoin = plugin.getConfig().getBoolean("settings.commands-use.count-time-from-first-join");
        timeCounter = countFromFirstJoin ? new FirstEntryCounter() : new OnlineCounter();
    }

    public static void setMode(final NewbieGuard plugin) {
        final boolean useWhiteList = plugin.getConfig().getBoolean("settings.commands-use.use-whitelist");
        mode = useWhiteList ? new WhiteList() : new BlackList();
    }
}
