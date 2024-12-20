package com.github.groundbreakingmc.newbieguard.listeners.connection;

import com.github.groundbreakingmc.newbieguard.NewbieGuard;
import com.github.groundbreakingmc.newbieguard.listeners.messages.ChatMessagesListener;
import com.github.groundbreakingmc.newbieguard.utils.PermissionUtil;
import com.github.groundbreakingmc.newbieguard.utils.config.ConfigValues;
import com.github.groundbreakingmc.newbieguard.utils.time.ITimeCounter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class PlayerJoinListener implements Listener {

    private final ConfigValues configValues;

    public PlayerJoinListener(final NewbieGuard plugin) {
        this.configValues = plugin.getConfigValues();
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final UUID playerUUID = player.getUniqueId();

        this.processMessagesSend(player, playerUUID);
        this.processCommands(player, playerUUID);
    }

    private void processMessagesSend(final Player player, final UUID playerUUID) {
        if (player.hasPermission("newbie.bypass.messages")) {
            return;
        }

        final ITimeCounter timeCounter = this.configValues.getMessagesSendTimeCounter();

        final long playedTime = timeCounter.count(player);
        final long requiredTime = this.configValues.getRequiredTimeToSendMessages();

        if (playedTime <= requiredTime) {
            final long leftTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(requiredTime - playedTime);
            ChatMessagesListener.PLAYERS.put(playerUUID, leftTime);
        }
    }

    private void processCommands(final Player player, final UUID playerUUID) {
        this.configValues.getBlockedCommands().forEach((group, commandGroup) -> {
            if (!player.hasPermission(commandGroup.bypassPermission)) {
                final long playedTime = commandGroup.timeCounter.count(player);
                final long requiredTime = commandGroup.requiredTime;

                if (playedTime <= requiredTime) {
                    final long leftTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(requiredTime - playedTime);
                    commandGroup.players.put(playerUUID, leftTime);
                } else {
                    PermissionUtil.givePermission(playerUUID, commandGroup.bypassPermission);
                }
            }
        });
    }
}
