package groundbreaking.newbieguard.listeners.messages;

import groundbreaking.newbieguard.NewbieGuard;
import groundbreaking.newbieguard.utils.PermissionUtil;
import groundbreaking.newbieguard.utils.PlaceholdersUtil;
import groundbreaking.newbieguard.utils.TimeFormatterUtil;
import groundbreaking.newbieguard.utils.config.ConfigValues;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class ChatMessagesListener implements Listener {

    private final NewbieGuard plugin;
    private final ConfigValues configValues;

    public static final Map<UUID, Long> PLAYERS = new HashMap<>();

    private boolean isRegistered = false;

    public ChatMessagesListener(final NewbieGuard plugin) {
        this.plugin = plugin;
        this.configValues = plugin.getConfigValues();
    }

    @EventHandler
    public void onEvent(final AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        final UUID playerUUID = player.getUniqueId();
        if (player.hasPermission("newbieguard.bypass.messages")) {
            return;
        }

        final Long endTime = PLAYERS.get(playerUUID);
        if (endTime == null) {
            return;
        }

        final long currentTime = System.currentTimeMillis();

        final long leftTime = endTime - currentTime;

        if (leftTime > 0) {
            final long leftTimeSeconds = TimeUnit.MILLISECONDS.toSeconds(leftTime);
            event.setCancelled(true);
            this.send(player, leftTimeSeconds);
        } else {
            PLAYERS.remove(playerUUID);
            PermissionUtil.givePermission(playerUUID, "newbieguard.bypass.messages");
        }
    }

    private void send(final Player player, final long time) {
        final String formattedTime = TimeFormatterUtil.getTime(time);

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
}
