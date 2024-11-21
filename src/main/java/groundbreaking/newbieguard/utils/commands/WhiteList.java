package groundbreaking.newbieguard.utils.commands;

import java.util.Set;

public final class WhiteList implements IMode {

    @Override
    public boolean check(final Set<String> blockedCommands, final String sentCommand) {
        return !blockedCommands.contains(sentCommand);
    }
}
