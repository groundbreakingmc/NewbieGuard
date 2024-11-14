package groundbreaking.newbieguard;

import groundbreaking.newbieguard.command.CommandHandler;
import groundbreaking.newbieguard.database.DatabaseHandler;
import groundbreaking.newbieguard.listeners.*;
import groundbreaking.newbieguard.utils.ServerInfo;
import groundbreaking.newbieguard.utils.UpdatesChecker;
import groundbreaking.newbieguard.utils.config.ConfigValues;
import groundbreaking.newbieguard.utils.logging.BukkitLogger;
import groundbreaking.newbieguard.utils.logging.ILogger;
import groundbreaking.newbieguard.utils.logging.PaperLogger;
import lombok.Getter;
import lombok.Setter;
import me.clip.placeholderapi.metrics.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.logging.Logger;

@Getter
public final class NewbieGuard extends JavaPlugin {

    @Setter
    private DatabaseHandler databaseHandler = null;

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

        new Metrics(this, 23872);

        this.setupLogger(serverInfo);
        this.logLoggerType();

        this.setupConnection();

        this.loadClassesAndEvents();

        this.configValues.setupValues();

        this.setupCommand();

        super.getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);

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
        final CommandHandler commandHandler = new CommandHandler(this);
        final PluginCommand pluginCommand = super.getCommand("newbieguard");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(commandHandler);
            pluginCommand.setTabCompleter(commandHandler);
        }
    }

    public void loadClassesAndEvents() {
        this.configValues = new ConfigValues(this);
        this.chatListener = new ChatMessagesListener(this);
        this.commandsListener = new CommandsListeners(this);
        this.columnCommandsListener = new ColumnCommandsListener(this);
    }

    public void reload() {
        this.configValues.setupValues();
    }

    public EventPriority getEventPriority(final String priority, final String sectionName) {
        return switch (priority) {
            case "LOWEST" -> EventPriority.LOWEST;
            case "LOW" -> EventPriority.LOW;
            case "NORMAL" -> EventPriority.NORMAL;
            case "HIGH" -> EventPriority.HIGH;
            case "HIGHEST" -> EventPriority.HIGHEST;
            default -> {
                this.myLogger.warning("Failed to parse value from \"" + sectionName +".listener-priority\" from config file. Please check your configuration file, or delete it and restart your server!");
                this.myLogger.warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/GigaChat/issues");
                throw new IllegalArgumentException("Failed to get event priority, please check your configuration files!");
            }
        };
    }
}