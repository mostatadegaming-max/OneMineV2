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
 * BountyService: Coordinates player bounties and payouts.
 */
public class BountyService {
    private final JavaPlugin plugin;
    private final DatabaseManager databaseManager;
    private final PlayerProfileService playerProfileService;
    private final BerryService berryService;

    public BountyService(JavaPlugin plugin, DatabaseManager databaseManager, PlayerProfileService playerProfileService, BerryService berryService) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.playerProfileService = playerProfileService;
        this.berryService = berryService;
    }

    /**
     * Place a bounty on a player.
     */
    public void placeBounty(UUID targetUuid, UUID placerUuid, int amount) {
        try {
            String sql = "INSERT INTO bounties (target_uuid, placer_uuid, amount) VALUES (?, ?, ?)";
            PreparedStatement stmt = databaseManager.prepareStatement(sql);
            stmt.setString(1, targetUuid.toString());
            stmt.setString(2, placerUuid.toString());
            stmt.setInt(3, amount);
            stmt.executeUpdate();
            stmt.close();

            // Update player bounty amount
            PlayerProfile target = playerProfileService.getProfile(targetUuid);
            target.setBountyAmount(target.getBountyAmount() + amount);
        } catch (SQLException e) {
            plugin.getLogger().severe("Error placing bounty: " + e.getMessage());
        }
    }

    /**
     * Claim a bounty.
     */
    public void claimBounty(int bountyId, UUID claimerUuid) {
        try {
            String sql = "UPDATE bounties SET claimed_by_uuid = ?, claimed_at = ? WHERE id = ?";
            PreparedStatement stmt = databaseManager.prepareStatement(sql);
            stmt.setString(1, claimerUuid.toString());
            stmt.setLong(2, System.currentTimeMillis());
            stmt.setInt(3, bountyId);
            stmt.executeUpdate();
            stmt.close();

            // Get bounty amount
            String querySql = "SELECT amount, target_uuid FROM bounties WHERE id = ?";
            PreparedStatement queryStmt = databaseManager.prepareStatement(querySql);
            queryStmt.setInt(1, bountyId);
            ResultSet rs = queryStmt.executeQuery();
            if (rs.next()) {
                int amount = rs.getInt("amount");
                UUID targetUuid = UUID.fromString(rs.getString("target_uuid"));

                // Pay the claimer
                PlayerProfile claimer = playerProfileService.getProfile(claimerUuid);
                berryService.addBerry(claimer, amount, "Bounty claimed on " + targetUuid);

                // Reduce target's bounty
                PlayerProfile target = playerProfileService.getProfile(targetUuid);
                target.setBountyAmount(Math.max(0, target.getBountyAmount() - amount));
            }
            rs.close();
            queryStmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error claiming bounty: " + e.getMessage());
        }
    }

    /**
     * Get all bounties on a player.
     */
    public List<Integer> getActiveBounties(UUID targetUuid) {
        List<Integer> bounties = new ArrayList<>();
        try {
            String sql = "SELECT id FROM bounties WHERE target_uuid = ? AND claimed_by_uuid IS NULL";
            PreparedStatement stmt = databaseManager.prepareStatement(sql);
            stmt.setString(1, targetUuid.toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                bounties.add(rs.getInt("id"));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting bounties: " + e.getMessage());
        }
        return bounties;
    }

    /**
     * Get total bounty amount on a player.
     */
    public int getTotalBounty(UUID targetUuid) {
        try {
            String sql = "SELECT COALESCE(SUM(amount), 0) as total FROM bounties WHERE target_uuid = ? AND claimed_by_uuid IS NULL";
            PreparedStatement stmt = databaseManager.prepareStatement(sql);
            stmt.setString(1, targetUuid.toString());
            ResultSet rs = stmt.executeQuery();
            int total = rs.getInt("total");
            rs.close();
            stmt.close();
            return total;
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting total bounty: " + e.getMessage());
        }
        return 0;
    }
}
