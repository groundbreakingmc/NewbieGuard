package groundbreaking.newbieguard.listeners;

import groundbreaking.newbieguard.NewbieGuard;
import groundbreaking.newbieguard.utils.config.ConfigValues;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;


public final class ColumnCommandsListener implements Listener {

    private final NewbieGuard plugin;
    private final ConfigValues configValues;

    public ColumnCommandsListener(NewbieGuard plugin) {
        this.plugin = plugin;
        this.configValues = plugin.getConfigValues();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommandSendLowest(final PlayerCommandPreprocessEvent event) {
        if (configValues.isColumnCommandsUsePriorityLowest() && isValid(event)) {
            processEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onCommandSendLow(final PlayerCommandPreprocessEvent event) {
        if (configValues.isColumnCommandsUsePriorityLow() && isValid(event)) {
            processEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCommandSendNormal(final PlayerCommandPreprocessEvent event) {
        if (configValues.isColumnCommandsUsePriorityNormal() && isValid(event)) {
            processEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCommandSendHigh(final PlayerCommandPreprocessEvent event) {
        if (configValues.isColumnCommandsUsePriorityHigh() && isValid(event)) {
            processEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommandSendHighest(final PlayerCommandPreprocessEvent event) {
        if (configValues.isColumnCommandsUsePriorityHighest() && isValid(event)) {
            processEvent(event);
        }
    }

    private boolean isValid(final PlayerCommandPreprocessEvent event) {
        return configValues.isColumnCommandsIgnoreCancelled()
                ? configValues.isColumnCommandsSendCheckEnabled()
                : configValues.isColumnCommandsSendCheckEnabled()
                && !event.isCancelled();
    }

    private void processEvent(final PlayerCommandPreprocessEvent event) {
        final Player player = event.getPlayer();
        if (player.hasPermission("newbieguard.bypass.columncommands")) {
            return;
        }

        final String sentCommand = event.getMessage();
        if (isBlocked(sentCommand)) {
            event.setCancelled(true);
            send(player);
        }
    }

    private boolean isBlocked(final String sentCommand) {
        final char[] chars = sentCommand.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char currentChar = chars[i];
            if (currentChar == ' ') {
                return false;
            }
            else if (currentChar == ':') {
                return true;
            }
        }
        return false;
    }

    private void send(final Player player) {
        player.sendMessage(configValues.getCommandUseDenyMessages());

        if (!configValues.isColumnCommandUseDenyTitleEnabled()) {
            player.showTitle(configValues.getColumnCommandUseTitle());
        }

        if (!configValues.isColumnCommandUseDenySoundEnabled()) {
            player.playSound(player.getLocation(),
                    configValues.getColumnCommandUseDenySound(),
                    configValues.getColumnCommandUseSoundVolume(),
                    configValues.getColumnCommandUseSoundPitch());
        }
    }
}
