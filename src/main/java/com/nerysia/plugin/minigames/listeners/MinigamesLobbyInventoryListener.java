package com.nerysia.plugin.minigames.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.entity.Player;

public class MinigamesLobbyInventoryListener implements Listener {
    
    /**
     * Empêcher de jeter des items dans le spawn_minijeux
     */
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        
        // Vérifier si le joueur est dans le monde spawn_minijeux
        if (!player.getWorld().getName().equals("spawn_minijeux")) {
            return;
        }
        
        // Annuler le jet d'item
        event.setCancelled(true);
    }
    
    /**
     * Empêcher de déplacer les items spéciaux de l'hôte dans le spawn_minijeux
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        
        // Vérifier si le joueur est dans le monde spawn_minijeux
        if (!player.getWorld().getName().equals("spawn_minijeux")) {
            return;
        }
        
        // Vérifier si l'item cliqué est un item spécial (Paramètres ou Gestion)
        if (event.getCurrentItem() != null && event.getCurrentItem().hasItemMeta()) {
            String displayName = event.getCurrentItem().getItemMeta().getDisplayName();
            
            if (displayName != null && 
                (displayName.equals("§e§lParamètres du Jeu") || 
                 displayName.equals("§b§lGestion des Joueurs"))) {
                // Empêcher de déplacer ces items
                event.setCancelled(true);
            }
        }
    }
}
