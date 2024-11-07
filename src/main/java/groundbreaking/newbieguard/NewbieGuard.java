package groundbreaking.newbieguard;

import groundbreaking.newbieguard.database.AbstractDB;
import groundbreaking.newbieguard.database.types.MariaDB;
import groundbreaking.newbieguard.database.types.SQLite;
import groundbreaking.newbieguard.listeners.ChatMessagesListener;
import groundbreaking.newbieguard.listeners.ColumnCommandsListener;
import groundbreaking.newbieguard.listeners.CommandsListeners;
import groundbreaking.newbieguard.listeners.UpdatesNotify;
import groundbreaking.newbieguard.utils.Placeholders;
import groundbreaking.newbieguard.utils.ServerInfo;
import groundbreaking.newbieguard.utils.UpdatesChecker;
import groundbreaking.newbieguard.utils.config.ConfigLoader;
import groundbreaking.newbieguard.utils.config.ConfigValues;
import groundbreaking.newbieguard.utils.logging.BukkitLogger;
import groundbreaking.newbieguard.utils.logging.ILogger;
import groundbreaking.newbieguard.utils.logging.PaperLogger;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;

@Getter
public final class NewbieGuard extends JavaPlugin {

    private AbstractDB connectionHandler = null;

    private ConfigValues configValues;

    private ILogger myLogger;

    @Getter(AccessLevel.NONE)
    private FileConfiguration config;

    private ChatMessagesListener chatListener;
    private CommandsListeners commandsListener;
    private ColumnCommandsListener columnCommandsListener;
    private Placeholders placeholders;

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

        this.config = new ConfigLoader(this).loadAndGet("config", 1.0);
        this.configValues = new ConfigValues(this);
        this.configValues.setValues();

        this.setupDatabaseHandler();
        this.setupConnection();

        this.setupCommand();

        final Server server = super.getServer();

        final UpdatesChecker updatesChecker = new UpdatesChecker(this);
        server.getScheduler().runTaskAsynchronously(this, updatesChecker::startCheck);

        final PluginManager pluginManager = server.getPluginManager();
        pluginManager.registerEvents(new UpdatesNotify(this), this);

        loadClassesAndEvents();

        this.myLogger.info("Plugin was successfully started in: " + (System.currentTimeMillis() - startTime) + "ms.");
    }

    @Override
    public void onDisable() {
        this.connectionHandler.close();
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

    private void setupDatabaseHandler() {
        final String dbType = this.config.getString("settings.database.type");

        if (dbType.equalsIgnoreCase("sqlite")) {

            final File dbFile = new File(getDataFolder() + File.separator + "database.db");
            this.loadDatabaseFile(dbFile);

            final String url = "jdbc:sqlite:" + dbFile;
            this.connectionHandler = new SQLite(url);

        } else if (dbType.equalsIgnoreCase("mariadb")) {

            final String host = this.config.getString("settings.database.maria-db.host");
            final String port = this.config.getString("settings.database.maria-db.port");
            final String dbName = this.config.getString("settings.database.maria-db.database-name");
            final String url = host + ":" + port + "/" + dbName;
            final String user = this.config.getString("settings.database.maria-db.username");
            final String pass = this.config.getString("settings.database.maria-db.password");

            this.connectionHandler = new MariaDB(url, user, pass);

        } else {
            throw new UnsupportedOperationException("Please choose SQLite or MariaDB as database!");
        }
    }

    public void setupConnection() {
        try {
            this.connectionHandler.createConnection();
        } catch (final SQLException ex) {
            this.myLogger.warning("An error coursed while trying to open database connection.");
            ex.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    public void loadDatabaseFile(final File dbFile) {
        if (!dbFile.exists()) {
            try {
                if (!dbFile.createNewFile()) {
                    this.myLogger.warning("Database file wasn't created. Plugin may work not correctly!");
                    this.myLogger.warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
                }
            } catch (final IOException ex) {
                ex.printStackTrace();
            }
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
                final String formattedMessage = this.placeholders.parse(sender, message);
                sender.sendMessage(formattedMessage);
                return true;
            }

            this.connectionHandler.close();
            this.reload();

            this.reloadEvents();

            final long reloadFinishTime = System.currentTimeMillis();
            final String timeLeft = String.valueOf(reloadFinishTime - reloadStartTime);
            final String message = this.configValues.getReloadMessages().replace("%time%", timeLeft);
            final String formattedMessage = this.placeholders.parse(sender, message);
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
        this.config = new ConfigLoader(this).loadAndGet("config", 1.0);
        this.configValues.setValues();
        this.setupDatabaseHandler();
        this.reloadEvents();
        ChatMessagesListener.setTimeCounter(this);
        CommandsListeners.setTimeCounter(this);
        CommandsListeners.setMode(this);
    }

    public void reloadEvents() {
        chatListener.unregisterEvent();
        chatListener.registerEvent();

        commandsListener.unregisterEvent();
        commandsListener.registerEvent();

        columnCommandsListener.unregisterEvent();
        columnCommandsListener.registerEvent();
    }

    @Override
    public @NotNull FileConfiguration getConfig() {
        return this.config;
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