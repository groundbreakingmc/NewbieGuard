package groundbreaking.newbieguard.listeners;

import groundbreaking.newbieguard.NewbieGuard;
import groundbreaking.newbieguard.utils.UpdatesChecker;
import groundbreaking.newbieguard.utils.colorizer.IColorizer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class UpdatesNotify implements Listener {

    private final NewbieGuard plugin;
    private final UpdatesChecker updates;
    private final IColorizer colorizer;

    public UpdatesNotify(NewbieGuard plugin, UpdatesChecker updates) {
        this.plugin = plugin;
        this.updates = updates;
        this.colorizer = plugin.getColorizer();
    }

    @EventHandler(ignoreCancelled = true)
    public void onAdminJoin(PlayerJoinEvent e) {
        final Player player = e.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if ((player.isOp() || player.hasPermission("newbieguard.updates")) && updates.getNew_version()) {
                player.sendMessage("");
                player.sendMessage(colorizer.colorize("&c[NewbieGuard] &6New update is available to download!"));
                player.sendMessage(colorizer.colorize("&c[NewbieGuard] &fDownload link: " + updates.getDownloadLink()));
                player.sendMessage(colorizer.colorize("&c[NewbieGuard] &fCurrently version: " + updates.getCurrentVersion()));
                player.sendMessage(colorizer.colorize("&c[NewbieGuard] &fNewest version: " + updates.getLatestVersion()));
                player.sendMessage("");
            }
        }, 40L);
    }
}
