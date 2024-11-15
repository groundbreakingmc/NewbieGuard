package groundbreaking.newbieguard.utils.config;

import groundbreaking.newbieguard.NewbieGuard;
import groundbreaking.newbieguard.database.DatabaseHandler;
import groundbreaking.newbieguard.database.types.MariaDB;
import groundbreaking.newbieguard.database.types.SQLite;
import groundbreaking.newbieguard.listeners.ChatMessagesListener;
import groundbreaking.newbieguard.listeners.ColumnCommandsListener;
import groundbreaking.newbieguard.listeners.CommandsListeners;
import groundbreaking.newbieguard.listeners.RegisterUtil;
import groundbreaking.newbieguard.utils.UpdatesChecker;
import groundbreaking.newbieguard.utils.colorizer.*;
import groundbreaking.newbieguard.utils.logging.ILogger;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.EventExecutor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Getter
public final class ConfigValues {

    private int needTimePlayedToSendMessages;
    private int needTimePlayedToUseCommands;

    private boolean messageSendCheckEnabled;
    private boolean commandsSendCheckEnabled;
    private boolean columnCommandsSendCheckEnabled;

    private boolean isMessageSendDenySoundEnabled;
    private boolean isCommandUseDenySoundEnabled;
    private boolean isColumnCommandUseDenySoundEnabled;

    private boolean isMessageSendDenyTitleEnabled;
    private boolean isCommandUseDenyTitleEnabled;
    private boolean isColumnCommandUseDenyTitleEnabled;

    private Sound messageSendDenySound;
    private Sound commandUseDenySound;
    private Sound columnCommandUseDenySound;

    private float messageSendSoundVolume;
    private float commandUseSoundVolume;

    private float messageSendSoundPitch;
    private float commandUseSoundPitch;

    private float columnCommandUseSoundVolume;
    private float columnCommandUseSoundPitch;

    private Title.Times messageSendTitleTimes;
    private Title.Times commandUseTitleTimes;

    private Title columnCommandUseDenyTitle;

    private final List<String> blockedWordsForChat = new ArrayList<>();
    private final List<String> blockedCommands = new ArrayList<>();
    private final List<String> blockedCommandsWithColumns = new ArrayList<>();

    private String noPermMessage;
    private String reloadMessage;
    private String playerNotFound;
    private String removedFromMessagesMessage;
    private String removedFromCommandsMessage;
    private String usageErrorMessage;
    private String helpMessage;
    private String messageSendCooldownMessage;
    private String commandUseCooldownMessage;
    private String columnCommandUseDenyMessage;

    private String messageSendDenyTitle;
    private String messageSendDenySubtitle;

    private String commandUseDenyTitle;
    private String commandUseDenySubtitle;

    @Getter private static String timeDays;
    @Getter private static String timeHours;
    @Getter private static String timeMinutes;
    @Getter private static String timeSeconds;

    private final NewbieGuard plugin;
    private final ILogger logger;

    public ConfigValues(final NewbieGuard plugin) {
        this.plugin = plugin;
        this.logger = plugin.getMyLogger();
    }

    public void setupValues() {
        final FileConfiguration config = new ConfigLoader(this.plugin).loadAndGet("config", 1.0);
        final IColorizer colorizer = this.getColorizer(config);

        if (colorizer == null) {
            this.logger.warning("Failed to load colorizer from path \"settings.serializer-for-formats\" from file \"config.yml\". Please check your configuration file, or delete it and restart your server!");
            this.logger.warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/NewbieGuard/issues");
            Bukkit.getPluginManager().disablePlugin(this.plugin);
            return;
        }

        this.setupSettings(config);
        this.setupMessagesSend(config, colorizer);
        this.setupCommandsUseValues(config, colorizer);
        this.setupColumnCommandsUseValues(config, colorizer);
        this.setupMessages(config, colorizer);
    }

    private void setupSettings(final FileConfiguration config) {
        final ConfigurationSection settingsSection = config.getConfigurationSection("settings");
        if (settingsSection != null) {
            this.setupUpdates(settingsSection);
            this.setupDatabaseHandler(settingsSection);
        } else {
            this.logger.warning("Failed to load section \"settings\" from file \"config.yml\". Please check your configuration file, or delete it and restart your server!");
            this.logger.warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/NewbieGuard/issues");
        }
    }

    private void setupUpdates(final ConfigurationSection settings) {
        final ConfigurationSection updatesSection = settings.getConfigurationSection("updates");
        if (updatesSection != null) {
            final boolean checkForUpdates = updatesSection.getBoolean("check");
            final boolean downloadUpdate = updatesSection.getBoolean("auto-update");

            final UpdatesChecker updatesChecker = new UpdatesChecker(this.plugin);
            this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () ->
                    updatesChecker.check(checkForUpdates, downloadUpdate)
            );
        } else {
            this.logger.warning("Failed to load section \"settings.updates\" from file \"config.yml\". Please check your configuration file, or delete it and restart your server!");
            this.logger.warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/NewbieGuard/issues");
        }
    }

    private void setupDatabaseHandler(final ConfigurationSection settings) {
        final ConfigurationSection databaseSection = settings.getConfigurationSection("database");
        if (databaseSection != null) {
            final String type = databaseSection.getString("type");
            switch (type.toLowerCase()) {
                case "sqlite" -> {
                    final File dbFile = new File(this.plugin.getDataFolder() + File.separator + "database.db");
                    final String url = "jdbc:sqlite:" + dbFile;
                    final DatabaseHandler connectionHandler = new SQLite(url);
                    this.plugin.setDatabaseHandler(connectionHandler);
                }
                case "mariadb" -> {
                    final ConfigurationSection mariaDb = databaseSection.getConfigurationSection("maria-db");
                    if (mariaDb != null) {
                        final String host = mariaDb.getString("host");
                        final String port = mariaDb.getString("port");
                        final String dbName = mariaDb.getString("database-name");
                        final String user = mariaDb.getString("username");
                        final String pass = mariaDb.getString("password");

                        final String url = host + ":" + port + "/" + dbName;
                        final DatabaseHandler connectionHandler = new MariaDB(url, user, pass);
                        this.plugin.setDatabaseHandler(connectionHandler);
                    }
                }
                default -> throw new UnsupportedOperationException("Please choose SQLite or MariaDB as database!");
            }
        } else {
            this.logger.warning("Failed to load section \"settings.database\" from file \"config.yml\". Please check your configuration file, or delete it and restart your server!");
            this.logger.warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/NewbieGuard/issues");
        }
    }

    private void setupMessagesSend(final FileConfiguration config, final IColorizer colorizer) {
        final ConfigurationSection messagesSend = config.getConfigurationSection("messages-send");
        if (messagesSend != null) {
            this.messageSendCheckEnabled = messagesSend.getBoolean("enable");
            final ChatMessagesListener chatMessagesListener = this.plugin.getChatListener();
            if (this.messageSendCheckEnabled) {
                final String listenerPriorityString = messagesSend.getString("listener-priority").toUpperCase();
                final boolean ignoreCancelled = messagesSend.getBoolean("ignore-cancelled");

                final boolean countFromFirstJoin = messagesSend.getBoolean("count-time-from-first-join");
                this.plugin.getChatListener().setTimeCounter(countFromFirstJoin);

                this.needTimePlayedToSendMessages = messagesSend.getInt("need-time-played");

                this.messageSendCooldownMessage = this.getMessage(messagesSend, "cooldown", "messages-send.cooldown", colorizer);

                this.setMessageSendDenySound(messagesSend);
                this.setMessageSendDenyTitle(messagesSend, colorizer);

                this.blockedWordsForChat.clear();
                this.blockedWordsForChat.addAll(messagesSend.getStringList("blocked-words"));

                final EventExecutor eventExecutor = (listener, event) -> chatMessagesListener.onEvent((AsyncPlayerChatEvent) event);
                final EventPriority eventPriority = this.plugin.getEventPriority(listenerPriorityString, "messages-send");
                RegisterUtil.register(this.plugin, chatMessagesListener, AsyncPlayerChatEvent.class, eventPriority, ignoreCancelled, eventExecutor);
            } else {
                RegisterUtil.unregister(chatMessagesListener);
            }
        } else {
            this.logger.warning("Failed to load section \"messages-send\" from file \"config.yml\". Please check your configuration file, or delete it and restart your server!");
            this.logger.warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/NewbieGuard/issues");
            Bukkit.getPluginManager().disablePlugin(this.plugin);
        }
    }

    private void setupCommandsUseValues(final FileConfiguration config, final IColorizer colorizer) {
        final ConfigurationSection commandUse = config.getConfigurationSection("commands-use");
        if (commandUse != null) {
            this.commandsSendCheckEnabled = commandUse.getBoolean("enable");
            final CommandsListeners commandsListeners = this.plugin.getCommandsListener();
            if (this.messageSendCheckEnabled) {
                final String listenerPriorityString = commandUse.getString("listener-priority").toUpperCase();
                final boolean ignoreCancelled = commandUse.getBoolean("ignore-cancelled");

                final boolean countFromFirstJoin = commandUse.getBoolean("count-time-from-first-join");
                this.plugin.getCommandsListener().setTimeCounter(countFromFirstJoin);

                final boolean useWhiteList = commandUse.getBoolean("use-whitelist");
                this.plugin.getCommandsListener().setMode(useWhiteList);

                this.needTimePlayedToUseCommands = commandUse.getInt("need-time-played");

                this.commandUseCooldownMessage = this.getMessage(commandUse, "cooldown", "commands-use.cooldown", colorizer);

                this.setupCommandUseDenySound(commandUse);
                this.setCommandUseDenyTitle(commandUse, colorizer);

                this.blockedCommands.clear();
                this.blockedCommands.addAll(commandUse.getStringList("list"));

                final EventExecutor eventExecutor = (listener, event) -> commandsListeners.onEvent((PlayerCommandPreprocessEvent) event);
                final EventPriority eventPriority = this.plugin.getEventPriority(listenerPriorityString, "commands-use");
                RegisterUtil.register(this.plugin, commandsListeners, PlayerCommandPreprocessEvent.class, eventPriority, ignoreCancelled, eventExecutor);
            } else {
                RegisterUtil.unregister(commandsListeners);
            }
        } else {
            this.logger.warning("Failed to load section \"commands-use\" from file \"config.yml\". Please check your configuration file, or delete it and restart your server!");
            this.logger.warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/NewbieGuard/issues");
        }
    }

    private void setupColumnCommandsUseValues(final FileConfiguration config, final IColorizer colorizer) {
        final ConfigurationSection columnCommandUse = config.getConfigurationSection("column-commands-use");
        if (columnCommandUse != null) {
            this.columnCommandsSendCheckEnabled = columnCommandUse.getBoolean("enable");
            final ColumnCommandsListener columnCommandsListener = this.plugin.getColumnCommandsListener();
            if (columnCommandsSendCheckEnabled) {
                final String listenerPriorityString = columnCommandUse.getString("listener-priority").toUpperCase();
                final boolean ignoreCancelled = columnCommandUse.getBoolean("ignore-cancelled");

                this.columnCommandUseDenyMessage = this.getMessage(columnCommandUse, "deny-message", "column-commands-use.deny-message", colorizer);

                this.setColumnCommandUseDenySound(columnCommandUse);
                this.setColumnCommandUseDenyTitle(columnCommandUse, colorizer);

                final EventExecutor eventExecutor = (listener, event) -> columnCommandsListener.onEvent((PlayerCommandPreprocessEvent) event);
                final EventPriority eventPriority = this.plugin.getEventPriority(listenerPriorityString, "column-commands-use");
                RegisterUtil.register(this.plugin, columnCommandsListener, PlayerCommandPreprocessEvent.class, eventPriority, ignoreCancelled, eventExecutor);
            } else {
                RegisterUtil.unregister(columnCommandsListener);
            }
        } else {
            this.logger.warning("Failed to load section \"column-commands-use\" from file \"config.yml\". Please check your configuration file, or delete it and restart your server!");
            this.logger.warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/NewbieGuard/issues");
        }
    }

    private void setupMessages(final FileConfiguration config, final IColorizer colorizer) {
        final ConfigurationSection pluginMessages = config.getConfigurationSection("plugin-messages");
        if (pluginMessages != null) {
            this.noPermMessage = this.getMessage(pluginMessages, "no-permission", "plugin-messages.no-perm", colorizer);
            this.reloadMessage = this.getMessage(pluginMessages, "reload", "plugin-messages.reload", colorizer);
            this.playerNotFound = this.getMessage(pluginMessages, "player-not-found", "plugin-messages.player-not-found", colorizer);
            this.removedFromMessagesMessage = this.getMessage(pluginMessages, "removed-from-messages", "plugin-messages.removed-from-messages", colorizer);
            this.removedFromCommandsMessage = this.getMessage(pluginMessages, "removed-from-commands", "plugin-messages.removed-from-commands", colorizer);
            this.usageErrorMessage = this.getMessage(pluginMessages, "usage-error", "plugin-messages.usage-error", colorizer);
            this.helpMessage = this.getMessage(pluginMessages, "help", "plugin-messages.help", colorizer);

            final ConfigurationSection time = pluginMessages.getConfigurationSection("time");
            if (time != null) {
                final String timeDaysString = time.getString("days", "&cError, check \"plugin-messages.time.days\"");
                timeDays = colorizer.colorize(timeDaysString);

                final String timeHoursString = time.getString("hours", "&cError, check \"plugin-messages.time.hours\"");
                timeHours = colorizer.colorize(timeHoursString);

                final String timeMinutesString = time.getString("minutes", "&cError, check \"plugin-messages.time.minutes\"");
                timeMinutes = colorizer.colorize(timeMinutesString);

                final String timeSecondsString = time.getString("seconds", "&cError, check \"plugin-messages.time.seconds\"");
                timeSeconds = colorizer.colorize(timeSecondsString);
            } else {
                this.logger.warning("Failed to load section \"plugin-messages.time\" from file \"config.yml\". Please check your configuration file, or delete it and restart your server!");
                this.logger.warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/NewbieGuard/issues");
                Bukkit.getPluginManager().disablePlugin(this.plugin);
            }
        } else {
            this.logger.warning("Failed to load section \"plugin-messages\" from file \"config.yml\". Please check your configuration file, or delete it and restart your server!");
            this.logger.warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/NewbieGuard/issues");
            Bukkit.getPluginManager().disablePlugin(this.plugin);
        }
    }

    private void setMessageSendDenySound(final ConfigurationSection section) {
        final String soundString = section.getString("deny-sound");
        if (soundString == null) {
            this.logger.warning("Failed to load sound \"messages-send.deny-sound\" from file \"config.yml\". Please check your configuration file, or delete it and restart your server!");
            this.logger.warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/NewbieGuard/issues");

            this.isMessageSendDenySoundEnabled = false;
        } else if (soundString.equalsIgnoreCase("disabled")) {
            this.isMessageSendDenySoundEnabled = false;
        } else {
            final String[] params = soundString.split(";");
            this.messageSendDenySound = params.length >= 1 ? Sound.valueOf(params[0].toUpperCase()) : Sound.ITEM_SHIELD_BREAK;
            this.messageSendSoundVolume = params.length >= 2 ? Float.parseFloat(params[1]) : 1.0f;
            this.messageSendSoundPitch = params.length >= 3 ? Float.parseFloat(params[2]) : 1.0f;

            this.isMessageSendDenySoundEnabled = true;
        }
    }

    private void setupCommandUseDenySound(final ConfigurationSection section) {
        final String soundString = section.getString("deny-sound");
        if (soundString == null) {
            this.logger.warning("Failed to load sound \"commands-use.deny-sound\" from file \"config.yml\". Please check your configuration file, or delete it and restart your server!");
            this.logger.warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/NewbieGuard/issues");

            this.isCommandUseDenySoundEnabled = false;
        } else if (soundString.equalsIgnoreCase("disabled")) {
            this.isCommandUseDenySoundEnabled = false;
        } else {
            final String[] params = soundString.split(";");
            this.commandUseDenySound = params.length >= 1 ? Sound.valueOf(params[0].toUpperCase()) : Sound.ITEM_SHIELD_BREAK;
            this.commandUseSoundVolume = params.length >= 2 ? Float.parseFloat(params[1]) : 1.0f;
            this.commandUseSoundPitch = params.length >= 3 ? Float.parseFloat(params[2]) : 1.0f;

            this.isCommandUseDenySoundEnabled = true;
        }
    }

    private void setColumnCommandUseDenySound(final ConfigurationSection section) {
        final String soundString = section.getString("deny-sound");
        if (soundString == null) {
            this.logger.warning("Failed to load sound \"column-commands-use.deny-sound\" from file \"config.yml\". Please check your configuration file, or delete it and restart your server!");
            this.logger.warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/NewbieGuard/issues");

            this.isColumnCommandUseDenySoundEnabled = false;
        } else if (soundString.equalsIgnoreCase("disabled")) {
            this.isColumnCommandUseDenySoundEnabled = false;
        } else {
            final String[] params = soundString.split(";");
            this.columnCommandUseDenySound = params.length >= 1 ? Sound.valueOf(params[0].toUpperCase()) : Sound.ITEM_SHIELD_BREAK;
            this.columnCommandUseSoundVolume = params.length >= 2 ? Float.parseFloat(params[1]) : 1.0f;
            this.columnCommandUseSoundPitch = params.length >= 3 ? Float.parseFloat(params[2]) : 1.0f;

            this.isColumnCommandUseDenySoundEnabled = true;
        }
    }

    private void setMessageSendDenyTitle(final ConfigurationSection section, final IColorizer colorizer) {
        final ConfigurationSection denyTitle = section.getConfigurationSection("deny-title");
        if (denyTitle != null) {
            final String durationString = denyTitle.getString("duration");
            if (durationString.equalsIgnoreCase("disabled")) {
                this.isMessageSendDenyTitleEnabled = false;
                return;
            }

            final String[] params = durationString.split(";");
            final int fadeIn = params.length >= 1 ? Integer.parseInt(params[0]) : 10;
            final int stay = params.length >= 2 ? Integer.parseInt(params[1]) : 40;
            final int fadeOut = params.length >= 3 ? Integer.parseInt(params[2]) : 20;

            this.messageSendTitleTimes = Title.Times.of(
                    Ticks.duration(fadeIn),
                    Ticks.duration(stay),
                    Ticks.duration(fadeOut)
            );

            String denyTitleText = section.getString("title-text");
            String denySubtitleText = section.getString("subtitle-text");
            if (denyTitleText == null) {
                denyTitleText = "&cError";
                denySubtitleText = "&fCheck \"messages-send.dent-title.title-text\"";
            } else if (denySubtitleText == null) {
                denyTitleText = "&cError";
                denySubtitleText = "&fCheck \"messages-send.dent-title.subtitle-text\"";
            }

            this.messageSendDenyTitle = colorizer.colorize(denyTitleText);
            this.messageSendDenySubtitle = colorizer.colorize(denySubtitleText);

            this.isMessageSendDenyTitleEnabled = true;
        } else {
            this.logger.warning("Failed to load title \"messages-send.deny-title\" from file \"config.yml\". Please check your configuration file, or delete it and restart your server!");
            this.logger.warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/NewbieGuard/issues");

            this.isMessageSendDenyTitleEnabled = false;
        }
    }

    private void setCommandUseDenyTitle(final ConfigurationSection section, final IColorizer colorizer) {
        final ConfigurationSection denyTitle = section.getConfigurationSection("deny-title");
        if (denyTitle != null) {
            final String durationString = denyTitle.getString("duration");
            if (durationString.equalsIgnoreCase("disabled")) {
                this.isCommandUseDenyTitleEnabled = false;
                return;
            }

            final String[] params = durationString.split(";");
            final int fadeIn = params.length >= 1 ? Integer.parseInt(params[0]) : 10;
            final int stay = params.length >= 2 ? Integer.parseInt(params[1]) : 40;
            final int fadeOut = params.length >= 3 ? Integer.parseInt(params[2]) : 20;

            this.commandUseTitleTimes = Title.Times.of(
                    Ticks.duration(fadeIn),
                    Ticks.duration(stay),
                    Ticks.duration(fadeOut)
            );

            String denyTitleText = section.getString("title-text");
            String denySubtitleText = section.getString("subtitle-text");
            if (denyTitleText == null) {
                denyTitleText = "&cError";
                denySubtitleText = "&fCheck \"commands-use.dent-title.title-text\"";
            } else if (denySubtitleText == null) {
                denyTitleText = "&cError";
                denySubtitleText = "&fCheck \"commands-use.dent-title.subtitle-text\"";
            }

            this.commandUseDenyTitle = colorizer.colorize(denyTitleText);
            this.commandUseDenySubtitle = colorizer.colorize(denySubtitleText);

            this.isCommandUseDenyTitleEnabled = true;
        } else {
            this.logger.warning("Failed to load title \"commands-use.deny-title\" from file \"config.yml\". Please check your configuration file, or delete it and restart your server!");
            this.logger.warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/NewbieGuard/issues");

            this.isCommandUseDenyTitleEnabled = false;
        }
    }

    private void setColumnCommandUseDenyTitle(final ConfigurationSection section, final IColorizer colorizer) {
        final ConfigurationSection denyTitle = section.getConfigurationSection("deny-title");
        if (denyTitle != null) {
            final String durationString = denyTitle.getString("duration");
            if (durationString.equalsIgnoreCase("disabled")) {
                this.isCommandUseDenyTitleEnabled = false;
                return;
            }

            final String[] params = durationString.split(";");
            final int fadeIn = params.length >= 1 ? Integer.parseInt(params[0]) : 10;
            final int stay = params.length >= 2 ? Integer.parseInt(params[1]) : 40;
            final int fadeOut = params.length >= 3 ? Integer.parseInt(params[2]) : 20;

            final Title.Times titleTimes = Title.Times.of(
                    Ticks.duration(fadeIn),
                    Ticks.duration(stay),
                    Ticks.duration(fadeOut)
            );

            String denyTitleText = section.getString("title-text");
            String denySubtitleText = section.getString("subtitle-text");
            if (denyTitleText == null) {
                denyTitleText = "&cError";
                denySubtitleText = "&fCheck \"commands-use.dent-title.title-text\"";
            } else if (denySubtitleText == null) {
                denyTitleText = "&cError";
                denySubtitleText = "&fCheck \"commands-use.dent-title.subtitle-text\"";
            }

            final Component titleText = Component.text(colorizer.colorize(denyTitleText));
            final Component subtitleText = Component.text(colorizer.colorize(denySubtitleText));

            this.columnCommandUseDenyTitle = Title.title(titleText, subtitleText, titleTimes);

            this.isColumnCommandUseDenyTitleEnabled = true;
        } else {
            this.logger.warning("Failed to load title \"commands-use.deny-title\" from file \"config.yml\". Please check your configuration file, or delete it and restart your server!");
            this.logger.warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/NewbieGuard/issues");

            this.isColumnCommandUseDenyTitleEnabled = false;
        }
    }

    private String getMessage(final ConfigurationSection config, final String path, final String fullPath, final IColorizer colorizer) {
        final String message = config.getString(path, "&4(!) &cFailed to get message on path: " + fullPath);
        return colorizer.colorize(message);
    }

    private IColorizer getColorizer(final FileConfiguration config) {
        final String colorizerMode = config.getString("settings.serializer-for-formats");

        if (colorizerMode == null) {
            return null;
        }

        return switch (colorizerMode.toUpperCase()) {
            case "MINIMESSAGE" -> new MiniMessageColorizer();
            case "LEGACY" -> new LegacyColorizer();
            case "LEGACY_ADVANCED" -> new LegacyAdvancedColorizer();
            default -> new VanillaColorizer();
        };
    }
}