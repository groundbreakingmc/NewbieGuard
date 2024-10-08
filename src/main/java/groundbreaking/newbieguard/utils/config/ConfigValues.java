package groundbreaking.newbieguard.utils.config;

import groundbreaking.newbieguard.NewbieGuard;
import groundbreaking.newbieguard.utils.ServerInfo;
import groundbreaking.newbieguard.utils.colorizer.IColorizer;
import groundbreaking.newbieguard.utils.colorizer.LegacyColorizer;
import groundbreaking.newbieguard.utils.colorizer.MiniMessagesColorizer;
import groundbreaking.newbieguard.utils.colorizer.VanillaColorizer;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public final class ConfigValues {

    @Getter
    private int
            needTimePlayedToSendMessages,
            needTimePlayedToUseCommands;

    @Getter
    private boolean
            messageSendCheckEnabled,
            commandsSendCheckEnabled,
            columnCommandsSendCheckEnabled,

            messageSendIgnoreCancelled,
            messageSendPriorityLowest,
            messageSendPriorityLow,
            messageSendPriorityNormal,
            messageSendPriorityHigh,
            messageSendPriorityHighest,
    
            commandsIgnoreCancelled,
            commandsUsePriorityLowest,
            commandsUsePriorityLow,
            commandsUsePriorityNormal,
            commandsUsePriorityHigh,
            commandsUsePriorityHighest,

            columnCommandsIgnoreCancelled,
            columnCommandsUsePriorityLowest,
            columnCommandsUsePriorityLow,
            columnCommandsUsePriorityNormal,
            columnCommandsUsePriorityHigh,
            columnCommandsUsePriorityHighest,
            
            isMessageSendDenySoundEnabled,
            isCommandUseDenySoundEnabled,
            isColumnCommandUseDenySoundEnabled,

            isMessageSendDenyTitleEnabled,
            isCommandUseDenyTitleEnabled,
            isColumnCommandUseDenyTitleEnabled;

    @Getter
    private Sound
            messageSendDenySound,
            commandUseDenySound,
            columnCommandUseDenySound;

    @Getter
    private float
            messageSendSoundVolume,
            messageSendSoundPitch,

            commandUseSoundVolume,
            commandUseSoundPitch,

            columnCommandUseSoundVolume,
            columnCommandUseSoundPitch;

    @Getter
    private int
            messageSendTitleFadeIn,
            messageSendTitleStay,
            messageSendTitleFadeOut,

            commandUseTitleFadeIn,
            commandUseTitleStay,
            commandUseTitleFadeOut,

            columnCommandUseTitleFadeIn,
            columnCommandUseTitleStay,
            columnCommandUseTitleFadeOut;

    @Getter
    private Title.Times
            messageSendTitleTimes,
            commandUseTitleTimes;

    @Getter
    private Title columnCommandUseTitle;

    @Getter
    private final Set<String>
            blockedWordsForChat = new HashSet<>(),
            blockedCommands = new HashSet<>(),
            blockedCommandsWithColumns = new HashSet<>();

    @Getter
    private String
            noPermMessages,
            reloadMessages,
            messageSendDenyMessages,
            commandUseDenyMessages,
            columnCommandUseDenyMessages;

    @Getter
    public String
            messageSendDenyTitle,
            messageSendDenySubTitle,

            commandUseDenyTitle,
            commandUseDenySubTitle;

    @Getter
    public static String
            timeDays,
            timeHours,
            timeMinutes,
            timeSeconds;

    private final NewbieGuard plugin;

    public ConfigValues(NewbieGuard plugin) {
        this.plugin = plugin;
    }

    public void setValues() {
        final FileConfiguration config = plugin.getConfig();
        final ConfigurationSection settings = config.getConfigurationSection("settings");
        final ConfigurationSection messages = config.getConfigurationSection("messages");
        final IColorizer colorizer = getColorizer();

        if (settings != null) {
            needTimePlayedToSendMessages = settings.getInt("chat-use.need-time-played");
            needTimePlayedToUseCommands = settings.getInt("commands-use.need-time-played");

            messageSendCheckEnabled = settings.getBoolean("chat-use.enable");
            commandsSendCheckEnabled = settings.getBoolean("commands-use.enable");
            columnCommandsSendCheckEnabled = settings.getBoolean("column-commands-use.enable");

            setMessagesSendPriority(settings);
            setCommandsUsePriority(settings);
            setColumnCommandsUsePriority(settings);

            setMessageSendDenySound(settings);
            setCommandUseDenySound(settings);
            setColumnCommandUseDenySound(settings);

            setMessageSendDenyTitle(settings);
            setCommandUseDenyTitle(settings);
            setColumnCommandUseDenyTitle(colorizer);

            blockedWordsForChat.clear();
            blockedWordsForChat.addAll(settings.getStringList("chat-use.blocked-words"));
            blockedCommands.clear();
            blockedCommands.addAll(settings.getStringList("commands-use.list"));
        } else {
            plugin.getMyLogger().warning("Failed to load section \"settings\" from file \"config.yml\". Please check your configuration file, or delete it and restart your server!");
            plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/NewbieGuard/issues");
        }

        if (messages != null) {
            noPermMessages = getMessage(messages, "no-perm", colorizer);
            reloadMessages = getMessage(messages, "reload", colorizer);
            messageSendDenyMessages = getMessage(messages, "chat-use-messages.cooldown-message", colorizer);
            commandUseDenyMessages = getMessage(messages, "command-use-messages.cooldown-message", colorizer);
            columnCommandUseDenyMessages = getMessage(messages, "column-command-use-messages.message", colorizer);

            messageSendDenyTitle = colorizer.colorize(messages.getString("chat-use-messages.cooldown-title", "&cError"));
            messageSendDenySubTitle = colorizer.colorize(messages.getString("chat-use-messages.cooldown-subtitle", "&fCheck \"messages.chat-use-messages.cooldown-subtitle\""));
            commandUseDenyTitle = colorizer.colorize(messages.getString("command-use-messages.cooldown-title", "&cError"));
            commandUseDenySubTitle = colorizer.colorize(messages.getString("command-use-messages.cooldown-subtitle", "&fCheck \"messages.blocked-command-use-messages.cooldown-subtitle\""));

            timeDays = colorizer.colorize(messages.getString("time.days", "&cError, check \"messages.time.days\""));
            timeHours = colorizer.colorize(messages.getString("time.hours", "&cError, check \"messages.time.hours\""));
            timeMinutes = colorizer.colorize(messages.getString("time.minutes", "&cError, check \"messages.time.minutes\""));
            timeSeconds = colorizer.colorize(messages.getString("time.seconds", "&cError, check \"messages.time.seconds\""));
        } else {
            plugin.getMyLogger().warning("Failed to load section \"messages\" from file \"config.yml\". Please check your configuration file, or delete it and restart your server!");
            plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/NewbieGuard/issues");
        }
    }

    public void setMessagesSendPriority(ConfigurationSection settings) {
        final String chatPriority = settings.getString("chat-use.listener-priority").toUpperCase();
        switch(chatPriority) {
            case "LOWEST" -> {
                messageSendPriorityLowest = true;
                messageSendPriorityLow = false;
                messageSendPriorityNormal = false;
                messageSendPriorityHigh = false;
                messageSendPriorityHighest = false;
            }
            case "LOW" -> {
                messageSendPriorityLowest = false;
                messageSendPriorityLow = true;
                messageSendPriorityNormal = false;
                messageSendPriorityHigh = false;
                messageSendPriorityHighest = false;
            }
            case "NORMAL" -> {
                messageSendPriorityLowest = false;
                messageSendPriorityLow = false;
                messageSendPriorityNormal = true;
                messageSendPriorityHigh = false;
                messageSendPriorityHighest = false;
            }
            case "HIGH" -> {
                messageSendPriorityLowest = false;
                messageSendPriorityLow = false;
                messageSendPriorityNormal = false;
                messageSendPriorityHigh = true;
                messageSendPriorityHighest = false;
            }
            default -> {
                messageSendPriorityLowest = false;
                messageSendPriorityLow = false;
                messageSendPriorityNormal = false;
                messageSendPriorityHigh = false;
                messageSendPriorityHighest = true;
            }
        }
    }

    public void setCommandsUsePriority(ConfigurationSection settings) {
        final String commandsPriority = settings.getString("commands-use.listener-priority").toUpperCase();
        switch(commandsPriority) {
            case "LOWEST" -> {
                commandsUsePriorityLowest = true;
                commandsUsePriorityLow = false;
                commandsUsePriorityNormal = false;
                commandsUsePriorityHigh = false;
                commandsUsePriorityHighest = false;
            }
            case "LOW" -> {
                commandsUsePriorityLowest = false;
                commandsUsePriorityLow = true;
                commandsUsePriorityNormal = false;
                commandsUsePriorityHigh = false;
                commandsUsePriorityHighest = false;
            }
            case "NORMAL" -> {
                commandsUsePriorityLowest = false;
                commandsUsePriorityLow = false;
                commandsUsePriorityNormal = true;
                commandsUsePriorityHigh = false;
                commandsUsePriorityHighest = false;
            }
            case "HIGH" ->  {
                commandsUsePriorityLowest = false;
                commandsUsePriorityLow = false;
                commandsUsePriorityNormal = false;
                commandsUsePriorityHigh = true;
                commandsUsePriorityHighest = false;
            }
            default ->  {
                commandsUsePriorityLowest = false;
                commandsUsePriorityLow = false;
                commandsUsePriorityNormal = false;
                commandsUsePriorityHigh = false;
                commandsUsePriorityHighest = true;
            }
        }
    }

    public void setColumnCommandsUsePriority(ConfigurationSection settings) {
        final String columnCommandsPriority = settings.getString("column-commands-use.listener-priority").toUpperCase();
        switch(columnCommandsPriority) {
            case "LOWEST" -> {
                columnCommandsUsePriorityLowest = true;
                columnCommandsUsePriorityLow = false;
                columnCommandsUsePriorityNormal = false;
                columnCommandsUsePriorityHigh = false;
                columnCommandsUsePriorityHighest = false;
            }
            case "LOW" -> {
                columnCommandsUsePriorityLowest = false;
                columnCommandsUsePriorityLow = true;
                columnCommandsUsePriorityNormal = false;
                columnCommandsUsePriorityHigh = false;
                columnCommandsUsePriorityHighest = false;
            }
            case "NORMAL" -> {
                columnCommandsUsePriorityLowest = false;
                columnCommandsUsePriorityLow = false;
                columnCommandsUsePriorityNormal = true;
                columnCommandsUsePriorityHigh = false;
                columnCommandsUsePriorityHighest = false;
            }
            case "HIGH" -> {
                columnCommandsUsePriorityLowest = false;
                columnCommandsUsePriorityLow = false;
                columnCommandsUsePriorityNormal = false;
                columnCommandsUsePriorityHigh = true;
                columnCommandsUsePriorityHighest = false;
            }
            default -> {
                columnCommandsUsePriorityLowest = false;
                columnCommandsUsePriorityLow = false;
                columnCommandsUsePriorityNormal = false;
                columnCommandsUsePriorityHigh = false;
                columnCommandsUsePriorityHighest = true;
            }
        }
    }

    public void setMessageSendDenySound(ConfigurationSection settings) {
        final String soundString = settings.getString("chat-use.deny-sound");
        if (soundString == null) {
            plugin.getMyLogger().warning("Failed to load sound \"settings.chat-use.deny-sound\" from file \"config.yml\". Please check your configuration file, or delete it and restart your server!");
            plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/NewbieGuard/issues");
            isMessageSendDenySoundEnabled = false;
        } else if (soundString.equalsIgnoreCase("disabled")) {
            isMessageSendDenySoundEnabled = false;
        } else {
            isMessageSendDenySoundEnabled = true;
            final String[] params = soundString.split(";");
            messageSendDenySound = params.length == 1 && params[0] != null ? Sound.valueOf(params[0].toUpperCase(Locale.ENGLISH)) : Sound.BLOCK_BREWING_STAND_BREW;
            messageSendSoundVolume = params.length == 2 && params[1] != null ? Float.parseFloat(params[1]) : 1.0f;
            messageSendSoundPitch = params.length == 3 && params[2] != null ? Float.parseFloat(params[2]) : 1.0f;
        }
    }

    public void setCommandUseDenySound(ConfigurationSection settings) {
        final String soundString = settings.getString("commands-use.deny-sound");
        if (soundString == null) {
            plugin.getMyLogger().warning("Failed to load sound \"settings.commands-use.deny-sound\" from file \"config.yml\". Please check your configuration file, or delete it and restart your server!");
            plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/NewbieGuard/issues");
            isCommandUseDenySoundEnabled = false;
        } else if (soundString.equalsIgnoreCase("disabled")) {
            isCommandUseDenySoundEnabled = false;
        } else {
            isCommandUseDenySoundEnabled = true;
            final String[] params = soundString.split(";");
            commandUseDenySound = params.length == 1 && params[0] != null ? Sound.valueOf(params[0].toUpperCase(Locale.ENGLISH)) : Sound.BLOCK_BREWING_STAND_BREW;
            commandUseSoundVolume = params.length == 2 && params[1] != null ? Float.parseFloat(params[1]) : 1.0f;
            commandUseSoundPitch = params.length == 3 && params[2] != null ? Float.parseFloat(params[2]) : 1.0f;
        }
    }

    public void setColumnCommandUseDenySound(ConfigurationSection settings) {
        final String soundString = settings.getString("column-commands-use.deny-sound");
        if (soundString == null) {
            plugin.getMyLogger().warning("Failed to load sound \"settings.column-commands-use.deny-sound\" from file \"config.yml\". Please check your configuration file, or delete it and restart your server!");
            plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/NewbieGuard/issues");
            isColumnCommandUseDenySoundEnabled = false;
        } else if (soundString.equalsIgnoreCase("disabled")) {
            isColumnCommandUseDenySoundEnabled = false;
        } else {
            isColumnCommandUseDenySoundEnabled = true;
            final String[] params = soundString.split(";");
            columnCommandUseDenySound = params.length == 1 && params[0] != null ? Sound.valueOf(params[0].toUpperCase(Locale.ENGLISH)) : Sound.BLOCK_BREWING_STAND_BREW;
            columnCommandUseSoundVolume = params.length == 2 && params[1] != null ? Float.parseFloat(params[1]) : 1.0f;
            columnCommandUseSoundPitch = params.length == 3 && params[2] != null ? Float.parseFloat(params[2]) : 1.0f;
        }
    }

    public void setMessageSendDenyTitle(ConfigurationSection settings) {
        final String titleString = settings.getString("chat-use.deny-title");
        if (titleString == null) {
            plugin.getMyLogger().warning("Failed to load title \"settings.chat-use.deny-title\" from file \"config.yml\". Please check your configuration file, or delete it and restart your server!");
            plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/NewbieGuard/issues");
            isMessageSendDenyTitleEnabled = false;
        } else if (titleString.equalsIgnoreCase("disabled")) {
            isMessageSendDenyTitleEnabled = false;
        } else {
            isMessageSendDenyTitleEnabled = true;
            final String[] params = titleString.split(";");
            final int fadeIn = params.length == 1 && params[0] != null ? Integer.parseInt(params[0]) : 10;
            final int stay = params.length == 2 && params[1] != null ? Integer.parseInt(params[1]) : 40;
            final int fadeOut = params.length == 3 && params[2] != null ? Integer.parseInt(params[2]) : 20;
            messageSendTitleTimes = Title.Times.of(Ticks.duration(fadeIn), Ticks.duration(stay), Ticks.duration(fadeOut));
        }
    }

    public void setCommandUseDenyTitle(ConfigurationSection settings) {
        final String titleString = settings.getString("commands-use.deny-title");
        if (titleString == null) {
            plugin.getMyLogger().warning("Failed to load title \"settings.commands-use.deny-title\" from file \"config.yml\". Please check your configuration file, or delete it and restart your server!");
            plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/NewbieGuard/issues");
            isCommandUseDenyTitleEnabled = false;
        } else if (titleString.equalsIgnoreCase("disabled")) {
            isCommandUseDenyTitleEnabled = false;
        } else {
            isCommandUseDenyTitleEnabled = true;
            final String[] params = titleString.split(";");
            final int fadeIn = params.length == 1 && params[0] != null ? Integer.parseInt(params[0]) : 10;
            final int stay = params.length == 2 && params[1] != null ? Integer.parseInt(params[1]) : 40;
            final int fadeOut = params.length == 3 && params[2] != null ? Integer.parseInt(params[2]) : 20;
            commandUseTitleTimes = Title.Times.of(Ticks.duration(fadeIn), Ticks.duration(stay), Ticks.duration(fadeOut));
        }
    }

    public void setColumnCommandUseDenyTitle(IColorizer colorizer) {
        final String titleString = plugin.getConfig().getString("settings.column-commands-use.deny-title");
        if (titleString == null) {
            plugin.getMyLogger().warning("Failed to load title \"settings.column-commands-use.deny-title\" from file \"config.yml\". Please check your configuration file, or delete it and restart your server!");
            plugin.getMyLogger().warning("If you think this is a plugin error, leave a issue on the https://github.com/grounbreakingmc/NewbieGuard/issues");
            isColumnCommandUseDenyTitleEnabled = false;
        } else if (titleString.equalsIgnoreCase("disabled")) {
            isColumnCommandUseDenyTitleEnabled = false;
        } else {
            isColumnCommandUseDenyTitleEnabled = true;
            final String[] params = titleString.split(";");
            final int fadeIn = params.length == 1 && params[0] != null ? Integer.parseInt(params[0]) : 10;
            final int stay = params.length == 2 && params[1] != null ? Integer.parseInt(params[1]) : 40;
            final int fadeOut = params.length == 3 && params[2] != null ? Integer.parseInt(params[2]) : 20;
            final String columnCommandUseDenyTitle = colorizer.colorize(
                    plugin.getConfig().getString("messages.blocked-command-use-messages.title", "&cError")
            );
            final String columnCommandUseDenySubTitle = colorizer.colorize(
                    plugin.getConfig().getString("messages.blocked-command-use-messages.subtitle", "&fCheck \"messages.blocked-command-use-messages.cooldown-subtitle\"")
            );
            final Title.Times columnCommandUseTitleTimes = Title.Times.of(Ticks.duration(fadeIn), Ticks.duration(stay), Ticks.duration(fadeOut));
            columnCommandUseTitle = Title.title(Component.text(columnCommandUseDenyTitle), Component.text(columnCommandUseDenySubTitle), columnCommandUseTitleTimes);
        }
    }

    public String getMessage(ConfigurationSection section, String path, IColorizer colorizer) {
        final String message = section.getString(path, "&4(!) &cFailed to get message on path: " + path);
        return colorizer.colorize(message);
    }

    public IColorizer getColorizer() {
        final boolean useMiniMessages = plugin.getConfig().getBoolean("use-minimessage");
        final boolean is16OrAbove = new ServerInfo(plugin).getSubVersion() >= 16;

        return useMiniMessages
                ? new MiniMessagesColorizer()
                : is16OrAbove
                ? new LegacyColorizer()
                : new VanillaColorizer();
    }
}