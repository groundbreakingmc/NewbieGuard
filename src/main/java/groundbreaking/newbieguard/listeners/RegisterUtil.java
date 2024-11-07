package groundbreaking.newbieguard.listeners;

import groundbreaking.newbieguard.NewbieGuard;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;

import java.lang.reflect.Field;

public class RegisterUtil {

    private RegisterUtil() {

    }

    public static void register(final NewbieGuard plugin, final Listener listener, final Class<? extends Event> eventClass, final EventPriority eventPriority, final boolean ignoreCancelled, final EventExecutor eventExecutor) {
        try {
            final Field registerField = listener.getClass().getDeclaredField("isRegistered");
            registerField.setAccessible(true);
            final boolean isRegistered = registerField.getBoolean(listener);
            if (!isRegistered) {
                plugin.getServer().getPluginManager().registerEvent(eventClass, listener, eventPriority, eventExecutor, plugin, ignoreCancelled);
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
