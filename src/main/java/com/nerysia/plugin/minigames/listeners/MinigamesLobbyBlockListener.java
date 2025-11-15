package com.nerysia.plugin.minigames.listeners;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class MinigamesLobbyBlockListener implements Listener {
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        
        // Vérifier si le joueur est dans le monde spawn_minijeux
        if (!player.getWorld().getName().equals("spawn_minijeux")) {
            return;
        }
        
        // Autoriser seulement en mode créatif
        if (player.getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        
        // Vérifier si le joueur est dans le monde spawn_minijeux
        if (!player.getWorld().getName().equals("spawn_minijeux")) {
            return;
        }
        
        // Autoriser seulement en mode créatif
        if (player.getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }
}
