package groundbreaking.newbieguard;

import groundbreaking.newbieguard.database.DatabaseHandler;
import groundbreaking.newbieguard.listeners.ChatMessagesListener;
import groundbreaking.newbieguard.listeners.ColumnCommandsListener;
import groundbreaking.newbieguard.listeners.CommandsListeners;
import groundbreaking.newbieguard.listeners.UpdatesNotify;
import groundbreaking.newbieguard.utils.PlaceholdersUtil;
import groundbreaking.newbieguard.utils.ServerInfo;
import groundbreaking.newbieguard.utils.UpdatesChecker;
import groundbreaking.newbieguard.utils.config.ConfigValues;
import groundbreaking.newbieguard.utils.logging.BukkitLogger;
import groundbreaking.newbieguard.utils.logging.ILogger;
import groundbreaking.newbieguard.utils.logging.PaperLogger;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.logging.Logger;

@Getter
public final class NewbieGuard extends JavaPlugin {

    @Setter private DatabaseHandler databaseHandler = null;

    private ConfigValues configValues;

    private ILogger myLogger;

    private ChatMessagesListener chatListener;
    private CommandsListeners commandsListener;
    private ColumnCommandsListener columnCommandsListener;

    @Override
    public void onEnable() {
        final long startTime = System.currentTimeMillis();

        final ServerInfo serverInfo = new ServerInfo();
        if (!serverInfo.isPaperOrFork()) {
            this.logPaperWarning();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        this.setupLogger(serverInfo);
        this.logLoggerType();

        this.configValues = new ConfigValues(this);
        this.configValues.setValues();

        this.setupConnection();

        this.setupCommand();

        final Server server = super.getServer();

        final UpdatesChecker updatesChecker = new UpdatesChecker(this);
        server.getScheduler().runTaskAsynchronously(this, updatesChecker::startCheck);

        final PluginManager pluginManager = server.getPluginManager();
        pluginManager.registerEvents(new UpdatesNotify(this), this);

        this.loadClassesAndEvents();

        this.myLogger.info("Plugin was successfully started in: " + (System.currentTimeMillis() - startTime) + "ms.");
    }

    @Override
    public void onDisable() {
        this.databaseHandler.close();
    }

    private void logPaperWarning() {
        final Logger logger = super.getLogger();
        logger.warning("\u001b[91m=============== \u001b[31mWARNING \u001b[91m===============\u001b[0m");
        logger.warning("\u001b[91mThe plugin author against using Bukkit, Spigot etc.!\u001b[0m");
        logger.warning("\u001b[91mMove to Paper or his forks. To download Paper visit:\u001b[0m");
        logger.warning("\u001b[91mhttps://papermc.io/downloads/all\u001b[0m");
        logger.warning("\u001b[91m=======================================\u001b[0m");
    }

    private void logLoggerType() {
        if (this.myLogger instanceof PaperLogger) {
            this.myLogger.info("Plugin will use new ComponentLogger for logging.");
        } else {
            this.myLogger.info("Plugin will use default old BukkitLogger for logging. Because your server version is under 19!");
        }
    }

    public void setupConnection() {
        try {
            this.databaseHandler.createConnection();
        } catch (final SQLException ex) {
            this.myLogger.warning("An error coursed while trying to open database connection.");
            ex.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    public void setupLogger(final ServerInfo serverInfo) {
        this.myLogger = serverInfo.getSubVersion(this) >= 19
                ? new PaperLogger(this)
                : new BukkitLogger(this);
    }

    public void setupCommand() {
        super.getCommand("newbieguard").setExecutor((sender, command, label, args) -> {
            final long reloadStartTime = System.currentTimeMillis();

            if (!sender.hasPermission("newbieguard.reload")) {
                final String message = this.configValues.getNoPermMessages();
                final String formattedMessage = PlaceholdersUtil.parse(sender, message);
                sender.sendMessage(formattedMessage);
                return true;
            }

            this.databaseHandler.close();
            this.reload();

            final long reloadFinishTime = System.currentTimeMillis();
            final String timeLeft = String.valueOf(reloadFinishTime - reloadStartTime);
            final String message = this.configValues.getReloadMessages().replace("%time%", timeLeft);
            final String formattedMessage = PlaceholdersUtil.parse(sender, message);
            sender.sendMessage(formattedMessage);

            return true;
        });
    }

    public void loadClassesAndEvents() {
        chatListener = new ChatMessagesListener(this);
        commandsListener = new CommandsListeners(this);
        columnCommandsListener = new ColumnCommandsListener(this);
    }

    public void reload() {
        this.configValues.setValues();
        ChatMessagesListener.setTimeCounter(this);
        CommandsListeners.setTimeCounter(this);
        CommandsListeners.setMode(this);
    }

    public EventPriority getEventPriority(final String priority) {
        return switch (priority) {
            case "LOWEST" -> EventPriority.LOWEST;
            case "LOW" -> EventPriority.LOW;
            case "NORMAL" -> EventPriority.NORMAL;
            case "HIGH" -> EventPriority.HIGH;
            case "HIGHEST" -> EventPriority.HIGHEST;
            default -> {
                this.myLogger.warning("Failed to parse value from \"settings.listener-priority\" from config file. Please check your configuration file, or delete it and restart your server!");
                this.myLogger.warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
                throw new RuntimeException("Failed to get event priority, please check your configuration files!");
            }
        };
    }
}