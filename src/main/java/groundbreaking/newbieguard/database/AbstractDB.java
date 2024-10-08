package groundbreaking.newbieguard.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class AbstractDB {

    protected HikariDataSource dataSource;

    public AbstractDB(String jdbcUrl, String user, String password) {
        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        if (user != null && !user.isEmpty()) {
            config.setUsername(user);
        }
        if (password != null && !password.isEmpty()) {
            config.setPassword(password);
        }
        config.setMaximumPoolSize(30);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        dataSource = new HikariDataSource(config);
    }

    public abstract void createConnection() throws SQLException;

    public abstract void addPlayerChatDatabase(Player p);

    public abstract void addPlayerCommandsDatabase(Player p);

    public abstract boolean chatDatabaseHasPlayer(Player p);

    public abstract boolean commandsDatabaseHasPlayer(Player p);

    protected Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null) {
            try {
                if (dataSource.getConnection() != null) {
                    dataSource.close();
                }
            }
            catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}