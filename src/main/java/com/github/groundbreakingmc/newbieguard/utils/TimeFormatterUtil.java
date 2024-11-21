package com.github.groundbreakingmc.newbieguard.utils;

import com.github.groundbreakingmc.newbieguard.utils.config.ConfigValues;

public final class TimeFormatterUtil {

    private TimeFormatterUtil() {

    }

    public static String getTime(long totalSeconds) {
        final long days = totalSeconds / 86400;
        final long hours = (totalSeconds / 3600) % 24;
        final long minutes = (totalSeconds / 60) % 60;
        final long seconds = totalSeconds % 60;

        final StringBuilder formattedTime = new StringBuilder();
        if (days > 0) {
            formattedTime.append(days).append(ConfigValues.getTimeDays());
        }

        boolean lengthMoreThenZero = formattedTime.length() > 0;
        if (hours > 0 || lengthMoreThenZero) {
            formattedTime.append(hours).append(ConfigValues.getTimeHours());
        }

        if (minutes > 0 || lengthMoreThenZero) {
            formattedTime.append(minutes).append(ConfigValues.getTimeMinutes());
        }

        formattedTime.append(seconds).append(ConfigValues.getTimeSeconds());

        return formattedTime.toString();
    }
}