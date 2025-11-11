package com.nerysia.plugin.game.gui;

import com.nerysia.plugin.Nerysia;
import com.nerysia.plugin.game.Game;
import com.nerysia.plugin.game.GameMode;
import com.nerysia.plugin.game.GameState;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class GameGUIListener implements Listener {
    
    private final Nerysia plugin;
    
    public GameGUIListener(Nerysia plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        // Menu d'un mode de jeu spécifique
        if (title.contains("Parties")) {
            event.setCancelled(true);
            
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
                return;
            }
            
            ItemStack item = event.getCurrentItem();
            
            // Bouton créer une partie
            if (item.getType() == Material.EMERALD) {
                // Trouver le mode de jeu depuis le titre
                GameMode mode = findGameModeFromTitle(title);
                if (mode != null) {
                    createGameForMode(player, mode);
                    player.closeInventory();
                }
                return;
            }
            
            // Clic sur une partie existante
            if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
                List<String> lore = item.getItemMeta().getLore();
                if (lore.size() > 0) {
                    String gameId = lore.get(0).replace("§8", "");
                    Game game = plugin.getGameManager().getGame(gameId);
                    
                    if (game != null) {
                        handleGameJoin(player, game);
                    }
                }
            }
        }
    }
    
    private void handleGameJoin(Player player, Game game) {
        // Vérifier si le joueur peut rejoindre
        if (game.getState() == GameState.TERMINEE) {
            player.sendMessage("§cCette partie est terminée !");
            player.closeInventory();
            return;
        }
        
        if (game.isFull()) {
            player.sendMessage("§cCette partie est complète !");
            return;
        }
        
        if (plugin.getGameManager().isPlayerInGame(player.getUniqueId())) {
            player.sendMessage("§cVous êtes déjà dans une partie !");
            return;
        }
        
        // Ajouter le joueur à la partie
        if (game.addPlayer(player.getUniqueId())) {
            player.sendMessage("§aVous avez rejoint la partie §e" + game.getName() + " §a!");
            player.sendMessage("§7Mode: " + game.getMode().getDisplayName());
            player.sendMessage("§7Joueurs: §e" + game.getPlayerCount() + "§7/§e" + game.getMaxPlayers());
            player.closeInventory();
            
            // TODO: Téléporter le joueur dans l'arène
        } else {
            player.sendMessage("§cImpossible de rejoindre cette partie.");
        }
    }
    
    private GameMode findGameModeFromTitle(String title) {
        for (GameMode mode : GameMode.values()) {
            if (title.contains(mode.getDisplayName().replace("§", "").substring(2))) {
                return mode;
            }
        }
        return null;
    }
    
    private void createGameForMode(Player player, GameMode mode) {
        // Vérifier si le joueur est déjà dans une partie
        if (plugin.getGameManager().isPlayerInGame(player.getUniqueId())) {
            player.sendMessage("§cVous êtes déjà dans une partie !");
            return;
        }
        
        // Créer la partie
        String gameName = player.getName() + "'s game";
        Game game = plugin.getGameManager().createGame(gameName, mode, player.getUniqueId());
        game.setState(GameState.DISPONIBLE);
        
        player.sendMessage("§a§lPartie créée avec succès !");
        player.sendMessage("§7Mode: " + mode.getDisplayName());
        player.sendMessage("§7Nom: §e" + gameName);
        player.sendMessage("§7ID: §8" + game.getId());
        
        // TODO: Téléporter le joueur dans l'arène
    }
}
