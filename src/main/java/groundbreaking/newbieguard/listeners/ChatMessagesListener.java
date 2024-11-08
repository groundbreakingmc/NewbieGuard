package groundbreaking.newbieguard.listeners;

import groundbreaking.newbieguard.NewbieGuard;
import groundbreaking.newbieguard.database.DatabaseHandler;
import groundbreaking.newbieguard.utils.PlaceholdersUtil;
import groundbreaking.newbieguard.utils.TimeFormatter;
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
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;

public final class ChatMessagesListener implements Listener {

    private final NewbieGuard plugin;
    private final ConfigValues configValues;
    private final DatabaseHandler database;

    private boolean isRegistered = false;

    public static final List<String> MESSAGES = new ObjectArrayList<>();
    private final TimeFormatter timeFormatter = new TimeFormatter();
    private static ITimeCounter timeCounter;

    public ChatMessagesListener(final NewbieGuard plugin) {
        this.plugin = plugin;
        this.configValues = plugin.getConfigValues();
        this.database = plugin.getDatabaseHandler();

        setTimeCounter(plugin);
    }

    @EventHandler
    public void onEvent(final AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        if (player.hasPermission("newbieguard.bypass.chat") || !MESSAGES.contains(player.getName())) {
            return;
        }
        
        final long playedTime = timeCounter.count(player);
        final long requiredTime = this.configValues.getNeedTimePlayedToSendMessages();
        if (playedTime <= requiredTime) {
            event.setCancelled(true);
            final long leftTime = requiredTime - playedTime;
            this.send(player, leftTime);
        } else {
            MESSAGES.remove(player.getName());
            this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () ->
                this.database.addPlayerToChatTable(player.getName())
            );
        }
    }

    private void send(final Player player, final long time) {
        final String formattedTime = this.timeFormatter.getTime(time);

        final String message = this.configValues.getMessageSendCooldownMessage();
        if (!message.isEmpty()) {
            final String formattedMessage = PlaceholdersUtil.parse(player, message.replace("%time%", formattedTime));
            player.sendMessage(formattedMessage);
        }

        if (!this.configValues.isMessageSendDenyTitleEnabled()) {
            final Component titleText = Component.text(this.configValues.getMessageSendDenyTitle().replace("%time%", formattedTime));
            final Component subtitleText = Component.text(this.configValues.getMessageSendDenySubtitle().replace("%time%", formattedTime));
            final Title.Times titleTimes = this.configValues.getMessageSendTitleTimes();

            final Title title = Title.title(titleText, subtitleText, titleTimes);
            player.showTitle(title);
        }

        if (!this.configValues.isMessageSendDenySoundEnabled()) {
            final Location playerLocation = player.getLocation();
            final Sound sound = this.configValues.getMessageSendDenySound();
            final float volume = this.configValues.getMessageSendSoundVolume();
            final float pitch = this.configValues.getMessageSendSoundPitch();

            player.playSound(playerLocation, sound, volume, pitch);
        }
    }

    public static void setTimeCounter(final NewbieGuard plugin) {
        final boolean countFromFirstJoin = plugin.getConfig().getBoolean("settings.messages-send.count-time-from-first-join");
        timeCounter = countFromFirstJoin ? new FirstEntryCounter() : new OnlineCounter();
    }
}
