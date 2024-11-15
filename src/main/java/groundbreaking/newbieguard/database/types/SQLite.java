package groundbreaking.newbieguard.database.types;

import groundbreaking.newbieguard.database.DatabaseHandler;

public final class SQLite extends DatabaseHandler {

    public SQLite(String url) {
        super(url, null, null);
        super.setAddPlayerToChat("INSERT OR IGNORE INTO chat (username) VALUES (?);");
        super.setAddPlayerToCommands("INSERT OR IGNORE INTO commands (username) VALUES (?);");
    }

    public void clearTables() {
        final String clearChat = "DELETE FROM chat";
        final String clearCommands = "DELETE FROM commands";
        super.clearTables(clearChat, clearCommands);
    }
}