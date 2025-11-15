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
    private final Map<UUID, Integer> totalKills;      // Kills totaux de la partie
    private final Map<UUID, Integer> roundKills;      // Kills du round en cours
    private final Map<UUID, Integer> roundsWon;
    
    public FocusPointsManager() {
        this.points = new HashMap<>();
        this.totalKills = new HashMap<>();
        this.roundKills = new HashMap<>();
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
    
    /**
     * Retourne les kills TOTAUX de la partie (pour la condition de victoire)
     */
    public int getKills(Player player) {
        return totalKills.getOrDefault(player.getUniqueId(), 0);
    }
    
    /**
     * Retourne les kills du round en cours (pour le classement)
     */
    public int getRoundKills(Player player) {
        return roundKills.getOrDefault(player.getUniqueId(), 0);
    }
    
    /**
     * Enregistre un kill (ajouté aux kills totaux ET aux kills du round)
     */
    public void registerKill(Player killer) {
        UUID id = killer.getUniqueId();
        totalKills.put(id, totalKills.getOrDefault(id, 0) + 1);
        roundKills.put(id, roundKills.getOrDefault(id, 0) + 1);
    }
    
    /**
     * Reset uniquement les kills du round (appelé à chaque début de round)
     */
    public void resetRoundKills() {
        roundKills.clear();
    }
    
    /**
     * @deprecated Utilisez resetRoundKills() à la place
     */
    @Deprecated
    public void resetKills(Player player) {
        roundKills.put(player.getUniqueId(), 0);
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
        totalKills.put(id, 0);
        roundKills.put(id, 0);
        roundsWon.put(id, 0);
    }
    
    /**
     * Reset uniquement les kills du round (pas les kills totaux!)
     */
    public void resetAllKills() {
        roundKills.clear();
    }
    
    public void resetAll() {
        points.clear();
        totalKills.clear();
        roundKills.clear();
        roundsWon.clear();
    }
}
