package groundbreaking.newbieguard.database.types;

import groundbreaking.newbieguard.database.DatabaseHandler;

import java.util.UUID;

public final class MariaDB extends DatabaseHandler {

    public MariaDB(final String url, final String user, final String password) {
        super("jdbc:mariadb://" + url, user, password);
    }

    public void addPlayerToChatTable(final UUID playerUUID) {
        final String addToChat = "INSERT OR IGNORE INTO chat (username) VALUES (?);";
        super.addPlayerToChatTable(playerUUID, addToChat);
    }

    public void addPlayerToCommandsDatabase(final UUID playerUUID) {
        final String addToCommands = "INSERT OR IGNORE INTO commands (username) VALUES (?);";
        super.addPlayerToCommandsDatabase(playerUUID, addToCommands);
    }

    public void clear() {
        final String clearChat = "TRUNCATE TABLE chat";
        final String clearCommands = "TRUNCATE TABLE commands";
        super.clear(clearChat, clearCommands);
    }
}
