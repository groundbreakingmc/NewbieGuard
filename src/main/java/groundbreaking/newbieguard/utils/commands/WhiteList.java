package groundbreaking.newbieguard.utils.commands;

import groundbreaking.newbieguard.constructors.CommandGroup;

public final class WhiteList implements IMode {

    @Override
    public boolean check(final CommandGroup commandGroup) {
        return commandGroup != null;
    }
}
