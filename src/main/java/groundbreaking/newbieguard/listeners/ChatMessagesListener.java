package groundbreaking.newbieguard.listeners;

import groundbreaking.newbieguard.NewbieGuard;
import groundbreaking.newbieguard.database.AbstractDB;
import groundbreaking.newbieguard.utils.TimeFormatter;
import groundbreaking.newbieguard.utils.time.FirstEntryCounter;
import groundbreaking.newbieguard.utils.time.ITimeCounter;
import groundbreaking.newbieguard.utils.time.OnlineCounter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import groundbreaking.newbieguard.utils.config.ConfigValues;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public final class ChatMessagesListener implements Listener {

    private final NewbieGuard plugin;
    private final ConfigValues configValues;
    private final AbstractDB db;

    private final TimeFormatter timeFormatter = new TimeFormatter();
    private static ITimeCounter timeCounter;

    public ChatMessagesListener(NewbieGuard plugin) {
        this.plugin = plugin;
        this.configValues = plugin.getConfigValues();
        this.db = plugin.getConnectionHandler();
        setTimeCounter(plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMessageSendLowest(final AsyncPlayerChatEvent event) {
        if (configValues.isMessageSendPriorityLowest() && isValid(event)) {
            processEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onMessageSendLow(final AsyncPlayerChatEvent event) {
        if (configValues.isMessageSendPriorityLow() && isValid(event)) {
            processEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onMessageSendNormal(final AsyncPlayerChatEvent event) {
        if (configValues.isMessageSendPriorityNormal() && isValid(event)) {
            processEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMessageSendHigh(final AsyncPlayerChatEvent event) {
        if (configValues.isMessageSendPriorityHigh() && isValid(event)) {
            processEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMessageSendHighest(final AsyncPlayerChatEvent event) {
        if (configValues.isMessageSendPriorityHighest() && isValid(event)) {
            processEvent(event);
        }
    }

    public boolean isValid(final AsyncPlayerChatEvent event) {
        return configValues.isMessageSendIgnoreCancelled()
                ? configValues.isMessageSendCheckEnabled()
                : configValues.isMessageSendCheckEnabled()
                && !event.isCancelled();
    }

    public void processEvent(final AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        if (player.hasPermission("newbieguard.bypass.chat") || db.chatDatabaseHasPlayer(player)) {
            return;
        }
        
        final long time = timeCounter.count(player);

        if (time <= configValues.getNeedTimePlayedToSendMessages()) {
            event.setCancelled(true);

            final long leftTime = configValues.getNeedTimePlayedToSendMessages() - time;
            send(player, leftTime);
        }
        else {
            db.addPlayerChatDatabase(player);
        }
    }

    private void send(final Player player, final long time) {
        final String formattedTime = timeFormatter.getTime(time);
        player.sendMessage(configValues.getMessageSendDenyMessages().replace("%time%", formattedTime));

        if (!configValues.isMessageSendDenyTitleEnabled()) {
            final Title title = Title.title(
                    Component.text(configValues.getMessageSendDenyTitle().replace("%time%", formattedTime)),
                    Component.text(configValues.getMessageSendDenySubTitle().replace("%time%", formattedTime)),
                    configValues.getMessageSendTitleTimes());
            player.showTitle(title);
        }

        if (!configValues.isMessageSendDenySoundEnabled()) {
            player.playSound(player.getLocation(),
                    configValues.getMessageSendDenySound(),
                    configValues.getMessageSendSoundVolume(),
                    configValues.getMessageSendSoundPitch());
        }
    }
    
    public static void setTimeCounter(NewbieGuard plugin) {
        timeCounter = plugin.getConfig().getBoolean("settings.chat-use.count-time-from-first-join", false)
                ? new FirstEntryCounter()
                : new OnlineCounter();
    }
}
