package groundbreaking.newbieguard.utils.commands;

import java.util.Set;

public interface IMode {

    boolean check(Set<String> blockedCommands, String sentCommand);
}
