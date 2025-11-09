package com.nerysia.plugin.lobby.listeners;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.WorldLoadEvent;

public class LobbyWeatherListener implements Listener {

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        World world = event.getWorld();
        
        // Si c'est le monde Lobby et qu'il commence à pleuvoir
        if (world.getName().equals("Lobby") && event.toWeatherState()) {
            event.setCancelled(true); // Annuler le changement de météo
        }
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        World world = event.getWorld();
        
        // Configurer le monde Lobby au chargement
        if (world.getName().equals("Lobby")) {
            setupLobbyWorld(world);
        }
    }

    private void setupLobbyWorld(World world) {
        // Désactiver la météo
        world.setStorm(false);
        world.setThundering(false);
        world.setWeatherDuration(Integer.MAX_VALUE);
        
        // Fixer le temps à midi (6000 ticks)
        world.setTime(6000);
        world.setGameRuleValue("doDaylightCycle", "false");
    }
}
