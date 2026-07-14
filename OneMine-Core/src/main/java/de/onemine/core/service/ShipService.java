package de.onemine.core.service;

import de.onemine.core.database.DatabaseManager;
import de.onemine.core.model.PlayerProfile;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * ShipService: Manages ship ownership, levels, and SimpleShips helm distribution.
 */
public class ShipService {
    private final JavaPlugin plugin;
    private final DatabaseManager databaseManager;
    private final PlayerProfileService playerProfileService;

    public ShipService(JavaPlugin plugin, DatabaseManager databaseManager, PlayerProfileService playerProfileService) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.playerProfileService = playerProfileService;
    }

    /**
     * Create a new ship for a player.
     */
    public int createShip(String ownerUuid, String shipName, int level) {
        try {
            int blockLimit = getBlockLimitForLevel(level);
            String sql = "INSERT INTO ships (owner_uuid, name, ship_level, block_limit) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = databaseManager.prepareStatement(sql);
            stmt.setString(1, ownerUuid);
            stmt.setString(2, shipName);
            stmt.setInt(3, level);
            stmt.setInt(4, blockLimit);
            stmt.executeUpdate();

            ResultSet rs = databaseManager.executeQuery("SELECT last_insert_rowid() as id");
            int shipId = rs.getInt("id");
            rs.close();
            stmt.close();

            return shipId;
        } catch (SQLException e) {
            plugin.getLogger().severe("Error creating ship: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Get block limit for a ship level.
     */
    public int getBlockLimitForLevel(int level) {
        return switch (level) {
            case 1 -> 100;
            case 2 -> 300;
            case 3 -> 500;
            default -> 100;
        };
    }

    /**
     * Upgrade a ship to the next level.
     */
    public void upgradeShip(int shipId) {
        try {
            String sql = "UPDATE ships SET ship_level = ship_level + 1, block_limit = ? WHERE id = ? AND ship_level < 3";
            PreparedStatement stmt = databaseManager.prepareStatement(sql);
            int newLevel = getShipLevel(shipId) + 1;
            stmt.setInt(1, getBlockLimitForLevel(newLevel));
            stmt.setInt(2, shipId);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error upgrading ship: " + e.getMessage());
        }
    }

    /**
     * Get ship level.
     */
    public int getShipLevel(int shipId) {
        try {
            String sql = "SELECT ship_level FROM ships WHERE id = ?";
            PreparedStatement stmt = databaseManager.prepareStatement(sql);
            stmt.setInt(1, shipId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int level = rs.getInt("ship_level");
                rs.close();
                stmt.close();
                return level;
            }
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting ship level: " + e.getMessage());
        }
        return 1;
    }

    /**
     * Get ship block limit.
     */
    public int getShipBlockLimit(int shipId) {
        try {
            String sql = "SELECT block_limit FROM ships WHERE id = ?";
            PreparedStatement stmt = databaseManager.prepareStatement(sql);
            stmt.setInt(1, shipId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int limit = rs.getInt("block_limit");
                rs.close();
                stmt.close();
                return limit;
            }
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting ship block limit: " + e.getMessage());
        }
        return 100;
    }

    /**
     * Delete a ship.
     */
    public void deleteShip(int shipId) {
        try {
            String sql = "DELETE FROM ships WHERE id = ?";
            PreparedStatement stmt = databaseManager.prepareStatement(sql);
            stmt.setInt(1, shipId);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error deleting ship: " + e.getMessage());
        }
    }
}
