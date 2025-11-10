package com.nerysia.plugin.lobby.handlers;

import com.nerysia.plugin.Nerysia;
import com.nerysia.plugin.grades.Grade;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerVisibilityManager {

    private Nerysia plugin;
    private Map<UUID, Boolean> playerVisibilitySettings; // true = joueurs visibles, false = joueurs invisibles

    public PlayerVisibilityManager(Nerysia plugin) {
        this.plugin = plugin;
        this.playerVisibilitySettings = new HashMap<>();
    }

    /**
     * Définit si un joueur veut voir les autres joueurs
     * @param uuid UUID du joueur
     * @param visible true pour voir les joueurs, false pour les cacher
     */
    public void setPlayersVisible(UUID uuid, boolean visible) {
        playerVisibilitySettings.put(uuid, visible);
        updateVisibilityForPlayer(uuid);
    }

    /**
     * Vérifie si un joueur a activé la visibilité des autres joueurs
     * @param uuid UUID du joueur
     * @return true si les joueurs sont visibles, false sinon
     */
    public boolean arePlayersVisible(UUID uuid) {
        return playerVisibilitySettings.getOrDefault(uuid, true); // Par défaut, les joueurs sont visibles
    }

    /**
     * Bascule l'état de visibilité pour un joueur
     * @param uuid UUID du joueur
     * @return Le nouvel état (true = visible, false = invisible)
     */
    public boolean toggleVisibility(UUID uuid) {
        boolean currentState = arePlayersVisible(uuid);
        boolean newState = !currentState;
        setPlayersVisible(uuid, newState);
        return newState;
    }

    /**
     * Met à jour la visibilité de tous les joueurs pour un joueur spécifique
     * @param viewerUuid UUID du joueur dont on met à jour la vision
     */
    private void updateVisibilityForPlayer(UUID viewerUuid) {
        Player viewer = Bukkit.getPlayer(viewerUuid);
        if (viewer == null || !viewer.isOnline()) {
            return;
        }

        boolean shouldSeeOthers = arePlayersVisible(viewerUuid);

        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target.getUniqueId().equals(viewerUuid)) {
                continue; // Ne pas se cacher soi-même
            }

            // Vérifier le grade du joueur cible
            Grade targetGrade = plugin.getGradeManager().getGrade(target.getUniqueId());

            if (shouldSeeOthers) {
                // Rendre tous les joueurs visibles
                viewer.showPlayer(target);
            } else {
                // Masquer uniquement les joueurs avec le grade JOUEUR
                if (targetGrade == Grade.JOUEUR) {
                    viewer.hidePlayer(target);
                } else {
                    viewer.showPlayer(target);
                }
            }
        }
    }

    /**
     * Met à jour la visibilité d'un joueur spécifique pour tous les autres joueurs
     * Utile quand un joueur rejoint ou change de grade
     * @param targetUuid UUID du joueur qui vient de rejoindre ou changer
     */
    public void updateVisibilityOfPlayer(UUID targetUuid) {
        Player target = Bukkit.getPlayer(targetUuid);
        if (target == null || !target.isOnline()) {
            return;
        }

        Grade targetGrade = plugin.getGradeManager().getGrade(targetUuid);

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.getUniqueId().equals(targetUuid)) {
                continue;
            }

            boolean viewerSeesOthers = arePlayersVisible(viewer.getUniqueId());

            if (viewerSeesOthers) {
                // Le viewer voit tout le monde
                viewer.showPlayer(target);
            } else {
                // Le viewer a masqué les joueurs normaux
                if (targetGrade == Grade.JOUEUR) {
                    viewer.hidePlayer(target);
                } else {
                    viewer.showPlayer(target);
                }
            }
        }
    }

    /**
     * Applique les paramètres de visibilité pour un joueur qui vient de rejoindre
     * @param uuid UUID du joueur
     */
    public void applyVisibilityForNewPlayer(UUID uuid) {
        // Mettre à jour ce que ce joueur voit
        updateVisibilityForPlayer(uuid);
        
        // Mettre à jour comment les autres voient ce joueur
        updateVisibilityOfPlayer(uuid);
    }

    /**
     * Retire un joueur de la map quand il quitte
     * @param uuid UUID du joueur
     */
    public void removePlayer(UUID uuid) {
        playerVisibilitySettings.remove(uuid);
    }
}
