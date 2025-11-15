package com.nerysia.plugin.grades.listeners;

import com.nerysia.plugin.Nerysia;
import com.nerysia.plugin.grades.Grade;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class GradeDisplayListener implements Listener {

    private Nerysia plugin;

    public GradeDisplayListener(Nerysia plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Si c'est la première connexion, enregistrer le grade Joueur
        if (!plugin.getGradeManager().hasGradeSet(player.getUniqueId())) {
            plugin.getGradeManager().setGrade(player.getUniqueId(), Grade.JOUEUR);
        }
        
        // Récupérer le grade du joueur
        Grade grade = plugin.getGradeManager().getGrade(player.getUniqueId());
        
        // Désactiver le message par défaut
        event.setJoinMessage(null);
        
        // Créer le message personnalisé
        String joinMessage = grade.getPrefix() + "[" + grade.getTabName() + "] §f" + player.getName() + " §ea rejoint le serveur";
        
        // Envoyer le message au joueur qui se connecte (toujours)
        player.sendMessage(joinMessage);
        
        // Envoyer aux autres joueurs dans le Lobby qui n'ont pas masqué les joueurs
        for (Player online : org.bukkit.Bukkit.getOnlinePlayers()) {
            // Ne pas renvoyer au joueur qui vient de se connecter
            if (online.equals(player)) {
                continue;
            }
            
            // Vérifier si le joueur est dans le lobby
            if (online.getWorld().getName().equals("Lobby")) {
                // Vérifier que le joueur n'a pas masqué les autres joueurs
                boolean canSeePlayers = plugin.getPlayerVisibilityManager().arePlayersVisible(online.getUniqueId());
                
                if (canSeePlayers) {
                    online.sendMessage(joinMessage);
                }
            }
        }
        
        // Mettre à jour le display name pour le chat
        String displayName = grade.getPrefix() + "[" + grade.getTabName() + "] §f" + player.getName() + "§r";
        player.setDisplayName(displayName);
    }
}

