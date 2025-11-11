package com.nerysia.plugin.game.npc;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

/**
 * Empêche les joueurs de pousser les NPCs en les repoussant légèrement
 */
public class NPCCollisionPreventer implements Listener {
    
    private final NPCManager npcManager;
    
    public NPCCollisionPreventer(NPCManager npcManager) {
        this.npcManager = npcManager;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // Vérifier s'il y a des NPCs à proximité
        for (Entity entity : player.getNearbyEntities(1.5, 1.5, 1.5)) {
            if (entity.getType() == EntityType.VILLAGER && npcManager.isGameNPC(entity)) {
                // Calculer la distance entre le joueur et le NPC
                double distance = player.getLocation().distance(entity.getLocation());
                
                // Si trop proche (collision potentielle), repousser légèrement le joueur
                if (distance < 0.6) {
                    Vector direction = player.getLocation().toVector()
                        .subtract(entity.getLocation().toVector())
                        .normalize()
                        .multiply(0.3);
                    direction.setY(0); // Ne pas repousser verticalement
                    
                    player.setVelocity(direction);
                    
                    // Téléporter le NPC à sa position d'origine
                    GameNPC npc = npcManager.getNPCFromEntity(entity);
                    if (npc != null) {
                        entity.teleport(npc.getLocation());
                    }
                }
            }
        }
    }
}
