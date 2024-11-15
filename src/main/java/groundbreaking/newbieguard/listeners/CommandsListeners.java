package groundbreaking.newbieguard.listeners;

import groundbreaking.newbieguard.NewbieGuard;
import groundbreaking.newbieguard.database.DatabaseHandler;
import groundbreaking.newbieguard.utils.PlaceholdersUtil;
import groundbreaking.newbieguard.utils.TimeFormatterUtil;
import groundbreaking.newbieguard.utils.commands.BlackList;
import groundbreaking.newbieguard.utils.commands.IMode;
import groundbreaking.newbieguard.utils.commands.WhiteList;
import groundbreaking.newbieguard.utils.config.ConfigValues;
import groundbreaking.newbieguard.utils.time.FirstEntryCounter;
import groundbreaking.newbieguard.utils.time.ITimeCounter;
import groundbreaking.newbieguard.utils.time.OnlineCounter;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;
import java.util.UUID;

public final class CommandsListeners implements Listener {

    private final NewbieGuard plugin;
    private final ConfigValues configValues;
    private final DatabaseHandler database;

    public static final List<UUID> COMMANDS = new ObjectArrayList<>();

    private ITimeCounter timeCounter;
    private IMode mode;

    private boolean isRegistered = false;

    public CommandsListeners(final NewbieGuard plugin) {
        this.plugin = plugin;
        this.configValues = plugin.getConfigValues();
        this.database = plugin.getDatabaseHandler();
    }

    @EventHandler
    public void onEvent(final PlayerCommandPreprocessEvent event) {
        final Player player = event.getPlayer();
        if (player.hasPermission("newbieguard.bypass.commands") || !COMMANDS.contains(player.getUniqueId())) {
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
            COMMANDS.remove(player.getUniqueId());
            this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () ->
                    this.database.addPlayerToCommandsDatabase(player.getUniqueId())
            );
        }
    }

    private void send(final Player player, final long time) {
        final String formattedTime = TimeFormatterUtil.getTime(time);

        final String message = configValues.getCommandUseCooldownMessage();
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

    public void setTimeCounter(final boolean countFromFirstJoin) {
        this.timeCounter = countFromFirstJoin ? new FirstEntryCounter() : new OnlineCounter();
    }

    public void setMode(final boolean useWhiteList) {
        this.mode = useWhiteList ? new WhiteList() : new BlackList();
    }
}
