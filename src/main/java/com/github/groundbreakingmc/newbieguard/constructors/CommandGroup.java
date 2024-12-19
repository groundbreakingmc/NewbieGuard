package com.github.groundbreakingmc.newbieguard.constructors;

import com.github.groundbreakingmc.newbieguard.utils.commands.IMode;
import com.github.groundbreakingmc.newbieguard.utils.time.ITimeCounter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;
import org.bukkit.permissions.Permission;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor @Builder
public final class CommandGroup {
    public final String bypassPermission;
    public final ITimeCounter timeCounter;
    public final IMode mode;
    public final int requiredTime;
    public final String cooldownMessage;
    public final boolean isDenySoundEnabled;
    public final Sound denySound;
    public final float denySoundVolume;
    public final float denySoundPitch;
    public final boolean isDenyTitleEnabled;
    public final Component denyTitle;
    public final Component denySubtitle;
    public final Title.Times denyTitleTimes;
    public final Map<UUID, Long> players = new HashMap<>();
}
