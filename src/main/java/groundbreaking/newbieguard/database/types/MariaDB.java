package groundbreaking.newbieguard.database.types;

import groundbreaking.newbieguard.database.DatabaseHandler;

import java.sql.SQLException;
import java.sql.Statement;

public final class MariaDB extends DatabaseHandler {

    public MariaDB(final String url, final String user, final String password) {
        super("jdbc:mariadb://" + url, user, password);
    }

    @Override
    public void createConnection() throws SQLException {
        final String chatTable = "CREATE TABLE IF NOT EXISTS chat (username VARCHAR(255) NOT NULL UNIQUE)";
        final String commandsTable = "CREATE TABLE IF NOT EXISTS commands (username VARCHAR(255) NOT NULL UNIQUE)";
        try (final Statement statement = getConnection().createStatement()) {
            statement.execute(chatTable);
            statement.execute(commandsTable);
        }
    }

    @Override
    public void clear() {
        try {
            final String chatTable = "TRUNCATE FROM chat";
            final String commandsTable = "TRUNCATE FROM commands";
            try (final Statement statement = getConnection().createStatement()) {
                statement.execute(chatTable);
                statement.execute(commandsTable);
            }
        } catch(final SQLException ex) {
            ex.printStackTrace();
        }
    }
}
