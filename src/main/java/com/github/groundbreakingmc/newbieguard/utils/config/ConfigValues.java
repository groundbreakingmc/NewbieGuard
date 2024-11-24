package com.github.groundbreakingmc.newbieguard.utils.config;

import com.github.groundbreakingmc.newbieguard.NewbieGuard;
import com.github.groundbreakingmc.newbieguard.constructors.CommandGroup;
import com.github.groundbreakingmc.newbieguard.listeners.RegisterUtil;
import com.github.groundbreakingmc.newbieguard.listeners.commands.ColonCommandsListener;
import com.github.groundbreakingmc.newbieguard.listeners.commands.CommandsListeners;
import com.github.groundbreakingmc.newbieguard.listeners.messages.ChatMessagesListener;
import com.github.groundbreakingmc.newbieguard.utils.UpdatesChecker;
import com.github.groundbreakingmc.newbieguard.utils.colorizer.*;
import com.github.groundbreakingmc.newbieguard.utils.commands.BlackList;
import com.github.groundbreakingmc.newbieguard.utils.commands.WhiteList;
import com.github.groundbreakingmc.newbieguard.utils.logging.ILogger;
import com.github.groundbreakingmc.newbieguard.utils.time.FirstEntryCounter;
import com.github.groundbreakingmc.newbieguard.utils.time.ITimeCounter;
import com.github.groundbreakingmc.newbieguard.utils.time.OnlineCounter;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
public final class ConfigValues {

    private ITimeCounter messagesSendTimeCounter;

    private int requiredTimeToSendMessages;

    private boolean isMessageSendDenySoundEnabled;
    private boolean isColonCommandUseDenySoundEnabled;

    private boolean isMessageSendDenyTitleEnabled;
    private boolean isColonCommandUseDenyTitleEnabled;

    private Sound messageSendDenySound;
    private Sound colonCommandUseDenySound;

    private float messageSendSoundVolume;
    private float messageSendSoundPitch;

    private float colonCommandUseSoundVolume;
    private float colonCommandUseSoundPitch;

    private Title.Times messageSendTitleTimes;

    private Title colonCommandUseDenyTitle;

    private final Map<String, CommandGroup> blockedCommands = new HashMap();

    private String noPermMessage;
    private String reloadMessage;
    private String playerNotFound;
    private String removedFromMessagesMessage;
    private String removedFromCommandsMessage;
    private String usageErrorMessage;
    private String helpMessage;
    private String messageSendCooldownMessage;
    private String colonCommandUseDenyMessage;

    private String messageSendDenyTitle;
    private String messageSendDenySubtitle;

    @Getter
    private static String timeDays;
    @Getter
    private static String timeHours;
    @Getter
    private static String timeMinutes;
    @Getter
    private static String timeSeconds;

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
        this.setupColonCommandsUseValues(config, colorizer);
        this.setupMessages(config, colorizer);
    }

    private void setupSettings(final FileConfiguration config) {
        final ConfigurationSection settingsSection = config.getConfigurationSection("settings");
        if (settingsSection != null) {
            this.setupUpdates(settingsSection);
        } else {
            this.logger.warning("Failed to load section \"settings\" from file \"config.yml\". Please check your configuration file, or delete it and restart your server!");
            this.logger.warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/NewbieGuard/issues");
        }
    }

    private void setupUpdates(final ConfigurationSection settings) {
        final ConfigurationSection updatesSection = settings.getConfigurationSection("updates");
        if (updatesSection != null) {
            final boolean checkForUpdates = updatesSection.getBoolean("check");
            if (!checkForUpdates) {
                this.plugin.getMyLogger().warning("Updates checker was disabled, but it's not recommend by the author to do it!");
                return;
            }

            final boolean downloadUpdate = updatesSection.getBoolean("auto-update");

            final UpdatesChecker updatesChecker = new UpdatesChecker(this.plugin);
            this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () ->
                    updatesChecker.check(downloadUpdate, false)
            );
        } else {
            this.logger.warning("Failed to load section \"settings.updates\" from file \"config.yml\". Please check your configuration file, or delete it and restart your server!");
            this.logger.warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/NewbieGuard/issues");
        }
    }

    private void setupMessagesSend(final FileConfiguration config, final IColorizer colorizer) {
        final ChatMessagesListener chatMessagesListener = this.plugin.getChatListener();

        final ConfigurationSection messagesSend = config.getConfigurationSection("messages-send");
        if (messagesSend != null) {

            final boolean checkEnabled = messagesSend.getBoolean("enable");
            if (checkEnabled) {
                final boolean countFromFirstJoin = messagesSend.getBoolean("count-time-from-first-join");
                this.messagesSendTimeCounter = countFromFirstJoin ? new FirstEntryCounter() : new OnlineCounter();

                this.requiredTimeToSendMessages = messagesSend.getInt("need-time-played");

                this.messageSendCooldownMessage = this.getMessage(messagesSend, "cooldown", "messages-send.cooldown", colorizer);

                this.setMessageSendDenySound(messagesSend);
                this.setMessageSendDenyTitle(messagesSend, colorizer);

                final EventExecutor eventExecutor = (listener, event) -> chatMessagesListener.onEvent((AsyncPlayerChatEvent) event);
                final EventPriority eventPriority = this.plugin.getEventPriority(messagesSend);
                final boolean ignoreCancelled = messagesSend.getBoolean("ignore-cancelled");

                RegisterUtil.register(this.plugin, chatMessagesListener, AsyncPlayerChatEvent.class, eventPriority, ignoreCancelled, eventExecutor);
            }
        } else {
            this.logger.warning("Failed to load section \"messages-send\" from file \"config.yml\". Please check your configuration file, or delete it and restart your server!");
            this.logger.warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/NewbieGuard/issues");
        }

        RegisterUtil.unregister(chatMessagesListener);
    }

    private void setupCommandsUseValues(final FileConfiguration config, final IColorizer colorizer) {
        final CommandsListeners commandsListeners = this.plugin.getCommandsListener();

        final ConfigurationSection commandUse = config.getConfigurationSection("commands-use");
        if (commandUse != null) {

            final boolean checkEnabled = commandUse.getBoolean("enable");
            if (checkEnabled) {

                final ConfigurationSection groupSection = commandUse.getConfigurationSection("groups");
                if (groupSection != null) {

                    final Set<String> groupKeys = groupSection.getKeys(false);

                    for (final String key : groupKeys) {
                        final ConfigurationSection keySection = groupSection.getConfigurationSection(key);
                        final CommandGroup commandGroup = this.getGroup(keySection, colorizer);

                        final List<String> list = keySection.getStringList("list");
                        for (int i = 0; i < list.size(); i++) {
                            this.blockedCommands.put(list.get(i), commandGroup);
                        }
                    }

                    final EventExecutor eventExecutor = (listener, event) -> commandsListeners.onEvent((PlayerCommandPreprocessEvent) event);
                    final EventPriority eventPriority = this.plugin.getEventPriority(commandUse);
                    final boolean ignoreCancelled = commandUse.getBoolean("ignore-cancelled");

                    RegisterUtil.register(this.plugin, commandsListeners, PlayerCommandPreprocessEvent.class, eventPriority, ignoreCancelled, eventExecutor);
                    return;
                } else {
                    this.logger.warning("Failed to load section \"commands-use.groups\" from file \"config.yml\". Please check your configuration file, or delete it and restart your server!");
                    this.logger.warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/NewbieGuard/issues");
                }
            }
        } else {
            this.logger.warning("Failed to load section \"commands-use\" from file \"config.yml\". Please check your configuration file, or delete it and restart your server!");
            this.logger.warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/NewbieGuard/issues");
        }

        RegisterUtil.unregister(commandsListeners);
    }

    private CommandGroup getGroup(final ConfigurationSection keySection, final IColorizer colorizer) {
        final CommandGroup.CommandGroupBuilder commandGroup = CommandGroup.builder();

        commandGroup.sectionName(keySection.getName());

        final boolean countFromFirstJoin = keySection.getBoolean("count-time-from-first-join");
        commandGroup.timeCounter(countFromFirstJoin ? new FirstEntryCounter() : new OnlineCounter());

        final boolean useBlackList = keySection.getBoolean("use-blacklist");
        commandGroup.mode(useBlackList ? new BlackList() : new WhiteList());

        final int requiredTime = keySection.getInt("need-time-played");
        commandGroup.requiredTime(requiredTime);

        final String cooldownMessage = this.getMessage(keySection, "cooldown", "commands-use.cooldown", colorizer);
        commandGroup.cooldownMessage(cooldownMessage);

        this.setupCommandUseDenySound(keySection, commandGroup);
        this.setCommandUseDenyTitle(keySection, commandGroup, colorizer);

        return commandGroup.build();
    }

    private void setupColonCommandsUseValues(final FileConfiguration config, final IColorizer colorizer) {
        final ColonCommandsListener colonCommandsListener = this.plugin.getColonCommandsListener();

        final ConfigurationSection colonCommandUse = config.getConfigurationSection("colon-commands-use");

        if (colonCommandUse != null) {

            final boolean checkEnabled = colonCommandUse.getBoolean("enable");
            if (checkEnabled) {
                final boolean ignoreCancelled = colonCommandUse.getBoolean("ignore-cancelled");

                this.colonCommandUseDenyMessage = this.getMessage(colonCommandUse, "deny-message", "colon-commands-use.deny-message", colorizer);

                this.setColonCommandUseDenySound(colonCommandUse);
                this.setColonCommandUseDenyTitle(colonCommandUse, colorizer);

                final EventExecutor eventExecutor = (listener, event) -> colonCommandsListener.onEvent((PlayerCommandPreprocessEvent) event);
                final EventPriority eventPriority = this.plugin.getEventPriority(colonCommandUse);
                RegisterUtil.register(this.plugin, colonCommandsListener, PlayerCommandPreprocessEvent.class, eventPriority, ignoreCancelled, eventExecutor);
                return;
            }
        } else {
            this.logger.warning("Failed to load section \"colon-commands-use\" from file \"config.yml\". Please check your configuration file, or delete it and restart your server!");
            this.logger.warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/NewbieGuard/issues");
        }

        RegisterUtil.unregister(colonCommandsListener);
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

    private void setupCommandUseDenySound(final ConfigurationSection section, final CommandGroup.CommandGroupBuilder builder) {
        final String soundString = section.getString("deny-sound");
        if (soundString == null) {
            this.logger.warning("Failed to load sound \"commands-use." + section.getName() + ".deny-sound\" from file \"config.yml\". Please check your configuration file, or delete it and restart your server!");
            this.logger.warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/NewbieGuard/issues");

            builder.isDenySoundEnabled(false);
        } else if (soundString.equalsIgnoreCase("disabled")) {
            builder.isDenySoundEnabled(false);
        } else {
            final String[] params = soundString.split(";");
            builder.denySound(params.length >= 1 ? Sound.valueOf(params[0].toUpperCase()) : Sound.ITEM_SHIELD_BREAK);
            builder.denySoundVolume(params.length >= 2 ? Float.parseFloat(params[1]) : 1.0f);
            builder.denySoundPitch(params.length >= 3 ? Float.parseFloat(params[2]) : 1.0f);
            builder.isDenySoundEnabled(true);
        }
    }

    private void setColonCommandUseDenySound(final ConfigurationSection section) {
        final String soundString = section.getString("deny-sound");
        if (soundString == null) {
            this.logger.warning("Failed to load sound \"colon-commands-use.deny-sound\" from file \"config.yml\". Please check your configuration file, or delete it and restart your server!");
            this.logger.warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/NewbieGuard/issues");

            this.isColonCommandUseDenySoundEnabled = false;
        } else if (soundString.equalsIgnoreCase("disabled")) {
            this.isColonCommandUseDenySoundEnabled = false;
        } else {
            final String[] params = soundString.split(";");
            this.colonCommandUseDenySound = params.length >= 1 ? Sound.valueOf(params[0].toUpperCase()) : Sound.ITEM_SHIELD_BREAK;
            this.colonCommandUseSoundVolume = params.length >= 2 ? Float.parseFloat(params[1]) : 1.0f;
            this.colonCommandUseSoundPitch = params.length >= 3 ? Float.parseFloat(params[2]) : 1.0f;

            this.isColonCommandUseDenySoundEnabled = true;
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

    private void setCommandUseDenyTitle(final ConfigurationSection section, final CommandGroup.CommandGroupBuilder builder, final IColorizer colorizer) {
        final ConfigurationSection denyTitle = section.getConfigurationSection("deny-title");
        if (denyTitle == null) {
            this.logger.warning("Failed to load title \"commands-use." + section.getName() + ".deny-title\" from file \"config.yml\". Please check your configuration file, or delete it and restart your server!");
            this.logger.warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/NewbieGuard/issues");

            builder.isDenyTitleEnabled(false);
        } else {
            final String durationString = denyTitle.getString("duration");
            if (durationString == null || durationString.equalsIgnoreCase("disabled")) {
                builder.isDenyTitleEnabled(false);
                return;
            }

            final String[] params = durationString.split(";");
            final int fadeIn = params.length >= 1 ? Integer.parseInt(params[0]) : 10;
            final int stay = params.length >= 2 ? Integer.parseInt(params[1]) : 40;
            final int fadeOut = params.length >= 3 ? Integer.parseInt(params[2]) : 20;

            builder.denyTitleTimes(
                    Title.Times.of(
                            Ticks.duration(fadeIn),
                            Ticks.duration(stay),
                            Ticks.duration(fadeOut)
                    )
            );

            String denyTitleText = section.getString("title-text");
            String denySubtitleText = section.getString("subtitle-text");
            if (denyTitleText == null) {
                denyTitleText = "&cError";
                denySubtitleText = "&fCheck \"commands-use.deny-title.title-text\"";
            } else if (denySubtitleText == null) {
                denyTitleText = "&cError";
                denySubtitleText = "&fCheck \"commands-use.deny-title.subtitle-text\"";
            }

            builder.denyTitle(Component.text(colorizer.colorize(denyTitleText)));
            builder.denySubtitle(Component.text(colorizer.colorize(denySubtitleText)));
            builder.isDenyTitleEnabled(true);
        }
    }

    private void setColonCommandUseDenyTitle(final ConfigurationSection section, final IColorizer colorizer) {
        final ConfigurationSection denyTitle = section.getConfigurationSection("deny-title");
        if (denyTitle != null) {
            final String durationString = denyTitle.getString("duration");
            if (durationString.equalsIgnoreCase("disabled")) {
                this.isColonCommandUseDenyTitleEnabled = false;
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

            this.colonCommandUseDenyTitle = Title.title(titleText, subtitleText, titleTimes);

            this.isColonCommandUseDenyTitleEnabled = true;
        } else {
            this.logger.warning("Failed to load title \"commands-use.deny-title\" from file \"config.yml\". Please check your configuration file, or delete it and restart your server!");
            this.logger.warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/NewbieGuard/issues");

            this.isColonCommandUseDenyTitleEnabled = false;
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