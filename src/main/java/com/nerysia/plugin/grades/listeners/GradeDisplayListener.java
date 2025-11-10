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
        
        // Personnaliser le message de connexion avec le grade
        event.setJoinMessage(grade.getPrefix() + "[" + grade.getTabName() + "] §f" + player.getName() + " §ea rejoint le serveur");
        
        // Mettre à jour le display name pour le chat
        String displayName = grade.getPrefix() + "[" + grade.getTabName() + "] §f" + player.getName() + "§r";
        player.setDisplayName(displayName);
    }
}

