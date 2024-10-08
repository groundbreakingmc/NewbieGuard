package groundbreaking.newbieguard.utils.commands;

import groundbreaking.newbieguard.utils.CommandChecker;

public final class BlackList implements IMode {

    @Override
    public boolean check(String sentCommand, String blockedCommand) {
        return CommandChecker.isBlocked(sentCommand, blockedCommand);
    }
}
