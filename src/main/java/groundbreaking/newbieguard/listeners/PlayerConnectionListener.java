package groundbreaking.newbieguard.listeners;

import groundbreaking.newbieguard.NewbieGuard;
import groundbreaking.newbieguard.database.DatabaseHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {

    private final NewbieGuard plugin;
    private final DatabaseHandler database;

    public PlayerConnectionListener(final NewbieGuard plugin) {
        this.plugin = plugin;
        this.database = plugin.getDatabaseHandler();
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        final String playerName = event.getPlayer().getName();
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
           if (!this.database.chatDatabaseHasPlayer(playerName)) {
               ChatMessagesListener.MESSAGES.add(playerName);
           }
           if (!this.database.commandsDatabaseHasPlayer(playerName)) {
               CommandsListeners.COMMANDS.add(playerName);
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
