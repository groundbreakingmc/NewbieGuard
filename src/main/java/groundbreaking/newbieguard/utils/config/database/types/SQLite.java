package groundbreaking.newbieguard.utils.config.database.types;

import groundbreaking.newbieguard.utils.config.database.AbstractDB;
import org.bukkit.entity.Player;

import java.sql.*;

public final class SQLite extends AbstractDB {

    public SQLite(String url) {
        super(url, null, null);
    }

    @Override
    public void createConnection() throws SQLException {
        try (Connection connection = getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("""
                        CREATE TABLE IF NOT EXISTS chat (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            username TEXT NOT NULL UNIQUE
                        )
                    """);

            statement.execute("""
                        CREATE TABLE IF NOT EXISTS commands (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            username TEXT NOT NULL UNIQUE
                        )
                    """);
        }
    }

    @Override
    public void addPlayerChatDatabase(Player p) {
        if (p == null) {
            return;
        }

        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement("INSERT INTO chat (username) VALUES (?)")) {
            statement.setString(1, p.getName());
            statement.executeUpdate();
        }
        catch (SQLException ex) {
            if (ex.getErrorCode() == 19) {
                return;
            }
            ex.printStackTrace();
        }
    }

    @Override
    public void addPlayerCommandsDatabase(Player p) {
        if (p == null) {
            return;
        }

        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement("INSERT INTO commands (username) VALUES (?)")) {
            statement.setString(1, p.getName());
            statement.executeUpdate();
        }
        catch (SQLException ex) {
            if (ex.getErrorCode() == 19) {
                return;
            }
            ex.printStackTrace();
        }
    }

    @Override
    public boolean chatDatabaseHasPlayer(Player p) {
        if (p == null) {
            return false;
        }

        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM chat WHERE username = ?")) {
            statement.setString(1, p.getName());
            ResultSet result = statement.executeQuery();
            return result.next();
        }
        catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean commandsDatabaseHasPlayer(Player p) {
        if (p == null) {
            return false;
        }

        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM commands WHERE username = ?")) {
            statement.setString(1, p.getName());
            ResultSet result = statement.executeQuery();
            return result.next();
        }
        catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}