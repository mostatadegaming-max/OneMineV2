package de.onemine.core.service;

import de.onemine.core.database.DatabaseManager;
import de.onemine.core.model.PlayerProfile;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * IslandService: Coordinates island claims, flags, and hourly Berry income payouts.
 */
public class IslandService {
    private final JavaPlugin plugin;
    private final DatabaseManager databaseManager;
    private final PlayerProfileService playerProfileService;

    public IslandService(JavaPlugin plugin, DatabaseManager databaseManager, PlayerProfileService playerProfileService) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.playerProfileService = playerProfileService;
    }

    /**
     * Claim an island for a crew.
     */
    public int claimIsland(int crewId, String worldName, double x, double y, double z, double claimCost) {
        try {
            String sql = "INSERT INTO islands (crew_id, world_name, center_x, center_y, center_z, claim_cost, hourly_berry_income, last_payout) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = databaseManager.prepareStatement(sql);
            stmt.setInt(1, crewId);
            stmt.setString(2, worldName);
            stmt.setDouble(3, x);
            stmt.setDouble(4, y);
            stmt.setDouble(5, z);
            stmt.setDouble(6, claimCost);
            stmt.setDouble(7, 200.0); // Default hourly income
            stmt.setLong(8, System.currentTimeMillis());
            stmt.executeUpdate();

            ResultSet rs = databaseManager.executeQuery("SELECT last_insert_rowid() as id");
            int islandId = rs.getInt("id");
            rs.close();
            stmt.close();

            return islandId;
        } catch (SQLException e) {
            plugin.getLogger().severe("Error claiming island: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Set island flag (PvP, build, etc.).
     */
    public void setIslandFlag(int islandId, String flagName, boolean value) {
        try {
            String sql = "INSERT OR REPLACE INTO island_flags (island_id, flag_name, flag_value) VALUES (?, ?, ?)";
            PreparedStatement stmt = databaseManager.prepareStatement(sql);
            stmt.setInt(1, islandId);
            stmt.setString(2, flagName);
            stmt.setBoolean(3, value);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error setting island flag: " + e.getMessage());
        }
    }

    /**
     * Get island flag value.
     */
    public boolean getIslandFlag(int islandId, String flagName) {
        try {
            String sql = "SELECT flag_value FROM island_flags WHERE island_id = ? AND flag_name = ?";
            PreparedStatement stmt = databaseManager.prepareStatement(sql);
            stmt.setInt(1, islandId);
            stmt.setString(2, flagName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("flag_value");
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting island flag: " + e.getMessage());
        }
        return false;
    }

    /**
     * Process hourly Berry payouts for all islands.
     */
    public void processHourlyPayouts() {
        try {
            String sql = "SELECT id, crew_id, hourly_berry_income FROM islands";
            ResultSet rs = databaseManager.executeQuery(sql);
            while (rs.next()) {
                int islandId = rs.getInt("id");
                int crewId = rs.getInt("crew_id");
                double income = rs.getDouble("hourly_berry_income");

                // Add to crew balance
                addCrewBalance(crewId, income);

                // Update last payout
                String updateSql = "UPDATE islands SET last_payout = ? WHERE id = ?";
                PreparedStatement stmt = databaseManager.prepareStatement(updateSql);
                stmt.setLong(1, System.currentTimeMillis());
                stmt.setInt(2, islandId);
                stmt.executeUpdate();
                stmt.close();
            }
            rs.close();
            plugin.getLogger().info("✓ Island hourly payouts processed");
        } catch (SQLException e) {
            plugin.getLogger().severe("Error processing island payouts: " + e.getMessage());
        }
    }

    /**
     * Add balance to crew.
     */
    private void addCrewBalance(int crewId, double amount) {
        try {
            String sql = "UPDATE crews SET balance = balance + ? WHERE id = ?";
            PreparedStatement stmt = databaseManager.prepareStatement(sql);
            stmt.setDouble(1, amount);
            stmt.setInt(2, crewId);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error adding crew balance: " + e.getMessage());
        }
    }
}
