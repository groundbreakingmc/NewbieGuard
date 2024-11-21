package com.github.groundbreakingmc.newbieguard.constructors;

import com.github.groundbreakingmc.newbieguard.utils.commands.IMode;
import com.github.groundbreakingmc.newbieguard.utils.time.ITimeCounter;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor @Getter @Builder
public class CommandGroup {
    private final String sectionName;
    private final ITimeCounter timeCounter;
    private final IMode mode;
    private final int requiredTime;
    private final String cooldownMessage;
    private final boolean isDenySoundEnabled;
    private final Sound denySound;
    private final float denySoundVolume;
    private final float denySoundPitch;
    private final boolean isDenyTitleEnabled;
    private final Component denyTitle;
    private final Component denySubtitle;
    private final Title.Times denyTitleTimes;

    @Getter(AccessLevel.NONE)
    public final Map<UUID, Long> players = new HashMap<>();
}
