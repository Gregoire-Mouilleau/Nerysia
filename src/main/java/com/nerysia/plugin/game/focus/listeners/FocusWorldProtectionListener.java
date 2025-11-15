package com.nerysia.plugin.game.focus.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

/**
 * Protections pour les mondes Focus dupliqués
 */
public class FocusWorldProtectionListener implements Listener {
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        
        Player player = (Player) event.getEntity();
        String worldName = player.getWorld().getName();
        
        // Bloquer la perte de faim dans les mondes Focus
        if (worldName.startsWith("FOCUS-")) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        String worldName = player.getWorld().getName();
        
        // Empêcher le drop d'items dans les mondes Focus
        if (worldName.startsWith("FOCUS-")) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onWeatherChange(WeatherChangeEvent event) {
        String worldName = event.getWorld().getName();
        
        // Empêcher les changements de météo dans les mondes Focus
        if (worldName.startsWith("FOCUS-")) {
            if (event.toWeatherState()) { // Si ça passe à pluie/orage
                event.setCancelled(true);
            }
        }
    }
}
