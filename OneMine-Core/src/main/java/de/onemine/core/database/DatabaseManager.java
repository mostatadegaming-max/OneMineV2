package de.onemine.core.database;

import de.onemine.core.Main;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;

/**
 * DatabaseManager: Centralized SQLite database manager for OneMine.
 * Manages all tables: players, crews, crew_members, crew_invitations, islands, island_flags, ships, bounties, marine_stats.
 */
public class DatabaseManager {
    private Connection connection;
    private final JavaPlugin plugin;
    private final String dbPath;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dbPath = plugin.getDataFolder() + "/onemine.db";
    }

    /**
     * Initialize database connection and create all required tables.
     */
    public void initialize() throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        createTables();
        plugin.getLogger().info("Database initialized at: " + dbPath);
    }

    /**
     * Create all required tables if they don't exist.
     */
    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Players table
            stmt.execute("CREATE TABLE IF NOT EXISTS players (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "uuid TEXT UNIQUE NOT NULL," +
                    "player_name TEXT NOT NULL," +
                    "berry_balance REAL DEFAULT 0.0," +
                    "crew_id INTEGER DEFAULT -1," +
                    "crew_role TEXT DEFAULT 'NONE'," +
                    "crew_color TEXT DEFAULT 'WHITE'," +
                    "crew_level INTEGER DEFAULT 0," +
                    "kills INTEGER DEFAULT 0," +
                    "deaths INTEGER DEFAULT 0," +
                    "bounty_amount INTEGER DEFAULT 0," +
                    "wanted_level INTEGER DEFAULT 0," +
                    "playtime_seconds LONG DEFAULT 0," +
                    "last_login LONG DEFAULT 0," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            // Crews table
            stmt.execute("CREATE TABLE IF NOT EXISTS crews (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT UNIQUE NOT NULL," +
                    "leader_uuid TEXT NOT NULL," +
                    "color TEXT DEFAULT 'WHITE'," +
                    "level INTEGER DEFAULT 1," +
                    "balance REAL DEFAULT 0.0," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            // Crew members table
            stmt.execute("CREATE TABLE IF NOT EXISTS crew_members (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "crew_id INTEGER NOT NULL," +
                    "uuid TEXT NOT NULL," +
                    "role TEXT DEFAULT 'MEMBER'," +
                    "joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (crew_id) REFERENCES crews(id)" +
                    ")");

            // Crew invitations table
            stmt.execute("CREATE TABLE IF NOT EXISTS crew_invitations (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "crew_id INTEGER NOT NULL," +
                    "invitee_uuid TEXT NOT NULL," +
                    "inviter_uuid TEXT NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "expires_at TIMESTAMP," +
                    "FOREIGN KEY (crew_id) REFERENCES crews(id)" +
                    ")");

            // Islands table
            stmt.execute("CREATE TABLE IF NOT EXISTS islands (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "crew_id INTEGER NOT NULL," +
                    "world_name TEXT NOT NULL," +
                    "center_x DOUBLE NOT NULL," +
                    "center_y DOUBLE NOT NULL," +
                    "center_z DOUBLE NOT NULL," +
                    "claim_cost REAL DEFAULT 1000.0," +
                    "hourly_berry_income REAL DEFAULT 200.0," +
                    "last_payout LONG," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (crew_id) REFERENCES crews(id)" +
                    ")");

            // Island flags table
            stmt.execute("CREATE TABLE IF NOT EXISTS island_flags (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "island_id INTEGER NOT NULL," +
                    "flag_name TEXT NOT NULL," +
                    "flag_value BOOLEAN DEFAULT 0," +
                    "UNIQUE(island_id, flag_name)," +
                    "FOREIGN KEY (island_id) REFERENCES islands(id)" +
                    ")");

            // Ships table
            stmt.execute("CREATE TABLE IF NOT EXISTS ships (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "owner_uuid TEXT NOT NULL," +
                    "name TEXT NOT NULL," +
                    "ship_level INTEGER DEFAULT 1," +
                    "block_limit INTEGER DEFAULT 100," +
                    "current_blocks INTEGER DEFAULT 0," +
                    "helm_uuid TEXT," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            // Bounties table
            stmt.execute("CREATE TABLE IF NOT EXISTS bounties (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "target_uuid TEXT NOT NULL," +
                    "placer_uuid TEXT NOT NULL," +
                    "amount INTEGER NOT NULL," +
                    "claimed_by_uuid TEXT," +
                    "claimed_at TIMESTAMP," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            // Marine stats table
            stmt.execute("CREATE TABLE IF NOT EXISTS marine_stats (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "player_uuid TEXT UNIQUE NOT NULL," +
                    "wanted_level INTEGER DEFAULT 0," +
                    "marine_encounters INTEGER DEFAULT 0," +
                    "marines_defeated INTEGER DEFAULT 0," +
                    "last_encounter LONG" +
                    ")");

            plugin.getLogger().info("✓ All database tables created/verified");
        }
    }

    /**
     * Get the current database connection.
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Execute a query and return results.
     */
    public ResultSet executeQuery(String sql) throws SQLException {
        return connection.createStatement().executeQuery(sql);
    }

    /**
     * Execute an update/insert/delete.
     */
    public int executeUpdate(String sql) throws SQLException {
        return connection.createStatement().executeUpdate(sql);
    }

    /**
     * Execute a prepared statement query.
     */
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return connection.prepareStatement(sql);
    }

    /**
     * Close database connection.
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("Database connection closed");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error closing database connection: " + e.getMessage());
        }
    }
}
