package com.nerysia.plugin.game.gui;

import com.nerysia.plugin.Nerysia;
import com.nerysia.plugin.game.Game;
import com.nerysia.plugin.game.GameMode;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GameModeMenuGUI {
    
    private final Nerysia plugin;
    private final GameMode gameMode;
    
    public GameModeMenuGUI(Nerysia plugin, GameMode gameMode) {
        this.plugin = plugin;
        this.gameMode = gameMode;
    }
    
    public void open(Player player) {
        // Limiter le titre à 32 caractères max (limitation Minecraft)
        String title = gameMode.getDisplayName() + " §8- §7Menu";
        if (title.length() > 32) {
            title = gameMode.getDisplayName();
        }
        Inventory inv = Bukkit.createInventory(null, 54, title);
        
        // Bouton pour créer une partie (slot 49)
        ItemStack createButton = new ItemStack(Material.EMERALD, 1);
        ItemMeta createMeta = createButton.getItemMeta();
        createMeta.setDisplayName("§a§lCréer une partie");
        List<String> createLore = new ArrayList<>();
        createLore.add("§7Cliquez pour créer");
        createLore.add("§7une nouvelle partie " + gameMode.getDisplayName());
        createMeta.setLore(createLore);
        createButton.setItemMeta(createMeta);
        inv.setItem(49, createButton);
        
        // Afficher les parties disponibles pour ce mode
        List<Game> games = plugin.getGameManager().getGamesByMode(gameMode);
        int slot = 0;
        
        for (Game game : games) {
            if (slot >= 45) break; // Maximum 45 parties affichées
            
            ItemStack gameItem = createGameItem(game);
            inv.setItem(slot, gameItem);
            slot++;
        }
        
        // Si aucune partie
        if (games.isEmpty()) {
            ItemStack noGame = new ItemStack(Material.BARRIER, 1);
            ItemMeta noGameMeta = noGame.getItemMeta();
            noGameMeta.setDisplayName("§c§lAucune partie disponible");
            List<String> noGameLore = new ArrayList<>();
            noGameLore.add("§7Créez une nouvelle partie");
            noGameLore.add("§7en cliquant sur l'émeraude !");
            noGameMeta.setLore(noGameLore);
            noGame.setItemMeta(noGameMeta);
            inv.setItem(22, noGame);
        }
        
        player.openInventory(inv);
    }
    
    private ItemStack createGameItem(Game game) {
        ItemStack item = new ItemStack(game.getMode().getIcon(), 1);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName("§f" + game.getName());
        
        List<String> lore = new ArrayList<>();
        lore.add("§8" + game.getId());
        lore.add("");
        lore.add("§7État: " + game.getState().getDisplayName());
        lore.add("§7Joueurs: §e" + game.getPlayerCount() + "§7/§e" + game.getMaxPlayers());
        lore.add("");
        
        if (game.isPrivate()) {
            lore.add("§6§l🔒 PARTIE PRIVÉE");
            lore.add("");
        }
        
        switch (game.getState()) {
            case DISPONIBLE:
            case SUR_DEMANDE:
            case PRIVEE:
                if (!game.isFull()) {
                    lore.add("§a§l► Clic pour rejoindre");
                } else {
                    lore.add("§c§l✖ Partie complète");
                }
                break;
            case EN_COURS:
                lore.add("§e§l⚔ Partie en cours");
                lore.add("§7Clic pour spectater");
                break;
            case CREATION:
                lore.add("§e§l⚙ En création...");
                break;
            case TERMINEE:
                lore.add("§7§l✓ Terminée");
                break;
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
    
    public GameMode getGameMode() {
        return gameMode;
    }
}
