package com.github.groundbreakingmc.newbieguard.listeners.commands;

import com.github.groundbreakingmc.newbieguard.NewbieGuard;
import com.github.groundbreakingmc.newbieguard.constructors.CommandGroup;
import com.github.groundbreakingmc.newbieguard.utils.PermissionUtil;
import com.github.groundbreakingmc.newbieguard.utils.PlaceholdersUtil;
import com.github.groundbreakingmc.newbieguard.utils.TimeFormatterUtil;
import com.github.groundbreakingmc.newbieguard.utils.config.ConfigValues;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class CommandsListeners implements Listener {

    private final ConfigValues configValues;

    private boolean isRegistered = false;

    public CommandsListeners(final NewbieGuard plugin) {
        this.configValues = plugin.getConfigValues();
    }

    @EventHandler
    public void onEvent(final PlayerCommandPreprocessEvent event) {
        final Player player = event.getPlayer();
        final UUID playerUUID = player.getUniqueId();

        final String fullSentCommand = event.getMessage();
        final String sentCommand = fullSentCommand.substring(1, fullSentCommand.indexOf(' '));

        final CommandGroup commandGroup = this.configValues.getBlockedCommands().get(sentCommand);

        if (commandGroup == null
                || player.hasPermission("newbieguard.bypass.commands." + commandGroup.getSectionName())) {
            return;
        }

        final Long endTime = commandGroup.players.get(playerUUID);
        if (endTime == null || commandGroup.getMode().check(commandGroup)) {
            return;
        }

        final long currentTime = System.currentTimeMillis();

        final long leftTime = endTime - currentTime;

        if (leftTime > 0) {
            final long leftTimeSeconds = TimeUnit.MILLISECONDS.toSeconds(leftTime);
            event.setCancelled(true);
            this.send(player, commandGroup, leftTimeSeconds);
        } else {
            commandGroup.players.remove(playerUUID);
            PermissionUtil.givePermission(playerUUID, "newbieguard.bypass.messages");
        }
    }

    private void send(final Player player, final CommandGroup commandGroup, final long time) {
        final String formattedTime = TimeFormatterUtil.getTime(time);

        final String message = commandGroup.getCooldownMessage();
        if (!message.isEmpty()) {
            final String formattedMessage = PlaceholdersUtil.parse(player, message.replace("%time%", formattedTime));
            player.sendMessage(formattedMessage);
        }

        if (commandGroup.isDenyTitleEnabled()) {
            final TextReplacementConfig replacement = TextReplacementConfig.builder()
                    .matchLiteral("%time%")
                    .replacement(formattedTime)
                    .build();

            final Component titleText = commandGroup.getDenyTitle().replaceText(replacement);
            final Component subtitleText = commandGroup.getDenySubtitle().replaceText(replacement);
            final Title.Times titleTimes = commandGroup.getDenyTitleTimes();

            final Title title = Title.title(titleText, subtitleText, titleTimes);

            player.showTitle(title);
        }

        if (commandGroup.isDenySoundEnabled()) {
            final Location playerLocation = player.getLocation();
            final Sound sound = commandGroup.getDenySound();
            final float volume = commandGroup.getDenySoundVolume();
            final float pitch = commandGroup.getDenySoundPitch();

            player.playSound(playerLocation, sound, volume, pitch);
        }
    }
}
