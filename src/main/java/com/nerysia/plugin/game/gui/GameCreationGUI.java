package com.nerysia.plugin.game.gui;

import com.nerysia.plugin.Nerysia;
import com.nerysia.plugin.game.GameMode;
import com.nerysia.plugin.game.GameType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GameCreationGUI {
    
    private final Nerysia plugin;
    private final GameType gameType;
    
    public GameCreationGUI(Nerysia plugin, GameType gameType) {
        this.plugin = plugin;
        this.gameType = gameType;
    }
    
    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§6Créer une partie");
        
        // Afficher les modes de jeu pour ce type
        int slot = 10;
        for (GameMode mode : GameMode.values()) {
            if (mode.getType() == gameType) {
                ItemStack modeItem = createModeItem(mode);
                inv.setItem(slot, modeItem);
                slot++;
            }
        }
        
        player.openInventory(inv);
    }
    
    private ItemStack createModeItem(GameMode mode) {
        ItemStack item = new ItemStack(mode.getIcon(), 1);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName(mode.getDisplayName());
        
        List<String> lore = new ArrayList<>();
        lore.add("§7" + mode.getDescription());
        lore.add("");
        lore.add("§7Joueurs max: §e" + mode.getMaxPlayers());
        lore.add("");
        lore.add("§a§l► Cliquez pour créer");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
}
