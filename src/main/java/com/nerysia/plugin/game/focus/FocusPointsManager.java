package com.nerysia.plugin.game.focus;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gère les points, kills et rounds gagnés de chaque joueur dans Focus
 */
public class FocusPointsManager {
    
    private final Map<UUID, Integer> points;
    private final Map<UUID, Integer> kills;
    private final Map<UUID, Integer> roundsWon;
    
    public FocusPointsManager() {
        this.points = new HashMap<>();
        this.kills = new HashMap<>();
        this.roundsWon = new HashMap<>();
    }
    
    // ========== POINTS ==========
    
    public int getPoints(Player player) {
        return points.getOrDefault(player.getUniqueId(), 0);
    }
    
    public void addPoints(Player player, int amount) {
        UUID id = player.getUniqueId();
        points.put(id, points.getOrDefault(id, 0) + amount);
    }
    
    public void setPoints(Player player, int amount) {
        points.put(player.getUniqueId(), amount);
    }
    
    public boolean removePoints(Player player, int amount) {
        UUID id = player.getUniqueId();
        int current = points.getOrDefault(id, 0);
        if (current >= amount) {
            points.put(id, current - amount);
            return true;
        }
        return false;
    }
    
    // ========== KILLS ==========
    
    public int getKills(Player player) {
        return kills.getOrDefault(player.getUniqueId(), 0);
    }
    
    public void registerKill(Player killer) {
        UUID id = killer.getUniqueId();
        kills.put(id, kills.getOrDefault(id, 0) + 1);
    }
    
    public void resetKills(Player player) {
        kills.put(player.getUniqueId(), 0);
    }
    
    // ========== ROUNDS WON ==========
    
    public int getRoundsWon(Player player) {
        return roundsWon.getOrDefault(player.getUniqueId(), 0);
    }
    
    public void incrementRoundsWon(Player player) {
        UUID id = player.getUniqueId();
        roundsWon.put(id, roundsWon.getOrDefault(id, 0) + 1);
    }
    
    // ========== RESET ==========
    
    public void resetPlayer(Player player) {
        UUID id = player.getUniqueId();
        points.put(id, 0);
        kills.put(id, 0);
        roundsWon.put(id, 0);
    }
    
    public void resetAllKills() {
        kills.clear();
    }
    
    public void resetAll() {
        points.clear();
        kills.clear();
        roundsWon.clear();
    }
}
