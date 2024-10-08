package groundbreaking.newbieguard.utils;

import groundbreaking.newbieguard.NewbieGuard;

public final class ServerInfo {

    private final NewbieGuard plugin;

    public ServerInfo(NewbieGuard plugin) {
        this.plugin = plugin;
    }

    public int getSubVersion() {
        try {
            return Integer.parseInt(plugin.getServer().getMinecraftVersion().split("\\.", 3)[1]);
        } catch (NumberFormatException ex) {
            plugin.getLogger().warning("\u001b[32mFailed to extract server version. Plugin may not work correctly!");
            return 0;
        }
    }

    public boolean isPaperOrFork() {
        try {
            Class.forName("com.destroystokyo.paper.utils.PaperPluginLogger");
            return true;
        } catch (ClassNotFoundException ignore) {
            return false;
        }
    }
}