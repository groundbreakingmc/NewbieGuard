package groundbreaking.newbieguard.listeners;

import groundbreaking.newbieguard.NewbieGuard;
import groundbreaking.newbieguard.database.DatabaseHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

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
           if (!this.database.chatDatabaseHasPlayer(playerUUID)) {
               ChatMessagesListener.MESSAGES.add(playerUUID);
           }
           if (!this.database.commandsDatabaseHasPlayer(playerUUID)) {
               CommandsListeners.COMMANDS.add(playerUUID);
           }
        });
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        final String playerName = event.getPlayer().getName();
        ChatMessagesListener.MESSAGES.remove(playerName);
        CommandsListeners.COMMANDS.remove(playerName);
    }
}
