package com.nerysia.plugin.game.focus.listeners;

import com.nerysia.plugin.game.focus.FocusGame;
import com.nerysia.plugin.game.focus.FocusGameController;
import com.nerysia.plugin.game.focus.FocusGameManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

/**
 * Protections pour les mondes Focus dupliqués
 */
public class FocusWorldProtectionListener implements Listener {
    
    private final FocusGameManager gameManager;
    
    public FocusWorldProtectionListener(FocusGameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        
        Player player = (Player) event.getEntity();
        String worldName = player.getWorld().getName();
        
        // Bloquer la perte de faim dans les mondes Focus (spawn + game)
        // spawn_minijeux = monde original, FOCUS-X_spawn = monde dupliqué, FOCUS-X = monde de jeu
        if (worldName.equals("spawn_minijeux") || worldName.endsWith("_spawn") || worldName.startsWith("FOCUS-")) {
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
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        String worldName = player.getWorld().getName();
        
        // Bloquer la casse de blocs dans spawn Focus (sauf créatif)
        if (worldName.equals("spawn_minijeux") || worldName.endsWith("_spawn")) {
            if (player.getGameMode() != GameMode.CREATIVE) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        String worldName = player.getWorld().getName();
        
        // Bloquer la pose de blocs dans spawn Focus (sauf créatif)
        if (worldName.equals("spawn_minijeux") || worldName.endsWith("_spawn")) {
            if (player.getGameMode() != GameMode.CREATIVE) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPvP(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!(event.getDamager() instanceof Player)) return;
        
        Player victim = (Player) event.getEntity();
        String worldName = victim.getWorld().getName();
        
        // Bloquer le PvP dans le spawn Focus (original + dupliqué)
        if (worldName.equals("spawn_minijeux") || worldName.endsWith("_spawn")) {
            event.setCancelled(true);
            return;
        }
        
        // Bloquer le PvP en phase WAITING/LOBBY dans le monde de jeu
        if (worldName.startsWith("FOCUS-") && !worldName.endsWith("_spawn") && gameManager != null) {
            FocusGame game = gameManager.getPlayerGame(victim.getUniqueId());
            if (game != null) {
                FocusGameController controller = gameManager.getGameController(game);
                if (controller != null) {
                    FocusGameController.State state = controller.getState();
                    if (state == FocusGameController.State.WAITING || 
                        state == FocusGameController.State.LOBBY) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}
