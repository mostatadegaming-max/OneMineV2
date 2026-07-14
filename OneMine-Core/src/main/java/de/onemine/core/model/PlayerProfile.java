package de.onemine.core.model;

import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

/**
 * PlayerProfile: Centralized player data model for all OneMine systems.
 */
public class PlayerProfile {
    private UUID uuid;
    private String playerName;
    private double berryBalance;
    private int crewId;
    private String crewRole; // MEMBER, CAPTAIN, LEADER
    private String crewColor;
    private int crewLevel;
    private Set<Integer> ownedShipIds;
    private Set<Integer> claimedIslandIds;
    private int kills;
    private int deaths;
    private int bountyAmount;
    private int wantedLevel; // 0-5
    private Set<String> titles;
    private long playtimeSeconds;
    private long lastLoginTime;

    public PlayerProfile(UUID uuid, String playerName) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.berryBalance = 0.0;
        this.crewId = -1;
        this.crewRole = "NONE";
        this.crewColor = "WHITE";
        this.crewLevel = 0;
        this.ownedShipIds = new HashSet<>();
        this.claimedIslandIds = new HashSet<>();
        this.kills = 0;
        this.deaths = 0;
        this.bountyAmount = 0;
        this.wantedLevel = 0;
        this.titles = new HashSet<>();
        this.playtimeSeconds = 0;
        this.lastLoginTime = System.currentTimeMillis();
    }

    // Getters and Setters
    public UUID getUuid() { return uuid; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public double getBerryBalance() { return berryBalance; }
    public void setBerryBalance(double berryBalance) { this.berryBalance = berryBalance; }
    public void addBerry(double amount) { this.berryBalance += amount; }
    public void removeBerry(double amount) { this.berryBalance = Math.max(0, this.berryBalance - amount); }

    public int getCrewId() { return crewId; }
    public void setCrewId(int crewId) { this.crewId = crewId; }

    public String getCrewRole() { return crewRole; }
    public void setCrewRole(String crewRole) { this.crewRole = crewRole; }

    public String getCrewColor() { return crewColor; }
    public void setCrewColor(String crewColor) { this.crewColor = crewColor; }

    public int getCrewLevel() { return crewLevel; }
    public void setCrewLevel(int crewLevel) { this.crewLevel = crewLevel; }

    public Set<Integer> getOwnedShipIds() { return ownedShipIds; }
    public void addShipId(int shipId) { this.ownedShipIds.add(shipId); }
    public void removeShipId(int shipId) { this.ownedShipIds.remove(shipId); }

    public Set<Integer> getClaimedIslandIds() { return claimedIslandIds; }
    public void addIslandId(int islandId) { this.claimedIslandIds.add(islandId); }
    public void removeIslandId(int islandId) { this.claimedIslandIds.remove(islandId); }

    public int getKills() { return kills; }
    public void setKills(int kills) { this.kills = kills; }
    public void incrementKills() { this.kills++; }

    public int getDeaths() { return deaths; }
    public void setDeaths(int deaths) { this.deaths = deaths; }
    public void incrementDeaths() { this.deaths++; }

    public int getBountyAmount() { return bountyAmount; }
    public void setBountyAmount(int bountyAmount) { this.bountyAmount = bountyAmount; }

    public int getWantedLevel() { return wantedLevel; }
    public void setWantedLevel(int wantedLevel) { this.wantedLevel = Math.min(5, Math.max(0, wantedLevel)); }
    public void incrementWantedLevel() { this.wantedLevel = Math.min(5, this.wantedLevel + 1); }
    public void decrementWantedLevel() { this.wantedLevel = Math.max(0, this.wantedLevel - 1); }

    public Set<String> getTitles() { return titles; }
    public void addTitle(String title) { this.titles.add(title); }

    public long getPlaytimeSeconds() { return playtimeSeconds; }
    public void setPlaytimeSeconds(long playtimeSeconds) { this.playtimeSeconds = playtimeSeconds; }
    public void addPlaytime(long seconds) { this.playtimeSeconds += seconds; }

    public long getLastLoginTime() { return lastLoginTime; }
    public void setLastLoginTime(long lastLoginTime) { this.lastLoginTime = lastLoginTime; }

    public boolean hasInCrew() { return crewId > 0; }
    public boolean isCrewLeader() { return "LEADER".equals(crewRole); }
}
