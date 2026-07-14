package de.onemine.core.service;

import de.onemine.core.database.DatabaseManager;
import de.onemine.core.model.PlayerProfile;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * BerryService: Coordinates transaction validation and Vault interactions.
 * Manages Berry currency as the unified economy.
 */
public class BerryService {
    private final JavaPlugin plugin;
    private final DatabaseManager databaseManager;
    private Economy vaultEconomy;

    public BerryService(JavaPlugin plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        setupVault();
    }

    /**
     * Initialize Vault economy integration.
     */
    private void setupVault() {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            vaultEconomy = rsp.getProvider();
            plugin.getLogger().info("✓ Vault economy integrated");
        } else {
            plugin.getLogger().warning("⚠ Vault not found; economy disabled");
        }
    }

    /**
     * Add Berry to a player.
     */
    public void addBerry(PlayerProfile profile, double amount, String reason) {
        if (amount <= 0) return;
        profile.addBerry(amount);
        logTransaction(profile.getUuid().toString(), amount, reason);
    }

    /**
     * Remove Berry from a player.
     */
    public boolean removeBerry(PlayerProfile profile, double amount, String reason) {
        if (amount <= 0 || profile.getBerryBalance() < amount) {
            return false;
        }
        profile.removeBerry(amount);
        logTransaction(profile.getUuid().toString(), -amount, reason);
        return true;
    }

    /**
     * Transfer Berry between players.
     */
    public boolean transferBerry(PlayerProfile from, PlayerProfile to, double amount, String reason) {
        if (!removeBerry(from, amount, reason + " (transfer)")) {
            return false;
        }
        addBerry(to, amount, reason + " (received)");
        return true;
    }

    /**
     * Check if player has enough Berry.
     */
    public boolean hasBerry(PlayerProfile profile, double amount) {
        return profile.getBerryBalance() >= amount;
    }

    /**
     * Get current Berry balance.
     */
    public double getBalance(PlayerProfile profile) {
        return profile.getBerryBalance();
    }

    /**
     * Sync Vault and OneMine Berry balance (if Vault is available).
     */
    public void syncWithVault(Player player) {
        if (vaultEconomy != null) {
            vaultEconomy.depositPlayer(player, 0); // Ensures account exists
        }
    }

    /**
     * Log transaction to database (for auditing).
     */
    private void logTransaction(String uuid, double amount, String reason) {
        try {
            String sql = "INSERT INTO berry_transactions (player_uuid, amount, reason, timestamp) VALUES (?, ?, ?, ?)";
            var stmt = databaseManager.prepareStatement(sql);
            stmt.setString(1, uuid);
            stmt.setDouble(2, amount);
            stmt.setString(3, reason);
            stmt.setLong(4, System.currentTimeMillis());
            stmt.executeUpdate();
            stmt.close();
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to log Berry transaction: " + e.getMessage());
        }
    }
}
