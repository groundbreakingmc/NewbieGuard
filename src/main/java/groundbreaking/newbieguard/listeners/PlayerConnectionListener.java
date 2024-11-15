package groundbreaking.newbieguard.listeners;

import groundbreaking.newbieguard.NewbieGuard;
import groundbreaking.newbieguard.database.DatabaseHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

public final class PlayerConnectionListener implements Listener {

    private final NewbieGuard plugin;
    private final DatabaseHandler database;

    public PlayerConnectionListener(final NewbieGuard plugin) {
        this.plugin = plugin;
        this.database = plugin.getDatabaseHandler();
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        final UUID playerUUID = event.getPlayer().getUniqueId();
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try(final Connection connection = this.database.getConnection()) {
                if (!this.database.containsPlayerInTable(connection, playerUUID, DatabaseHandler.CHECK_IF_PLAYER_IN_CHAT_TABLE)) {
                    ChatMessagesListener.MESSAGES.add(playerUUID);
                }
                if (!this.database.containsPlayerInTable(connection, playerUUID, DatabaseHandler.CHECK_IF_PLAYER_IN_COMMANDS_TABLE)) {
                    CommandsListeners.COMMANDS.add(playerUUID);
                }
            } catch(final SQLException ex) {
                ex.printStackTrace();
            }
        });
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        final UUID playerUUID = event.getPlayer().getUniqueId();
        ChatMessagesListener.MESSAGES.remove(playerUUID);
        CommandsListeners.COMMANDS.remove(playerUUID);
    }
}
