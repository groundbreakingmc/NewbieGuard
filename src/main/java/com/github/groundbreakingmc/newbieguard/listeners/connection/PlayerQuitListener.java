package com.github.groundbreakingmc.newbieguard.listeners.connection;

import com.github.groundbreakingmc.newbieguard.NewbieGuard;
import com.github.groundbreakingmc.newbieguard.listeners.messages.ChatMessagesListener;
import com.github.groundbreakingmc.newbieguard.utils.config.ConfigValues;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerQuitListener {

    private final ConfigValues configValues;

    public PlayerQuitListener(final NewbieGuard plugin) {
        this.configValues = plugin.getConfigValues();
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {

        final UUID playerUUID = event.getPlayer().getUniqueId();

        ChatMessagesListener.PLAYERS.remove(playerUUID);

        this.configValues.getBlockedCommands().forEach((group, commandGroup) ->
                commandGroup.players.remove(playerUUID)
        );
    }
}
