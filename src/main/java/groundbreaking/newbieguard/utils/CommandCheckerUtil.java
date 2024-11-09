package groundbreaking.newbieguard.utils;

public final class CommandCheckerUtil {

    private CommandCheckerUtil() {

    }

    public static boolean matchesBlockedCommand(final String sentCommand, final String blockedCommand) {
        if (sentCommand.equalsIgnoreCase(blockedCommand)) {
            return true;
        }

        final int sentCommandSpaceIndex = sentCommand.indexOf(' ');
        final int blockedCommandLength = blockedCommand.length();
        if (sentCommandSpaceIndex == -1 || sentCommandSpaceIndex != blockedCommandLength) {
            return false;
        }

        for (int i = 0; i <= blockedCommandLength; i++) {
            final char currentChar = sentCommand.charAt(i);
            if (currentChar == ' ') {
                return true;
            }
            if (currentChar != blockedCommand.charAt(i)) {
                return false;
            }
        }

        return true;
    }
}
