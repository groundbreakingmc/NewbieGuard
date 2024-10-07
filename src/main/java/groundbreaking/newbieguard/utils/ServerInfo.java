package groundbreaking.newbieguard.utils;

import groundbreaking.newbieguard.NewbieGuard;
import lombok.Getter;
import org.bukkit.Bukkit;

public final class ServerInfo {

    @Getter
    private final boolean
            isPapiExist = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null,
            isAbove16,
            isPaperOrFork = checkIsPaperOrFork();

    @Getter
    private final int subVersion;

    private final NewbieGuard plugin;

    public ServerInfo(NewbieGuard plugin) {
        this.plugin = plugin;
        this.subVersion = extractMainVersion();
        this.isAbove16 = subVersion >= 16;
    }

    public int extractMainVersion() {
        try {
            return Integer.parseInt(plugin.getServer().getMinecraftVersion().split("\\.", 3)[1]);
        } catch (NumberFormatException ex) {
            plugin.getLogger().warning("\u001b[32mFailed to extract server version. Plugin may not work correctly!");
            return 0;
        }
    }

    public boolean checkIsPaperOrFork() {
        try {
            Class.forName("com.destroystokyo.paper.utils.PaperPluginLogger");
            return true;
        } catch (ClassNotFoundException ignore) {
            return false;
        }
    }
}