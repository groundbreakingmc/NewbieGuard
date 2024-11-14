package groundbreaking.newbieguard.database.types;

import groundbreaking.newbieguard.database.DatabaseHandler;

import java.util.UUID;

public final class SQLite extends DatabaseHandler {

    public SQLite(String url) {
        super(url, null, null);
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
        final String clearChat = "DELETE FROM chat";
        final String clearCommands = "DELETE FROM commands";
        super.clear(clearChat, clearCommands);
    }
}