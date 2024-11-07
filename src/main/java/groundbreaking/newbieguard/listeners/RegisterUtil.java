package groundbreaking.newbieguard.listeners;

import groundbreaking.newbieguard.NewbieGuard;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.lang.reflect.Field;

public class RegisterUtil {

    private RegisterUtil() {

    }

    public static void register(final NewbieGuard plugin, final Listener listener) {
        try {
            final Field registerField = listener.getClass().getDeclaredField("isRegistered");
            registerField.setAccessible(true);
            final boolean isRegistered = registerField.getBoolean(listener);
            if (!isRegistered) {
                plugin.getServer().getPluginManager().registerEvents(listener, plugin);
                registerField.set(listener, true);
            }
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void unregister(final Listener listener) {
        try {
            final Field registerField = listener.getClass().getDeclaredField("isRegistered");
            registerField.setAccessible(true);
            final boolean isRegistered = registerField.getBoolean(listener);
            if (isRegistered) {
                HandlerList.unregisterAll(listener);
                registerField.set(listener, false);
            }
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }
}
