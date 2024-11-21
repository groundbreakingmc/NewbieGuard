package com.github.groundbreakingmc.newbieguard.utils.logging;

import com.github.groundbreakingmc.newbieguard.NewbieGuard;

import java.util.logging.Logger;

public final class BukkitLogger implements ILogger {

    private final Logger logger;

    public BukkitLogger(final NewbieGuard plugin) {
        logger = plugin.getLogger();
    }

    public void info(final String msg) {
        logger.info(msg);
    }

    public void warning(final String msg) {
        logger.warning(msg);
    }
}
