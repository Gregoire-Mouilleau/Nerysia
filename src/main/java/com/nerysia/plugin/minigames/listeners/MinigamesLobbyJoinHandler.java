package com.nerysia.plugin.minigames.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Gère l'arrivée des joueurs dans le spawn_minijeux
 */
public class MinigamesLobbyJoinHandler implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Vérifier si le joueur est dans le monde spawn_minijeux
        if (!player.getWorld().getName().equals("spawn_minijeux")) {
            return;
        }
        
        // Téléporter le joueur au spawn du monde spawn_minijeux
        World minigamesWorld = Bukkit.getWorld("spawn_minijeux");
        if (minigamesWorld != null) {
            Location spawnLocation = new Location(minigamesWorld, .5, 104, 0.5);
            spawnLocation.setYaw(0f);
            spawnLocation.setPitch(0f);
            player.teleport(spawnLocation);
        }
    }
}
