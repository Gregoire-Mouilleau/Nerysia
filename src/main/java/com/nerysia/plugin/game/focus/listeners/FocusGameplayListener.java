package com.nerysia.plugin.game.focus.listeners;

import com.nerysia.plugin.Nerysia;
import com.nerysia.plugin.game.focus.FocusGame;
import com.nerysia.plugin.game.focus.FocusGameController;
import com.nerysia.plugin.game.focus.FocusGameManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Gère les événements de gameplay Focus (mort, dégâts, etc.)
 */
public class FocusGameplayListener implements Listener {
    
    private final FocusGameManager gameManager;
    private final FocusItemsListener itemsListener;
    private static final Set<UUID> deadPlayersThisRound = new HashSet<>();
    
    public FocusGameplayListener(FocusGameManager gameManager, FocusItemsListener itemsListener) {
        this.gameManager = gameManager;
        this.itemsListener = itemsListener;
    }
    
    /**
     * Réinitialiser les joueurs morts au début d'un nouveau round
     */
    public static void clearDeadPlayers() {
        deadPlayersThisRound.clear();
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        
        // Vérifier si le joueur est dans une partie Focus
        FocusGame game = gameManager.getPlayerGame(victim.getUniqueId());
        if (game == null) return;
        
        // Récupérer le controller
        FocusGameController controller = gameManager.getGameController(game);
        if (controller == null) return;
        
        // Vérifier si le joueur a été tué par une mine
        if (killer == null && itemsListener != null) {
            UUID mineKillerUUID = itemsListener.getMineKiller(victim.getUniqueId());
            if (mineKillerUUID != null) {
                killer = Bukkit.getPlayer(mineKillerUUID);
                Bukkit.getLogger().info("[Focus] Mort par mine - Poseur: " + (killer != null ? killer.getName() : "null"));
            }
        }
        
        // Marquer le joueur comme mort ce round
        deadPlayersThisRound.add(victim.getUniqueId());
        
        Bukkit.getLogger().info("[Focus] Mort de " + victim.getName() + " - Tueur: " + (killer != null ? killer.getName() : "null"));
        
        // Message de mort personnalisé
        if (killer != null && killer != victim) {
            String deathMessage = ChatColor.RED + "💀 " + ChatColor.GRAY + victim.getName() + 
                                 ChatColor.DARK_GRAY + " ➜ " + 
                                 ChatColor.GOLD + killer.getName();
            // Broadcast à tous les joueurs de la partie
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (gameManager.getPlayerGame(player.getUniqueId()) == game) {
                    player.sendMessage(deathMessage);
                }
            }
        }
        
        // Gérer la mort dans le controller
        controller.onPlayerDeath(victim, killer);
        
        // Stocker la position de mort EXACTE
        Location deathLoc = victim.getLocation().clone();
        victim.setMetadata("focus_death_loc", new FixedMetadataValue(
            Nerysia.getInstance(), 
            deathLoc
        ));
        
        Bukkit.getLogger().info("[Focus] Position de mort stockée: " + deathLoc);
        
        // Sauvegarder les items consommables avant de mourir
        controller.savePlayerConsumables(victim);
        
        // Empêcher le drop et message par défaut
        event.getDrops().clear();
        event.setDroppedExp(0);
        event.setDeathMessage(null);
        event.setKeepInventory(false);
        event.setKeepLevel(true);
        
        // Forcer respawn immédiat
        Bukkit.getScheduler().runTaskLater(Nerysia.getInstance(), () -> {
            victim.spigot().respawn();
        }, 2L);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        
        // Vérifier si le joueur est dans une partie Focus
        FocusGame game = gameManager.getPlayerGame(player.getUniqueId());
        if (game == null) return;
        
        // Si pas mort ce round, ignorer
        if (!deadPlayersThisRound.contains(player.getUniqueId())) return;
        
        deadPlayersThisRound.remove(player.getUniqueId());
        
        // Récupérer la position de mort
        Location loc = player.hasMetadata("focus_death_loc") 
            ? (Location) player.getMetadata("focus_death_loc").get(0).value() 
            : player.getLocation();
        
        Bukkit.getLogger().info("[Focus] Respawn de " + player.getName() + " à " + loc);
        
        // Première correction à 10 ticks
        Bukkit.getScheduler().runTaskLater(Nerysia.getInstance(), () -> {
            player.teleport(loc);
            player.setGameMode(GameMode.SPECTATOR);
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));
            Bukkit.getLogger().info("[Focus] TP forcé (10 ticks): " + loc);
        }, 10L);
        
        // Deuxième correction à 15 ticks pour contrecarrer tout plugin tiers ou reset tardif
        Bukkit.getScheduler().runTaskLater(Nerysia.getInstance(), () -> {
            player.teleport(loc);
            player.setGameMode(GameMode.SPECTATOR);
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));
            player.setAllowFlight(true);
            player.setFlying(true);
            player.getInventory().clear();
            player.sendMessage(ChatColor.GRAY + "Vous êtes maintenant en mode spectateur.");
            Bukkit.getLogger().info("[Focus] TP forcé final (15 ticks): " + loc);
        }, 15L);
    }
}
