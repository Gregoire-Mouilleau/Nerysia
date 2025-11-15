package com.nerysia.plugin.minigames.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class MinigamesLobbyProtectionListener implements Listener {
    
    /**
     * Empêcher les dégâts dans le spawn_minijeux
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        
        // Vérifier si le joueur est dans le monde spawn_minijeux
        if (!player.getWorld().getName().equals("spawn_minijeux")) {
            return;
        }
        
        // Annuler tous les dégâts
        event.setCancelled(true);
    }
    
    /**
     * Empêcher le PvP dans le spawn_minijeux
     */
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        
        Player victim = (Player) event.getEntity();
        
        // Vérifier si le joueur est dans le monde spawn_minijeux
        if (!victim.getWorld().getName().equals("spawn_minijeux")) {
            return;
        }
        
        // Annuler le PvP
        event.setCancelled(true);
    }
    
    /**
     * Empêcher la perte de nourriture dans le spawn_minijeux
     */
    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        
        // Vérifier si le joueur est dans le monde spawn_minijeux
        if (!player.getWorld().getName().equals("spawn_minijeux")) {
            return;
        }
        
        // Annuler la perte de nourriture
        event.setCancelled(true);
    }
}
