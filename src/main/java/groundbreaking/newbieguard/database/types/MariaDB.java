package groundbreaking.newbieguard.database.types;

import groundbreaking.newbieguard.database.DatabaseHandler;

public final class MariaDB extends DatabaseHandler {

    public MariaDB(final String url, final String user, final String password) {
        super("jdbc:mariadb://" + url, user, password);
        super.setAddPlayerToChat("INSERT OR IGNORE INTO chat (username) VALUES (?);");
        super.setAddPlayerToCommands("INSERT OR IGNORE INTO commands (username) VALUES (?);");
    }

    public void clearTables() {
        final String clearChat = "TRUNCATE TABLE chat";
        final String clearCommands = "TRUNCATE TABLE commands";
        super.clearTables(clearChat, clearCommands);
    }
}
