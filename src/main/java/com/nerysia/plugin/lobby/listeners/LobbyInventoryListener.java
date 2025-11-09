package com.nerysia.plugin.lobby.listeners;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class LobbyInventoryListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            
            // Vérifier si le joueur est dans le monde Lobby et clique dans son propre inventaire
            if (player.getWorld().getName().equals("Lobby")) {
                // Autoriser seulement si le joueur est en gamemode créatif (1)
                if (player.getGameMode() != GameMode.CREATIVE) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        
        // Vérifier si le joueur est dans le monde Lobby
        if (player.getWorld().getName().equals("Lobby")) {
            // Autoriser seulement si le joueur est en gamemode créatif (1)
            if (player.getGameMode() != GameMode.CREATIVE) {
                event.setCancelled(true);
            }
        }
    }
}
