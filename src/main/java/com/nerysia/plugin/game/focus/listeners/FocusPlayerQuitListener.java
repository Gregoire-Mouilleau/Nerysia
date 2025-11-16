package com.nerysia.plugin.game.focus.listeners;

import com.nerysia.plugin.Nerysia;
import com.nerysia.plugin.game.focus.FocusGame;
import com.nerysia.plugin.game.focus.FocusGameManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class FocusPlayerQuitListener implements Listener {
    
    private final Nerysia plugin;
    private final FocusGameManager gameManager;
    
    public FocusPlayerQuitListener(Nerysia plugin, FocusGameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Vérifier si le joueur est dans une partie Focus
        FocusGame game = gameManager.getPlayerGame(player.getUniqueId());
        
        if (game != null) {
            // Si c'est l'hôte qui quitte, supprimer la partie
            if (game.isHost(player.getUniqueId())) {
                Bukkit.getLogger().info("[Focus] L'hôte " + player.getName() + " a quitté, suppression de la partie " + game.getGameId());
                
                // Notifier et téléporter tous les autres joueurs
                for (java.util.UUID playerId : game.getPlayers()) {
                    if (!playerId.equals(player.getUniqueId())) {
                        Player p = Bukkit.getPlayer(playerId);
                        if (p != null) {
                            p.sendMessage("§c[Focus] §7L'hôte a quitté, la partie a été supprimée !");
                            teleportToLobby(p);
                        }
                    }
                }
                
                // Supprimer la partie
                gameManager.deleteGame(game.getGameId());
            } else {
                // Joueur normal qui quitte
                Bukkit.getLogger().info("[Focus] Le joueur " + player.getName() + " a quitté la partie " + game.getGameId());
                gameManager.removePlayerFromGame(player.getUniqueId());
                
                // Message unique à tous les joueurs restants
                String message = "§c[Focus] §e" + player.getName() + " §7a quitté la partie ! §8(§c" + game.getPlayerCount() + "§8/§e" + game.getSettings().getMaxPlayers() + "§8)";
                for (java.util.UUID playerId : game.getPlayers()) {
                    Player p = Bukkit.getPlayer(playerId);
                    if (p != null) {
                        p.sendMessage(message);
                    }
                }
                
                // Vérifier si le round doit se terminer (1 seul joueur vivant)
                com.nerysia.plugin.game.focus.FocusGameController controller = gameManager.getGameController(game);
                if (controller != null) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        controller.checkRoundEnd();
                    }, 5L); // Petit délai pour que la déconnexion soit complète
                }
            }
        }
    }
    
    private void teleportToLobby(Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            org.bukkit.World lobby = Bukkit.getWorld("Lobby");
            if (lobby != null) {
                player.teleport(lobby.getSpawnLocation());
                player.getInventory().clear();
                
                // Exécuter la commande /lobby pour réinitialiser et revoir les NPCs
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    player.performCommand("lobby");
                }, 5L);
            }
        });
    }
}
