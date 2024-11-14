package groundbreaking.newbieguard.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.UUID;

public abstract class DatabaseHandler {

    protected HikariDataSource dataSource;

    protected DatabaseHandler(final String jdbcUrl, final String user, final String password) {
        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        if (user != null) {
            config.setUsername(user);
        }
        if (password != null) {
            config.setPassword(password);
        }
        config.setMaximumPoolSize(15);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        this.dataSource = new HikariDataSource(config);
    }

    public void createConnection() throws SQLException {
        final String chatTable = "CREATE TABLE IF NOT EXISTS chat (username TEXT NOT NULL UNIQUE)";
        final String commandsTable = "CREATE TABLE IF NOT EXISTS commands (username TEXT NOT NULL UNIQUE)";
        try (final Statement statement = getConnection().createStatement()) {
            statement.execute(chatTable);
            statement.execute(commandsTable);
        }
    }

    public abstract void clear();

    protected void clear(final String clearChat, final String clearCommands) {
        try {
            try (final Statement statement = getConnection().createStatement()) {
                statement.execute(clearChat);
                statement.execute(clearCommands);
            }
        } catch(final SQLException ex) {
            ex.printStackTrace();
        }
    }

    public abstract void addPlayerToChatTable(UUID playerUUID);

    protected void addPlayerToChatTable(final UUID playerUUID, final String sqlQuery) {
        try (final PreparedStatement statement = getConnection().prepareStatement(sqlQuery)) {
            statement.setString(1, playerUUID.toString());
            statement.executeUpdate();
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void removePlayerFromChatTable(final UUID playerUUID) {
        final String sqlQuery = "DELETE FROM chat WHERE username = ?;";
        try (final PreparedStatement statement = getConnection().prepareStatement(sqlQuery)) {
            statement.setString(1, playerUUID.toString());
            statement.executeUpdate();
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }

    public abstract void addPlayerToCommandsDatabase(UUID playerUUID);

    public void addPlayerToCommandsDatabase(final UUID playerUUID, final String sqlQuery) {
        try (final PreparedStatement statement = getConnection().prepareStatement(sqlQuery)) {
            statement.setString(1, playerUUID.toString());
            statement.executeUpdate();
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void removePlayerFromCommandsTable(final UUID playerUUID) {
        final String sqlQuery = "DELETE FROM commands WHERE username = ?;";
        try (final PreparedStatement statement = getConnection().prepareStatement(sqlQuery)) {
            statement.setString(1, playerUUID.toString());
            statement.executeUpdate();
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }

    public boolean chatDatabaseHasPlayer(final UUID playerUUID) {
        final String sqlQuery = "SELECT 1 FROM commands WHERE username = ?;";
        try (final PreparedStatement statement = getConnection().prepareStatement(sqlQuery)) {
            statement.setString(1, playerUUID.toString());
            try (final ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        } catch (final SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean commandsDatabaseHasPlayer(final UUID playerUUID) {
        final String sqlQuery = "SELECT EXISTS(SELECT 1 FROM chat WHERE username = ?);";
        try (final PreparedStatement statement = getConnection().prepareStatement(sqlQuery)) {
            statement.setString(1, playerUUID.toString());
            try (final ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        } catch (final SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    protected final Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    public final void close() {
        try {
            if (this.dataSource != null && dataSource.getConnection() != null) {
                dataSource.close();
            }
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }
}
