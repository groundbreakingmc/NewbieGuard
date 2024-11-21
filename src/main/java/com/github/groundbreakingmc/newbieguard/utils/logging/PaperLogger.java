package com.github.groundbreakingmc.newbieguard.utils.logging;

import com.github.groundbreakingmc.newbieguard.NewbieGuard;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class PaperLogger implements ILogger {

    private final ComponentLogger logger;
    private final LegacyComponentSerializer legacySection;

    public PaperLogger(final NewbieGuard plugin) {
        logger = ComponentLogger.logger(plugin.getLogger().getName());
        legacySection = LegacyComponentSerializer.legacySection();
    }

    public void info(final String msg) {
        logger.info(legacySection.deserialize(msg));
    }

    public void warning(final String msg) {
        logger.warn(legacySection.deserialize(msg));
    }
}
