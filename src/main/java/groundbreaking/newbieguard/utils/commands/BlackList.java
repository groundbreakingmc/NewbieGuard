package groundbreaking.newbieguard.utils.commands;

import groundbreaking.newbieguard.utils.CommandCheckerUtil;

public final class BlackList implements IMode {

    @Override
    public boolean check(String sentCommand, String blockedCommand) {
        return CommandCheckerUtil.matchesBlockedCommand(sentCommand, blockedCommand);
    }
}
