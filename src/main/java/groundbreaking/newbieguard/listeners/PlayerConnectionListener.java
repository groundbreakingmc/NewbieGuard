package groundbreaking.newbieguard.listeners;

import groundbreaking.newbieguard.NewbieGuard;
import groundbreaking.newbieguard.database.DatabaseHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

public final class PlayerConnectionListener implements Listener {

    private final NewbieGuard plugin;

    public PlayerConnectionListener(final NewbieGuard plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(final PlayerJoinEvent event) {
        final UUID playerUUID = event.getPlayer().getUniqueId();

        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {

            final DatabaseHandler databaseHandler = this.plugin.getDatabaseHandler();

            try (final Connection connection = databaseHandler.getConnection()) {
                if (!databaseHandler.containsPlayerInTable(connection, playerUUID, DatabaseHandler.CHECK_IF_PLAYER_IN_CHAT_TABLE)) {
                    ChatMessagesListener.MESSAGES.add(playerUUID);
                }
                if (!databaseHandler.containsPlayerInTable(connection, playerUUID, DatabaseHandler.CHECK_IF_PLAYER_IN_COMMANDS_TABLE)) {
                    CommandsListeners.COMMANDS.add(playerUUID);
                }
            } catch (final SQLException ex) {
                ex.printStackTrace();
            }
        });
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onKick(final PlayerKickEvent event) {
        this.unloadData(event);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(final PlayerQuitEvent event) {
        this.unloadData(event);
    }

    public void unloadData(final PlayerEvent event) {
        final UUID playerUUID = event.getPlayer().getUniqueId();
        ChatMessagesListener.MESSAGES.remove(playerUUID);
        CommandsListeners.COMMANDS.remove(playerUUID);
    }
}
