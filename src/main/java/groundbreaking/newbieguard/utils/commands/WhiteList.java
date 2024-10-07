package groundbreaking.newbieguard.utils.commands;

import groundbreaking.newbieguard.utils.CommandChecker;

public final class WhiteList implements IMode {

    @Override
    public boolean check(String sentCommand, String blockedCommand) {
        return CommandChecker.isBlocked(sentCommand, blockedCommand);
    }
}
