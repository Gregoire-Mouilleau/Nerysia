package com.nerysia.plugin.game.focus.gui;

import com.nerysia.plugin.Nerysia;
import com.nerysia.plugin.game.focus.FocusGame;
import com.nerysia.plugin.game.focus.FocusGameManager;
import com.nerysia.plugin.game.focus.FocusGameSettings;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class FocusSettingsGUI implements Listener {
    
    private final Nerysia plugin;
    private final FocusGameManager gameManager;
    private Inventory inventory;
    private Player viewer;
    
    public FocusSettingsGUI(Nerysia plugin, FocusGameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }
    
    public void open(Player player) {
        FocusGame game = gameManager.getPlayerGame(player.getUniqueId());
        
        if (game == null) {
            player.sendMessage("§c[Focus] §7Vous n'êtes pas dans une partie !");
            return;
        }
        
        this.viewer = player;
        this.inventory = Bukkit.createInventory(null, 27, "§e§lFocus - Paramètres");
        
        updateItems(game);
        player.openInventory(inventory);
    }
    
    private void updateItems(FocusGame game) {
        inventory.clear();
        FocusGameSettings settings = game.getSettings();
        
        // Nombre de joueurs maximum
        ItemStack maxPlayers = new ItemStack(Material.SKULL_ITEM, settings.getMaxPlayers(), (short) 3);
        ItemMeta maxPlayersMeta = maxPlayers.getItemMeta();
        maxPlayersMeta.setDisplayName("§a§lNombre de Joueurs Max");
        List<String> maxPlayersLore = new ArrayList<>();
        maxPlayersLore.add("§7Actuel: §e" + settings.getMaxPlayers() + " joueurs");
        maxPlayersLore.add("");
        maxPlayersLore.add("§e➤ Clic gauche: §a+1");
        maxPlayersLore.add("§e➤ Clic droit: §c-1");
        maxPlayersMeta.setLore(maxPlayersLore);
        maxPlayers.setItemMeta(maxPlayersMeta);
        inventory.setItem(10, maxPlayers);
        
        // Difficulté
        ItemStack difficulty = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta difficultyMeta = difficulty.getItemMeta();
        difficultyMeta.setDisplayName("§c§lDifficulté");
        List<String> difficultyLore = new ArrayList<>();
        difficultyLore.add("§7Actuel: " + settings.getDifficultyName());
        difficultyLore.add("");
        difficultyLore.add("§a1. Facile");
        difficultyLore.add("§e2. Normal");
        difficultyLore.add("§c3. Difficile");
        difficultyLore.add("");
        difficultyLore.add("§e➤ Cliquez pour changer");
        difficultyMeta.setLore(difficultyLore);
        difficulty.setItemMeta(difficultyMeta);
        inventory.setItem(12, difficulty);
        
        // Temps par round
        ItemStack roundTime = new ItemStack(Material.WATCH);
        ItemMeta roundTimeMeta = roundTime.getItemMeta();
        roundTimeMeta.setDisplayName("§b§lTemps par Round");
        List<String> roundTimeLore = new ArrayList<>();
        roundTimeLore.add("§7Actuel: §e" + settings.getRoundTime() + " secondes");
        roundTimeLore.add("");
        roundTimeLore.add("§e➤ Clic gauche: §a+10s");
        roundTimeLore.add("§e➤ Clic droit: §c-10s");
        roundTimeLore.add("§e➤ Shift + Clic gauche: §a+30s");
        roundTimeLore.add("§e➤ Shift + Clic droit: §c-30s");
        roundTimeMeta.setLore(roundTimeLore);
        roundTime.setItemMeta(roundTimeMeta);
        inventory.setItem(14, roundTime);
        
        // Spectateurs autorisés
        ItemStack spectators = new ItemStack(settings.isAllowSpectators() ? Material.EMERALD : Material.REDSTONE);
        ItemMeta spectatorsMeta = spectators.getItemMeta();
        spectatorsMeta.setDisplayName("§d§lAutoriser les Spectateurs");
        List<String> spectatorsLore = new ArrayList<>();
        spectatorsLore.add("§7État: " + (settings.isAllowSpectators() ? "§aActivé" : "§cDésactivé"));
        spectatorsLore.add("");
        spectatorsLore.add("§e➤ Cliquez pour " + (settings.isAllowSpectators() ? "§cdésactiver" : "§aactiver"));
        spectatorsMeta.setLore(spectatorsLore);
        spectators.setItemMeta(spectatorsMeta);
        inventory.setItem(16, spectators);
        
        // Bouton retour
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("§e§lRetour");
        back.setItemMeta(backMeta);
        inventory.setItem(22, back);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) {
            return;
        }
        
        event.setCancelled(true);
        
        Player player = (Player) event.getWhoClicked();
        FocusGame game = gameManager.getPlayerGame(player.getUniqueId());
        
        if (game == null) {
            player.closeInventory();
            return;
        }
        
        // Si le joueur n'est pas l'hôte, il ne peut rien modifier
        if (!game.isHost(player.getUniqueId())) {
            player.sendMessage("§c[Focus] §7Seul l'hôte peut modifier les paramètres !");
            return;
        }
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }
        
        FocusGameSettings settings = game.getSettings();
        boolean needUpdate = false;
        
        switch (event.getSlot()) {
            case 10: // Nombre de joueurs
                if (event.isLeftClick()) {
                    if (settings.getMaxPlayers() < 16) {
                        settings.setMaxPlayers(settings.getMaxPlayers() + 1);
                        needUpdate = true;
                    }
                } else if (event.isRightClick()) {
                    if (settings.getMaxPlayers() > game.getPlayerCount() && settings.getMaxPlayers() > 2) {
                        settings.setMaxPlayers(settings.getMaxPlayers() - 1);
                        needUpdate = true;
                    }
                }
                break;
                
            case 12: // Difficulté
                int newDifficulty = settings.getDifficulty() + 1;
                if (newDifficulty > 3) newDifficulty = 1;
                settings.setDifficulty(newDifficulty);
                needUpdate = true;
                break;
                
            case 14: // Temps par round
                if (event.isShiftClick()) {
                    if (event.isLeftClick()) {
                        settings.setRoundTime(Math.min(300, settings.getRoundTime() + 30));
                        needUpdate = true;
                    } else if (event.isRightClick()) {
                        settings.setRoundTime(Math.max(30, settings.getRoundTime() - 30));
                        needUpdate = true;
                    }
                } else {
                    if (event.isLeftClick()) {
                        settings.setRoundTime(Math.min(300, settings.getRoundTime() + 10));
                        needUpdate = true;
                    } else if (event.isRightClick()) {
                        settings.setRoundTime(Math.max(30, settings.getRoundTime() - 10));
                        needUpdate = true;
                    }
                }
                break;
                
            case 16: // Spectateurs
                settings.setAllowSpectators(!settings.isAllowSpectators());
                needUpdate = true;
                break;
                
            case 22: // Retour
                player.closeInventory();
                return;
        }
        
        if (needUpdate) {
            updateItems(game);
        }
    }
}
