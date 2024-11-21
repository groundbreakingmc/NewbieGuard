package com.github.groundbreakingmc.newbieguard.listeners.commands;

import com.github.groundbreakingmc.newbieguard.NewbieGuard;
import com.github.groundbreakingmc.newbieguard.utils.PlaceholdersUtil;
import com.github.groundbreakingmc.newbieguard.utils.config.ConfigValues;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public final class ColonCommandsListener implements Listener {

    private final ConfigValues configValues;

    private boolean isRegistered = false;

    public ColonCommandsListener(final NewbieGuard plugin) {
        this.configValues = plugin.getConfigValues();
    }

    @EventHandler
    public void onEvent(final PlayerCommandPreprocessEvent event) {
        final Player player = event.getPlayer();
        if (player.hasPermission("newbieguard.bypass.coloncommands")) {
            return;
        }

        final String sentCommand = event.getMessage();
        if (this.isBlocked(sentCommand)) {
            event.setCancelled(true);
            this.send(player);
        }
    }

    private boolean isBlocked(final String sentCommand) {
        final int spaceIndex = sentCommand.indexOf(' ');
        final int colonIndex = sentCommand.indexOf(':');
        return colonIndex != -1 && spaceIndex > colonIndex || spaceIndex == -1 && colonIndex != -1;
    }

    private void send(final Player player) {
        final String message = this.configValues.getColonCommandUseDenyMessage();
        if (!message.isEmpty()) {
            final String formattedMessage = PlaceholdersUtil.parse(player, message);
            player.sendMessage(formattedMessage);
        }

        if (this.configValues.isColonCommandUseDenyTitleEnabled()) {
            final Title title = this.configValues.getColonCommandUseDenyTitle();
            player.showTitle(title);
        }

        if (this.configValues.isColonCommandUseDenySoundEnabled()) {
            final Location playerLocation = player.getLocation();
            final Sound sound = this.configValues.getColonCommandUseDenySound();
            final float volume = this.configValues.getColonCommandUseSoundVolume();
            final float pitch = this.configValues.getColonCommandUseSoundPitch();

            player.playSound(playerLocation, sound, volume, pitch);
        }
    }
}
