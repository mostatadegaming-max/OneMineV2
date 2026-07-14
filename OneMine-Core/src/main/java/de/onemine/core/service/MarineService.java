package de.onemine.core.service;

import de.onemine.core.database.DatabaseManager;
import de.onemine.core.model.PlayerProfile;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * MarineService: Coordinates wanted levels and marine threat tiers.
 */
public class MarineService {
    private final JavaPlugin plugin;
    private final DatabaseManager databaseManager;
    private final PlayerProfileService playerProfileService;

    public MarineService(JavaPlugin plugin, DatabaseManager databaseManager, PlayerProfileService playerProfileService) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.playerProfileService = playerProfileService;
    }

    /**
     * Increment wanted level for a player.
     */
    public void incrementWantedLevel(UUID playerUuid) {
        PlayerProfile profile = playerProfileService.getProfile(playerUuid);
        profile.incrementWantedLevel();
        updateWantedLevelInDatabase(playerUuid, profile.getWantedLevel());
    }

    /**
     * Decrement wanted level for a player.
     */
    public void decrementWantedLevel(UUID playerUuid) {
        PlayerProfile profile = playerProfileService.getProfile(playerUuid);
        profile.decrementWantedLevel();
        updateWantedLevelInDatabase(playerUuid, profile.getWantedLevel());
    }

    /**
     * Set wanted level directly.
     */
    public void setWantedLevel(UUID playerUuid, int level) {
        PlayerProfile profile = playerProfileService.getProfile(playerUuid);
        profile.setWantedLevel(level);
        updateWantedLevelInDatabase(playerUuid, level);
    }

    /**
     * Get wanted level.
     */
    public int getWantedLevel(UUID playerUuid) {
        PlayerProfile profile = playerProfileService.getProfile(playerUuid);
        return profile.getWantedLevel();
    }

    /**
     * Check if player is wanted (wanted level > 0).
     */
    public boolean isWanted(UUID playerUuid) {
        return getWantedLevel(playerUuid) > 0;
    }

    /**
     * Get marine threat tier based on wanted level.
     */
    public String getMarineThreatTier(UUID playerUuid) {
        int wantedLevel = getWantedLevel(playerUuid);
        return switch (wantedLevel) {
            case 0 -> "NONE";
            case 1 -> "RECRUIT";
            case 2 -> "SOLDIER";
            case 3 -> "OFFICER";
            case 4 -> "VICE_ADMIRAL";
            case 5 -> "ADMIRAL";
            default -> "NONE";
        };
    }

    /**
     * Update wanted level in database.
     */
    private void updateWantedLevelInDatabase(UUID playerUuid, int level) {
        try {
            String sql = "INSERT OR REPLACE INTO marine_stats (player_uuid, wanted_level) VALUES (?, ?)";
            PreparedStatement stmt = databaseManager.prepareStatement(sql);
            stmt.setString(1, playerUuid.toString());
            stmt.setInt(2, level);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error updating wanted level: " + e.getMessage());
        }
    }

    /**
     * Log marine encounter.
     */
    public void logMarineEncounter(UUID playerUuid) {
        try {
            String sql = "UPDATE marine_stats SET marine_encounters = marine_encounters + 1, last_encounter = ? WHERE player_uuid = ?";
            PreparedStatement stmt = databaseManager.prepareStatement(sql);
            stmt.setLong(1, System.currentTimeMillis());
            stmt.setString(2, playerUuid.toString());
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error logging marine encounter: " + e.getMessage());
        }
    }

    /**
     * Log marine defeat.
     */
    public void logMarineDefeated(UUID playerUuid) {
        try {
            String sql = "UPDATE marine_stats SET marines_defeated = marines_defeated + 1 WHERE player_uuid = ?";
            PreparedStatement stmt = databaseManager.prepareStatement(sql);
            stmt.setString(1, playerUuid.toString());
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error logging marine defeat: " + e.getMessage());
        }
    }
}
