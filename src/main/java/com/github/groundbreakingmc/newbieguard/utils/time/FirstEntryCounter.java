package com.github.groundbreakingmc.newbieguard.utils.time;

import org.bukkit.entity.Player;

public final class FirstEntryCounter implements ITimeCounter {

    @Override
    public long count(Player player) {
        return (System.currentTimeMillis() - player.getFirstPlayed()) / 1000;
    }

}
