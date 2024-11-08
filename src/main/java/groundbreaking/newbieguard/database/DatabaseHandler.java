package groundbreaking.newbieguard.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class DatabaseHandler {

    protected HikariDataSource dataSource;

    public DatabaseHandler(final String jdbcUrl, final String user, final String password) {
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

    public abstract void createConnection() throws SQLException;

    public abstract void clear();

    public void addPlayerToChatTable(final String playerName) {
        final String sqlQuery = "INSERT OR IGNORE INTO chat (username) VALUES (?)";
        try (final PreparedStatement statement = getConnection().prepareStatement(sqlQuery)) {
            statement.setString(1, playerName);
            statement.executeUpdate();
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void removePlayerFromChatTable(final String playerName) {
        final String sqlQuery = "DELETE FROM chat WHERE username = ? LIMIT 1";
        try (final PreparedStatement statement = getConnection().prepareStatement(sqlQuery)) {
            statement.setString(1, playerName);
            statement.executeUpdate();
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void addPlayerCommandsDatabase(final String playerName) {
        final String sqlQuery = "INSERT OR IGNORE INTO commands (username) VALUES (?)";
        try (final PreparedStatement statement = getConnection().prepareStatement(sqlQuery)) {
            statement.setString(1, playerName);
            statement.executeUpdate();
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void removePlayerFromCommandsTable(final String playerName) {
        final String sqlQuery = "DELETE FROM chat WHERE commands = ? LIMIT 1";
        try (final PreparedStatement statement = getConnection().prepareStatement(sqlQuery)) {
            statement.setString(1, playerName);
            statement.executeUpdate();
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }

    public boolean chatDatabaseHasPlayer(final String playerName) {
        final String sqlQuery = "SELECT 1 FROM commands WHERE username = ? LIMIT 1";
        try (final PreparedStatement statement = getConnection().prepareStatement(sqlQuery)) {
            statement.setString(1, playerName);
            final ResultSet result = statement.executeQuery();
            return result.next();
        } catch (final SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean commandsDatabaseHasPlayer(final String playerName) {
        final String sqlQuery = "SELECT 1 FROM chat WHERE username = ? LIMIT 1";
        try (final PreparedStatement statement = getConnection().prepareStatement(sqlQuery)) {
            statement.setString(1, playerName);
            final ResultSet result = statement.executeQuery();
            return result.next();
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