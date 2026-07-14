package de.onemine.core.service;

import de.onemine.core.database.DatabaseManager;
import de.onemine.core.model.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PlayerProfileService: Manages profile loading, caching, saving, and updates.
 */
public class PlayerProfileService {
    private final JavaPlugin plugin;
    private final DatabaseManager databaseManager;
    private final Map<UUID, PlayerProfile> profileCache;
    private final Map<UUID, Long> lastSaveTime;

    public PlayerProfileService(JavaPlugin plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.profileCache = new ConcurrentHashMap<>();
        this.lastSaveTime = new ConcurrentHashMap<>();
    }

    /**
     * Load or create a player profile.
     */
    public PlayerProfile getProfile(UUID uuid) {
        // Check cache first
        if (profileCache.containsKey(uuid)) {
            return profileCache.get(uuid);
        }

        // Try to load from database
        PlayerProfile profile = loadFromDatabase(uuid);
        if (profile == null) {
            // Create new profile
            Player player = Bukkit.getPlayer(uuid);
            String playerName = player != null ? player.getName() : "Unknown";
            profile = new PlayerProfile(uuid, playerName);
            saveToDatabase(profile);
        }

        profileCache.put(uuid, profile);
        return profile;
    }

    /**
     * Load profile from database.
     */
    private PlayerProfile loadFromDatabase(UUID uuid) {
        try {
            String sql = "SELECT * FROM players WHERE uuid = ?";
            PreparedStatement stmt = databaseManager.prepareStatement(sql);
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                PlayerProfile profile = new PlayerProfile(uuid, rs.getString("player_name"));
                profile.setBerryBalance(rs.getDouble("berry_balance"));
                profile.setCrewId(rs.getInt("crew_id"));
                profile.setCrewRole(rs.getString("crew_role"));
                profile.setCrewColor(rs.getString("crew_color"));
                profile.setCrewLevel(rs.getInt("crew_level"));
                profile.setKills(rs.getInt("kills"));
                profile.setDeaths(rs.getInt("deaths"));
                profile.setBountyAmount(rs.getInt("bounty_amount"));
                profile.setWantedLevel(rs.getInt("wanted_level"));
                profile.setPlaytimeSeconds(rs.getLong("playtime_seconds"));
                profile.setLastLoginTime(rs.getLong("last_login"));
                rs.close();
                stmt.close();
                return profile;
            }
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error loading profile for " + uuid + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Save profile to database.
     */
    public void saveToDatabase(PlayerProfile profile) {
        try {
            String sql = "INSERT OR REPLACE INTO players (uuid, player_name, berry_balance, crew_id, crew_role, crew_color, crew_level, kills, deaths, bounty_amount, wanted_level, playtime_seconds, last_login) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = databaseManager.prepareStatement(sql);
            stmt.setString(1, profile.getUuid().toString());
            stmt.setString(2, profile.getPlayerName());
            stmt.setDouble(3, profile.getBerryBalance());
            stmt.setInt(4, profile.getCrewId());
            stmt.setString(5, profile.getCrewRole());
            stmt.setString(6, profile.getCrewColor());
            stmt.setInt(7, profile.getCrewLevel());
            stmt.setInt(8, profile.getKills());
            stmt.setInt(9, profile.getDeaths());
            stmt.setInt(10, profile.getBountyAmount());
            stmt.setInt(11, profile.getWantedLevel());
            stmt.setLong(12, profile.getPlaytimeSeconds());
            stmt.setLong(13, profile.getLastLoginTime());
            stmt.executeUpdate();
            stmt.close();
            lastSaveTime.put(profile.getUuid(), System.currentTimeMillis());
        } catch (SQLException e) {
            plugin.getLogger().severe("Error saving profile for " + profile.getUuid() + ": " + e.getMessage());
        }
    }

    /**
     * Save profile if enough time has passed (debounce).
     */
    public void saveIfNeeded(UUID uuid, long intervalMs) {
        Long lastSave = lastSaveTime.getOrDefault(uuid, 0L);
        if (System.currentTimeMillis() - lastSave > intervalMs) {
            PlayerProfile profile = profileCache.get(uuid);
            if (profile != null) {
                saveToDatabase(profile);
            }
        }
    }

    /**
     * Unload profile from cache and save to database.
     */
    public void unloadProfile(UUID uuid) {
        PlayerProfile profile = profileCache.remove(uuid);
        if (profile != null) {
            saveToDatabase(profile);
        }
    }

    /**
     * Clear all cached profiles (use on shutdown).
     */
    public void saveAll() {
        for (PlayerProfile profile : profileCache.values()) {
            saveToDatabase(profile);
        }
    }
}
