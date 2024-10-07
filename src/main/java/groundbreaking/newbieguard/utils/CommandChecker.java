package groundbreaking.newbieguard.utils;

public final class CommandChecker {

    public static boolean isBlocked(final String sentCommand, final String blockedCommand) {
        if (sentCommand.equalsIgnoreCase(blockedCommand)) {
            return true;
        }

        final int length = Math.min(sentCommand.length(), blockedCommand.length());

        for (int i = 0; i < length; i++) {
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
