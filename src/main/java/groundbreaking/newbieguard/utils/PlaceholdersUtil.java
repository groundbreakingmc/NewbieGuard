package groundbreaking.newbieguard.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class PlaceholdersUtil {

    private PlaceholdersUtil() {

    }

    public static String parse(final CommandSender sender, final String message) {
        if (sender instanceof Player player) {
            return PlaceholderAPI.setPlaceholders(player, message);
        } else {
            return PlaceholderAPI.setPlaceholders(null, message);
        }
    }
}
