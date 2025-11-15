package com.nerysia.plugin.lobby.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Gère le message de déconnexion
 */
public class LobbyQuitListener implements Listener {
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Désactiver le message de déconnexion
        event.setQuitMessage(null);
    }
}
