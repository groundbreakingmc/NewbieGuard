package groundbreaking.newbieguard.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.sql.*;
import java.util.UUID;

public abstract class DatabaseHandler {

    private final HikariDataSource dataSource;

    @Getter @Setter(AccessLevel.PROTECTED)
    private String addPlayerToChat;
    @Getter @Setter(AccessLevel.PROTECTED)
    private String addPlayerToCommands;

    public static final String REMOVE_PLAYER_FROM_CHAT = "DELETE FROM chat WHERE username = ?;";
    public static final String REMOVE_PLAYER_FROM_COMMANDS = "DELETE FROM commands WHERE username = ?;";
    public static final String CHECK_IF_PLAYER_IN_CHAT_TABLE = "SELECT EXISTS(SELECT 1 FROM chat WHERE username = ?);";
    public static final String CHECK_IF_PLAYER_IN_COMMANDS_TABLE = "SELECT EXISTS(SELECT 1 FROM commands WHERE username = ?);";

    protected DatabaseHandler(final String jdbcUrl, final String user, final String password) {
        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        if (user != null) {
            config.setUsername(user);
        }
        if (password != null) {
            config.setPassword(password);
        }
        config.setMinimumIdle(4);
        config.setMaximumPoolSize(12);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        this.dataSource = new HikariDataSource(config);
    }

    public void createTables() throws SQLException {
        final String chatTable = "CREATE TABLE IF NOT EXISTS chat (username TEXT NOT NULL UNIQUE)";
        final String commandsTable = "CREATE TABLE IF NOT EXISTS commands (username TEXT NOT NULL UNIQUE)";
        try (final Connection connection = this.getConnection(); final Statement statement = connection.createStatement()) {
            statement.execute(chatTable);
            statement.execute(commandsTable);
        }
    }

    public abstract void clearTables();

    protected void clearTables(final String clearChat, final String clearCommands) {
        try (final Connection connection = this.getConnection(); final Statement statement = connection.createStatement()) {
            statement.execute(clearChat);
            statement.execute(clearCommands);
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void executeUpdateQuery(final UUID playerUUID, final String query) {
        try (final Connection connection = this.getConnection()) {
            connection.setAutoCommit(false);
            try (final PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, playerUUID.toString());
                statement.executeUpdate();
                connection.commit();
            } catch (final SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }

    public boolean containsPlayerInTable(final Connection connection, final UUID playerUUID, final String query) throws SQLException {
        try (final PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, playerUUID.toString());
            try (final ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public final Connection getConnection() throws SQLException {
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
