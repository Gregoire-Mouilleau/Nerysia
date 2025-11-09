package com.nerysia.plugin.lobby.listeners;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class LobbyBlockListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        
        // Vérifier si le joueur est dans le monde Lobby
        if (player.getWorld().getName().equals("Lobby")) {
            // Autoriser seulement si le joueur est en gamemode créatif (1)
            if (player.getGameMode() != GameMode.CREATIVE) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
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
