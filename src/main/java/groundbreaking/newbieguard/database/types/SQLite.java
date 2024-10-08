package groundbreaking.newbieguard.database.types;

import groundbreaking.newbieguard.database.AbstractDB;
import org.bukkit.entity.Player;

import java.sql.*;

public final class SQLite extends AbstractDB {

    public SQLite(String url) {
        super(url, null, null);
    }

    @Override
    public void createConnection() throws SQLException {
        try (final Statement statement = getConnection().createStatement()) {
            statement.execute("""
                        CREATE TABLE IF NOT EXISTS chat (
                            username TEXT NOT NULL UNIQUE
                        )
                    """);

            statement.execute("""
                        CREATE TABLE IF NOT EXISTS commands (
                            username TEXT NOT NULL UNIQUE
                        )
                    """);
        }
    }

    @Override
    public void addPlayerChatDatabase(final Player player) {
        if (player == null) {
            return;
        }

        try (final PreparedStatement statement = getConnection().prepareStatement("INSERT INTO chat (username) VALUES (?)")) {
            statement.setString(1, player.getName());
            statement.executeUpdate();
        }
        catch (final SQLException ex) {
            if (ex.getErrorCode() == 19) {
                return;
            }
            ex.printStackTrace();
        }
    }

    @Override
    public void addPlayerCommandsDatabase(final Player player) {
        if (player == null) {
            return;
        }

        try (final PreparedStatement statement = getConnection().prepareStatement("INSERT INTO commands (username) VALUES (?)")) {
            statement.setString(1, player.getName());
            statement.executeUpdate();
        }
        catch (final SQLException ex) {
            if (ex.getErrorCode() == 19) {
                return;
            }
            ex.printStackTrace();
        }
    }

    @Override
    public boolean chatDatabaseHasPlayer(final Player player) {
        if (player == null) {
            return false;
        }

        try (final PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM chat WHERE username = ?")) {
            statement.setString(1, player.getName());
            ResultSet result = statement.executeQuery();
            return result.next();
        }
        catch (final SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean commandsDatabaseHasPlayer(final Player player) {
        if (player == null) {
            return false;
        }

        try (final PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM commands WHERE username = ?")) {
            statement.setString(1, player.getName());
            ResultSet result = statement.executeQuery();
            return result.next();
        }
        catch (final SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}