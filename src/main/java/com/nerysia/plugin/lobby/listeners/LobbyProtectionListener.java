package com.nerysia.plugin.lobby.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class LobbyProtectionListener implements Listener {

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            
            // Vérifier si le joueur est dans le monde Lobby
            if (player.getWorld().getName().equals("Lobby")) {
                // Empêcher la perte de faim
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            
            // Vérifier si le joueur est dans le monde Lobby
            if (player.getWorld().getName().equals("Lobby")) {
                // Empêcher tous les dégâts
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            
            // Vérifier si le joueur est dans le monde Lobby
            if (player.getWorld().getName().equals("Lobby")) {
                // Empêcher le PvP
                event.setCancelled(true);
            }
        }
        
        if (event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            
            // Vérifier si l'attaquant est dans le monde Lobby
            if (damager.getWorld().getName().equals("Lobby")) {
                // Empêcher le PvP
                event.setCancelled(true);
            }
        }
    }
}
