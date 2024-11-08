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

    public void addPlayerChatDatabase(final Player player) {
        final String sqlQuery = "INSERT INTO chat (username) VALUES (?)";
        try (final PreparedStatement statement = getConnection().prepareStatement(sqlQuery)) {
            statement.setString(1, player.getName());
            statement.executeUpdate();
        } catch (final SQLException ex) {
            if (ex.getErrorCode() != 19) {
                ex.printStackTrace();
            }
        }
    }

    public void addPlayerCommandsDatabase(final Player player) {
        final String sqlQuery = "INSERT INTO commands (username) VALUES (?)";
        try (final PreparedStatement statement = getConnection().prepareStatement(sqlQuery)) {
            statement.setString(1, player.getName());
            statement.executeUpdate();
        } catch (final SQLException ex) {
            if (ex.getErrorCode() != 19) {
                ex.printStackTrace();
            }
        }
    }

    public boolean chatDatabaseHasPlayer(final Player player) {
        final String sqlQuery = "SELECT * FROM commands WHERE username = ?";
        try (final PreparedStatement statement = getConnection().prepareStatement(sqlQuery)) {
            statement.setString(1, player.getName());
            final ResultSet result = statement.executeQuery();
            return result.next();
        } catch (final SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean commandsDatabaseHasPlayer(final Player player) {
        final String sqlQuery = "SELECT * FROM chat WHERE username = ?";
        try (final PreparedStatement statement = getConnection().prepareStatement(sqlQuery)) {
            statement.setString(1, player.getName());
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