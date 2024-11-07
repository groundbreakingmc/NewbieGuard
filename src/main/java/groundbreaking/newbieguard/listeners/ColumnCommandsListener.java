package groundbreaking.newbieguard.listeners;

import groundbreaking.newbieguard.NewbieGuard;
import groundbreaking.newbieguard.utils.Placeholders;
import groundbreaking.newbieguard.utils.config.ConfigValues;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;


public final class ColumnCommandsListener implements Listener {

    private final NewbieGuard plugin;
    private final ConfigValues configValues;
    private final Placeholders placeholders;

    private boolean isRegistered = false;

    public ColumnCommandsListener(NewbieGuard plugin) {
        this.plugin = plugin;
        this.configValues = plugin.getConfigValues();
        this.placeholders = plugin.getPlaceholders();

        this.registerEvent();
    }

    @EventHandler
    public void onCommandSend(final PlayerCommandPreprocessEvent event) {
        this.processEvent(event);
    }

    public void registerEvent() {
        if (this.isRegistered) {
            return;
        }

        final Class<? extends Event> eventClass = PlayerCommandPreprocessEvent.class;

        final String priorityString = this.configValues.getColumnCommandsUseListenerPriority();
        final EventPriority eventPriority = this.plugin.getEventPriority(priorityString);

        final boolean ignoreCanceled = this.configValues.isColumnCommandsUseIgnoreCancelled();

        this.plugin.getServer().getPluginManager().registerEvent(eventClass, this, eventPriority, (listener, event) -> {

            if (event instanceof PlayerCommandPreprocessEvent commandPreprocessEvent) {
                this.onCommandSend(commandPreprocessEvent);
            }

        }, this.plugin, ignoreCanceled);

        this.isRegistered = true;
    }

    public void unregisterEvent() {
        if (!this.isRegistered) {
            return;
        }

        HandlerList.unregisterAll(this);
        this.isRegistered = false;
    }

    private void processEvent(final PlayerCommandPreprocessEvent event) {
        final Player player = event.getPlayer();
        if (player.hasPermission("newbieguard.bypass.columncommands")) {
            return;
        }

        final String sentCommand = event.getMessage();
        if (this.isBlocked(sentCommand)) {
            event.setCancelled(true);

            this.send(player);
        }
    }

    private boolean isBlocked(final String sentCommand) {
        final char[] chars = sentCommand.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char currentChar = chars[i];
            if (currentChar == ' ') {
                return false;
            } else if (currentChar == ':') {
                return true;
            }
        }

        return false;
    }

    private void send(final Player player) {

        final String message = this.configValues.getColumnCommandUseDenyMessages();
        if (!message.isEmpty()) {
            final String formattedMessage = placeholders.parse(player, message);
            player.sendMessage(formattedMessage);
        }

        if (!this.configValues.isColumnCommandUseDenyTitleEnabled()) {
            final Title title = this.configValues.getColumnCommandUseDenyTitle();
            player.showTitle(title);
        }

        if (!this.configValues.isColumnCommandUseDenySoundEnabled()) {

            final Location playerLocation = player.getLocation();
            final Sound sound = this.configValues.getColumnCommandUseDenySound();
            final float volume = this.configValues.getColumnCommandUseSoundVolume();
            final float pitch = this.configValues.getColumnCommandUseSoundPitch();

            player.playSound(playerLocation, sound, volume, pitch);
        }
    }
}
