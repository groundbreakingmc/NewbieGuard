package groundbreaking.newbieguard.database.types;

import groundbreaking.newbieguard.database.DatabaseHandler;

import java.sql.SQLException;
import java.sql.Statement;

public final class SQLite extends DatabaseHandler {

    public SQLite(String url) {
        super(url, null, null);
    }

    @Override
    public void createConnection() throws SQLException {
        final String chatTable = "CREATE TABLE IF NOT EXISTS chat (username TEXT NOT NULL UNIQUE)";
        final String commandsTable = "CREATE TABLE IF NOT EXISTS commands (username TEXT NOT NULL UNIQUE)";
        try (final Statement statement = getConnection().createStatement()) {
            statement.execute(chatTable);
            statement.execute(commandsTable);
        }
    }
}