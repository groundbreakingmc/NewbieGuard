package groundbreaking.newbieguard.listeners;

import groundbreaking.newbieguard.NewbieGuard;
import groundbreaking.newbieguard.utils.UpdatesChecker;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class UpdatesNotify implements Listener {

    private final NewbieGuard plugin;

    public UpdatesNotify(NewbieGuard plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onAdminJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        if ((player.isOp() || player.hasPermission("newbieguard.updates")) && UpdatesChecker.isNewVersion()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.sendMessage("");
                player.sendMessage("§c[NewbieGuard] §6New update is available to download!");
                player.sendMessage("§c[NewbieGuard] §fDownload link: " + UpdatesChecker.getDownloadLink());
                player.sendMessage("§c[NewbieGuard] §fCurrently version: " + UpdatesChecker.getCurrentVersion());
                player.sendMessage("§c[NewbieGuard] §fNewest version: " + UpdatesChecker.getLatestVersion());
                player.sendMessage("");
            }, 40L);
        }
    }
}
