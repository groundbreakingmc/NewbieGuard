package groundbreaking.newbieguard.listeners;

import groundbreaking.newbieguard.NewbieGuard;
import groundbreaking.newbieguard.database.DatabaseHandler;
import groundbreaking.newbieguard.utils.PlaceholdersUtil;
import groundbreaking.newbieguard.utils.TimeFormatter;
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
import org.bukkit.event.player.AsyncPlayerChatEvent;

public final class ChatMessagesListener implements Listener {

    private final ConfigValues configValues;
    private final DatabaseHandler database;

    private boolean isRegistered = false;

    private final TimeFormatter timeFormatter = new TimeFormatter();
    private static ITimeCounter timeCounter;

    public ChatMessagesListener(NewbieGuard plugin) {
        this.configValues = plugin.getConfigValues();
        this.database = plugin.getConnectionHandler();

        setTimeCounter(plugin);
    }

    @EventHandler
    public void onEvent(final AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        if (player.hasPermission("newbieguard.bypass.chat") || this.database.chatDatabaseHasPlayer(player)) {
            return;
        }
        
        final long playedTime = timeCounter.count(player);

        if (playedTime <= this.configValues.getNeedTimePlayedToSendMessages()) {
            event.setCancelled(true);
            final long requiredTime = this.configValues.getNeedTimePlayedToSendMessages();
            final long leftTime = requiredTime - playedTime;
            this.send(player, leftTime);
        } else {
            this.database.addPlayerChatDatabase(player);
        }
    }

    private void send(final Player player, final long time) {
        final String formattedTime = this.timeFormatter.getTime(time);

        final String message = this.configValues.getMessageSendCooldownMessages();
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
