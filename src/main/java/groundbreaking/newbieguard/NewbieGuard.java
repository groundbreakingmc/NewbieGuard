package groundbreaking.newbieguard;

import groundbreaking.newbieguard.database.AbstractDB;
import groundbreaking.newbieguard.database.types.MariaDB;
import groundbreaking.newbieguard.database.types.SQLite;
import groundbreaking.newbieguard.listeners.ChatMessagesListener;
import groundbreaking.newbieguard.listeners.ColumnCommandsListener;
import groundbreaking.newbieguard.listeners.CommandsListeners;
import groundbreaking.newbieguard.listeners.UpdatesNotify;
import groundbreaking.newbieguard.utils.ServerInfo;
import groundbreaking.newbieguard.utils.UpdatesChecker;
import groundbreaking.newbieguard.utils.config.ConfigLoader;
import groundbreaking.newbieguard.utils.config.ConfigValues;
import groundbreaking.newbieguard.utils.logging.BukkitLogger;
import groundbreaking.newbieguard.utils.logging.ILogger;
import groundbreaking.newbieguard.utils.logging.PaperLogger;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public final class NewbieGuard extends JavaPlugin {

    @Getter
    private AbstractDB connectionHandler = null;

    @Getter
    private ConfigValues configValues = new ConfigValues(this);

    @Getter
    private ILogger myLogger;

    private FileConfiguration config;

    @Override
    public void onEnable() {
        final long startTime = System.currentTimeMillis();

        final ServerInfo serverInfo = new ServerInfo(this);
        if (!serverInfo.isPaperOrFork()) {
            logPaperWarning();
            Bukkit.getPluginManager().disablePlugin(this);
        }

        setupLogger(serverInfo);
        logLoggerType();

        config = new ConfigLoader(this).loadAndGet("config", 1.0);
        configValues.setValues();

        setupDatabaseHandler();
        setupConnection();

        setupCommand();

        final Server server = getServer();

        final UpdatesChecker updatesChecker = new UpdatesChecker(this);
        server.getScheduler().runTaskAsynchronously(this, updatesChecker::startCheck);

        final PluginManager pluginManager = server.getPluginManager();
        pluginManager.registerEvents(new ChatMessagesListener(this), this);
        pluginManager.registerEvents(new CommandsListeners(this), this);
        pluginManager.registerEvents(new ColumnCommandsListener(this), this);
        pluginManager.registerEvents(new UpdatesNotify(this), this);

        getLogger().info("Plugin was successfully started in: " + (System.currentTimeMillis() - startTime) + "ms.");
    }

    @Override
    public void onDisable() {
        connectionHandler.close();
    }

    private void logPaperWarning() {
        getLogger().warning("\u001b[91m=============== \u001b[31mWARNING \u001b[91m===============\u001b[0m");
        getLogger().warning("\u001b[91mThe plugin author against using Bukkit, Spigot etc.!\u001b[0m");
        getLogger().warning("\u001b[91mMove to Paper or his forks. To download Paper visit:\u001b[0m");
        getLogger().warning("\u001b[91mhttps://papermc.io/downloads/all\u001b[0m");
        getLogger().warning("\u001b[91m=======================================\u001b[0m");
    }

    private void logLoggerType() {
        if (myLogger instanceof PaperLogger) {
            myLogger.info("Plugin will use new ComponentLogger for logging.");
        }
        else {
            myLogger.info("Plugin will use default old BukkitLogger for logging. Because your server version is under 19!");
        }
    }

    private void setupDatabaseHandler() {
        final String dbType = config.getString("settings.database.type", "sqlite");
        if (dbType.equalsIgnoreCase("sqlite")) {
            final File dbFile = new File(getDataFolder() + File.separator + "database.db");
            loadDatabaseFile(dbFile);
            final String url = "jdbc:sqlite:" + dbFile;
            connectionHandler = new SQLite(url);
        }
        else if (dbType.equalsIgnoreCase("mariadb")) {

            final String host = config.getString("settings.database.maria-db.host");
            final String port = config.getString("settings.database.maria-db.port");
            final String dbName = config.getString("settings.database.maria-db.database-name");
            final String url = host + ":" + port + "/" + dbName;
            final String user = config.getString("settings.database.maria-db.username");
            final String pass = config.getString("settings.database.maria-db.password");

            connectionHandler = new MariaDB(url, user, pass);
        }
        else {
            throw new UnsupportedOperationException("Please choose SQLite or MariaDB as database!");
        }
    }

    public void setupConnection() {
        try {
            connectionHandler.createConnection();
        }
        catch (SQLException ex) {
            getLogger().warning("An error coursed while trying to open database connection.");
            ex.printStackTrace();

            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    public void loadDatabaseFile(File dbFile) {
        if (!dbFile.exists()) {
            try {
                dbFile.createNewFile();
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void setupLogger(final ServerInfo serverInfo) {
        myLogger = serverInfo.getSubVersion() >= 19
                ? new PaperLogger(this)
                : new BukkitLogger(this);
    }

    public void setupCommand() {
        getCommand("newbieguard").setExecutor((sender, command, label, args) -> {
            final long _startTime = System.currentTimeMillis();

            if (!sender.hasPermission("newbieguard.reload")) {
                sender.sendMessage(configValues.getNoPermMessages());
                return true;
            }

            connectionHandler.close();
            reloadConfig();

            sender.sendMessage(configValues.getReloadMessages().replace("%time%", String.valueOf(System.currentTimeMillis() - _startTime)));

            return true;
        });
    }

    @Override
    public @NotNull FileConfiguration getConfig() {
        return config;
    }

    @Override
    public void reloadConfig() {
        config = new ConfigLoader(this).loadAndGet("config", 1.0);
        configValues.setValues();
        setupDatabaseHandler();
        ChatMessagesListener.setTimeCounter(this);
        CommandsListeners.setTimeCounter(this);
        CommandsListeners.setMode(this);
    }
}