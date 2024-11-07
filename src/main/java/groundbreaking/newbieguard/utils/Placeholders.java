package groundbreaking.newbieguard.utils;

import groundbreaking.newbieguard.NewbieGuard;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Placeholders {

    private final NewbieGuard plugin;

    public Placeholders(final NewbieGuard plugin) {
        this.plugin = plugin;
    }

    public String parse(final CommandSender sender, String message) {

        if (sender instanceof Player player) {
            message = PlaceholderAPI.setPlaceholders(player, message);
        } else {
            message = PlaceholderAPI.setPlaceholders(null, message);
        }

        return message;
    }
}
