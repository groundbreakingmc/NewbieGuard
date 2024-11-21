package com.github.groundbreakingmc.newbieguard.utils.commands;

import com.github.groundbreakingmc.newbieguard.constructors.CommandGroup;

public final class WhiteList implements IMode {

    @Override
    public boolean check(final CommandGroup commandGroup) {
        return commandGroup != null;
    }
}
