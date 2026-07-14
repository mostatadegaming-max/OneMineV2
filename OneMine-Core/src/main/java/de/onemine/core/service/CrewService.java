package de.onemine.core.service;

import de.onemine.core.database.DatabaseManager;
import de.onemine.core.model.PlayerProfile;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * CrewService: Coordinates crew creation, membership, colors, and levels.
 */
public class CrewService {
    private final JavaPlugin plugin;
    private final DatabaseManager databaseManager;
    private final PlayerProfileService playerProfileService;

    public CrewService(JavaPlugin plugin, DatabaseManager databaseManager, PlayerProfileService playerProfileService) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.playerProfileService = playerProfileService;
    }

    /**
     * Create a new crew.
     */
    public int createCrew(String crewName, UUID leaderUuid, String color) {
        try {
            String sql = "INSERT INTO crews (name, leader_uuid, color, level, balance) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = databaseManager.prepareStatement(sql);
            stmt.setString(1, crewName);
            stmt.setString(2, leaderUuid.toString());
            stmt.setString(3, color);
            stmt.setInt(4, 1);
            stmt.setDouble(5, 0.0);
            stmt.executeUpdate();

            // Get the inserted crew ID
            ResultSet rs = databaseManager.executeQuery("SELECT last_insert_rowid() as id");
            int crewId = rs.getInt("id");
            rs.close();
            stmt.close();

            // Add leader to crew_members
            addMember(crewId, leaderUuid, "LEADER");

            // Update player profile
            PlayerProfile profile = playerProfileService.getProfile(leaderUuid);
            profile.setCrewId(crewId);
            profile.setCrewRole("LEADER");
            profile.setCrewColor(color);

            return crewId;
        } catch (SQLException e) {
            plugin.getLogger().severe("Error creating crew: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Add member to crew.
     */
    public void addMember(int crewId, UUID memberUuid, String role) {
        try {
            String sql = "INSERT INTO crew_members (crew_id, uuid, role) VALUES (?, ?, ?)";
            PreparedStatement stmt = databaseManager.prepareStatement(sql);
            stmt.setInt(1, crewId);
            stmt.setString(2, memberUuid.toString());
            stmt.setString(3, role);
            stmt.executeUpdate();
            stmt.close();

            PlayerProfile profile = playerProfileService.getProfile(memberUuid);
            profile.setCrewId(crewId);
            profile.setCrewRole(role);
        } catch (SQLException e) {
            plugin.getLogger().severe("Error adding crew member: " + e.getMessage());
        }
    }

    /**
     * Remove member from crew.
     */
    public void removeMember(int crewId, UUID memberUuid) {
        try {
            String sql = "DELETE FROM crew_members WHERE crew_id = ? AND uuid = ?";
            PreparedStatement stmt = databaseManager.prepareStatement(sql);
            stmt.setInt(1, crewId);
            stmt.setString(2, memberUuid.toString());
            stmt.executeUpdate();
            stmt.close();

            PlayerProfile profile = playerProfileService.getProfile(memberUuid);
            profile.setCrewId(-1);
            profile.setCrewRole("NONE");
        } catch (SQLException e) {
            plugin.getLogger().severe("Error removing crew member: " + e.getMessage());
        }
    }

    /**
     * Get crew member count.
     */
    public int getMemberCount(int crewId) {
        try {
            String sql = "SELECT COUNT(*) as count FROM crew_members WHERE crew_id = ?";
            PreparedStatement stmt = databaseManager.prepareStatement(sql);
            stmt.setInt(1, crewId);
            ResultSet rs = stmt.executeQuery();
            int count = rs.getInt("count");
            rs.close();
            stmt.close();
            return count;
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting member count: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Get all crew members.
     */
    public List<UUID> getMembers(int crewId) {
        List<UUID> members = new ArrayList<>();
        try {
            String sql = "SELECT uuid FROM crew_members WHERE crew_id = ?";
            PreparedStatement stmt = databaseManager.prepareStatement(sql);
            stmt.setInt(1, crewId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                members.add(UUID.fromString(rs.getString("uuid")));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting crew members: " + e.getMessage());
        }
        return members;
    }

    /**
     * Level up a crew.
     */
    public void levelUpCrew(int crewId) {
        try {
            String sql = "UPDATE crews SET level = level + 1 WHERE id = ?";
            PreparedStatement stmt = databaseManager.prepareStatement(sql);
            stmt.setInt(1, crewId);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error leveling up crew: " + e.getMessage());
        }
    }

    /**
     * Set crew color.
     */
    public void setCrewColor(int crewId, String color) {
        try {
            String sql = "UPDATE crews SET color = ? WHERE id = ?";
            PreparedStatement stmt = databaseManager.prepareStatement(sql);
            stmt.setString(1, color);
            stmt.setInt(2, crewId);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error setting crew color: " + e.getMessage());
        }
    }
}
