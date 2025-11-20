package com.nerysia.plugin.game.focus.gui;

import com.nerysia.plugin.Nerysia;
import com.nerysia.plugin.game.focus.FocusGame;
import com.nerysia.plugin.game.focus.FocusGameManager;
import com.nerysia.plugin.game.focus.FocusGameSettings;
import com.nerysia.plugin.game.focus.FocusGameSettings.VictoryCondition;
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
        this.inventory = Bukkit.createInventory(null, 54, "§e§lFocus - Paramètres");
        
        updateItems(game);
        player.openInventory(inventory);
    }
    
    private void updateItems(FocusGame game) {
        inventory.clear();
        FocusGameSettings settings = game.getSettings();
        
        // Vitre grise partout
        ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, glass);
        }
        
        // Nombre de joueurs maximum (slot 10)
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
        
        // Condition de victoire (slot 13)
        ItemStack victoryCondition = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta victoryMeta = victoryCondition.getItemMeta();
        victoryMeta.setDisplayName("§6§lCondition de Victoire");
        List<String> victoryLore = new ArrayList<>();
        victoryLore.add("§7Actuel: " + settings.getVictoryConditionName());
        victoryLore.add("");
        victoryLore.add("§e➤ Cliquez pour changer:");
        victoryLore.add((settings.getVictoryCondition() == VictoryCondition.KILLS ? "§a➤ " : "§7  ") + "Kills");
        victoryLore.add((settings.getVictoryCondition() == VictoryCondition.ROUNDS ? "§a➤ " : "§7  ") + "Rounds");
        victoryLore.add((settings.getVictoryCondition() == VictoryCondition.KILLS_AND_ROUNDS ? "§a➤ " : "§7  ") + "Kills + Rounds");
        victoryMeta.setLore(victoryLore);
        victoryCondition.setItemMeta(victoryMeta);
        inventory.setItem(13, victoryCondition);
        
        // Kills pour gagner (slot 20) - visible si KILLS ou KILLS_AND_ROUNDS
        if (settings.getVictoryCondition() == VictoryCondition.KILLS || 
            settings.getVictoryCondition() == VictoryCondition.KILLS_AND_ROUNDS) {
            ItemStack killsItem = new ItemStack(Material.SKULL_ITEM, 1, (short) 1);
            ItemMeta killsMeta = killsItem.getItemMeta();
            killsMeta.setDisplayName("§c§lKills pour gagner");
            List<String> killsLore = new ArrayList<>();
            killsLore.add("§7Actuel: §e" + settings.getKillsToWin() + " kills");
            killsLore.add("");
            killsLore.add("§e➤ Clic gauche: §a+1");
            killsLore.add("§e➤ Clic droit: §c-1");
            killsLore.add("§e➤ Shift + Clic: §a±5");
            killsMeta.setLore(killsLore);
            killsItem.setItemMeta(killsMeta);
            inventory.setItem(20, killsItem);
        }
        
        // Rounds pour gagner (slot 24) - visible si ROUNDS ou KILLS_AND_ROUNDS
        if (settings.getVictoryCondition() == VictoryCondition.ROUNDS || 
            settings.getVictoryCondition() == VictoryCondition.KILLS_AND_ROUNDS) {
            ItemStack roundsItem = new ItemStack(Material.WATCH);
            ItemMeta roundsMeta = roundsItem.getItemMeta();
            roundsMeta.setDisplayName("§a§lRounds pour gagner");
            List<String> roundsLore = new ArrayList<>();
            roundsLore.add("§7Actuel: §e" + settings.getRoundsToWin() + " rounds");
            roundsLore.add("");
            roundsLore.add("§e➤ Clic gauche: §a+1");
            roundsLore.add("§e➤ Clic droit: §c-1");
            roundsLore.add("§e➤ Shift + Clic: §a±5");
            roundsMeta.setLore(roundsLore);
            roundsItem.setItemMeta(roundsMeta);
            inventory.setItem(24, roundsItem);
        }
        
        // Points par kill (slot 29)
        ItemStack killPoints = new ItemStack(Material.GOLD_NUGGET, Math.max(1, settings.getPointsPerKill()));
        ItemMeta killPointsMeta = killPoints.getItemMeta();
        killPointsMeta.setDisplayName("§6§lPoints par Kill");
        List<String> killPointsLore = new ArrayList<>();
        killPointsLore.add("§7Actuel: §e" + settings.getPointsPerKill() + " points");
        killPointsLore.add("");
        killPointsLore.add("§7Points gagnés à chaque kill");
        killPointsLore.add("");
        killPointsLore.add("§e➤ Clic gauche: §a+1");
        killPointsLore.add("§e➤ Clic droit: §c-1");
        killPointsMeta.setLore(killPointsLore);
        killPoints.setItemMeta(killPointsMeta);
        inventory.setItem(29, killPoints);
        
        // Points 1ère place (slot 33)
        ItemStack firstPlace = new ItemStack(Material.GOLD_INGOT, Math.max(1, settings.getFirstPlacePoints()));
        ItemMeta firstPlaceMeta = firstPlace.getItemMeta();
        firstPlaceMeta.setDisplayName("§e§lPoints 1ère Place");
        List<String> firstPlaceLore = new ArrayList<>();
        firstPlaceLore.add("§7Actuel: §e" + settings.getFirstPlacePoints() + " points");
        firstPlaceLore.add("");
        firstPlaceLore.add("§7Distribution automatique:");
        firstPlaceLore.add("§a 1er: §e" + settings.getPointsForPlacement(1) + " pts");
        firstPlaceLore.add("§7 2ème: §e" + settings.getPointsForPlacement(2) + " pts");
        firstPlaceLore.add("§7 3ème: §e" + settings.getPointsForPlacement(3) + " pts");
        firstPlaceLore.add("§7 4ème: §e" + settings.getPointsForPlacement(4) + " pts");
        firstPlaceLore.add("§7 5ème: §e" + settings.getPointsForPlacement(5) + " pts");
        firstPlaceLore.add("§7 6ème: §e" + settings.getPointsForPlacement(6) + " pts");
        firstPlaceLore.add("§7 7ème+: §e" + settings.getPointsForPlacement(7) + " pts");
        firstPlaceLore.add("");
        firstPlaceLore.add("§e➤ Clic gauche: §a+1");
        firstPlaceLore.add("§e➤ Clic droit: §c-1");
        firstPlaceMeta.setLore(firstPlaceLore);
        firstPlace.setItemMeta(firstPlaceMeta);
        inventory.setItem(33, firstPlace);
        
        // Bouton reset (slot 40)
        ItemStack reset = new ItemStack(Material.BARRIER);
        ItemMeta resetMeta = reset.getItemMeta();
        resetMeta.setDisplayName("§c§lRéinitialiser les paramètres");
        List<String> resetLore = new ArrayList<>();
        resetLore.add("");
        resetLore.add("§7Remet tous les paramètres");
        resetLore.add("§7à leurs valeurs par défaut");
        resetLore.add("");
        resetLore.add("§e➤ Cliquez pour réinitialiser");
        resetMeta.setLore(resetLore);
        reset.setItemMeta(resetMeta);
        inventory.setItem(40, reset);
        
        // Bouton retour (slot 49)
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("§e§lRetour");
        back.setItemMeta(backMeta);
        inventory.setItem(49, back);
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
                
            case 13: // Condition de victoire
                VictoryCondition current = settings.getVictoryCondition();
                VictoryCondition next;
                
                if (current == VictoryCondition.KILLS) {
                    next = VictoryCondition.ROUNDS;
                } else if (current == VictoryCondition.ROUNDS) {
                    next = VictoryCondition.KILLS_AND_ROUNDS;
                } else {
                    next = VictoryCondition.KILLS;
                }
                
                settings.setVictoryCondition(next);
                player.sendMessage("§a[Focus] §7Condition de victoire: " + settings.getVictoryConditionName());
                needUpdate = true;
                break;
                
            case 20: // Kills pour gagner
                int currentKills = settings.getKillsToWin();
                if (event.isShiftClick()) {
                    if (event.isLeftClick()) {
                        settings.setKillsToWin(currentKills + 5);
                    } else if (event.isRightClick()) {
                        settings.setKillsToWin(currentKills - 5);
                    }
                } else {
                    if (event.isLeftClick()) {
                        settings.setKillsToWin(currentKills + 1);
                    } else if (event.isRightClick()) {
                        settings.setKillsToWin(currentKills - 1);
                    }
                }
                player.sendMessage("§a[Focus] §7Kills pour gagner: §e" + settings.getKillsToWin());
                needUpdate = true;
                break;
                
            case 24: // Rounds pour gagner
                int currentRounds = settings.getRoundsToWin();
                if (event.isShiftClick()) {
                    if (event.isLeftClick()) {
                        settings.setRoundsToWin(currentRounds + 5);
                    } else if (event.isRightClick()) {
                        settings.setRoundsToWin(currentRounds - 5);
                    }
                } else {
                    if (event.isLeftClick()) {
                        settings.setRoundsToWin(currentRounds + 1);
                    } else if (event.isRightClick()) {
                        settings.setRoundsToWin(currentRounds - 1);
                    }
                }
                player.sendMessage("§a[Focus] §7Rounds pour gagner: §e" + settings.getRoundsToWin());
                needUpdate = true;
                break;
                
            case 29: // Points par kill
                int currentKillPoints = settings.getPointsPerKill();
                if (event.isLeftClick()) {
                    settings.setPointsPerKill(currentKillPoints + 1);
                } else if (event.isRightClick()) {
                    settings.setPointsPerKill(currentKillPoints - 1);
                }
                player.sendMessage("§a[Focus] §7Points par kill: §e" + settings.getPointsPerKill());
                needUpdate = true;
                break;
                
            case 33: // Points 1ère place
                int currentFirstPlace = settings.getFirstPlacePoints();
                if (event.isLeftClick()) {
                    settings.setFirstPlacePoints(currentFirstPlace + 1);
                } else if (event.isRightClick()) {
                    settings.setFirstPlacePoints(currentFirstPlace - 1);
                }
                player.sendMessage("§a[Focus] §7Points 1ère place: §e" + settings.getFirstPlacePoints());
                needUpdate = true;
                break;
                
            case 40: // Reset
                settings.resetToDefaults();
                player.sendMessage("§a[Focus] §7Paramètres réinitialisés aux valeurs par défaut !");
                needUpdate = true;
                break;
                
            case 49: // Retour
                player.closeInventory();
                return;
        }
        
        if (needUpdate) {
            updateItems(game);
        }
    }
}
