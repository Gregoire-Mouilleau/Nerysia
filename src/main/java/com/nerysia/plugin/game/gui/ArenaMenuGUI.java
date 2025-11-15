package com.nerysia.plugin.game.gui;

import com.nerysia.plugin.Nerysia;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ArenaMenuGUI implements Listener {
    
    private final Nerysia plugin;
    private final Inventory inventory;
    
    public ArenaMenuGUI(Nerysia plugin) {
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(null, 27, "§4§lArena - Menu Principal");
        
        initializeItems();
    }
    
    private void initializeItems() {
        // Item: Rejoindre une partie
        ItemStack joinGame = createItem(
            Material.DIAMOND_SWORD,
            "§a§lRejoindre une Partie",
            "§7Rejoignez une partie d'Arena",
            "§7en cours ou créez-en une nouvelle.",
            "",
            "§e➤ Cliquez pour rejoindre"
        );
        inventory.setItem(11, joinGame);
        
        // Item: Parties en cours
        ItemStack currentGames = createItem(
            Material.PAPER,
            "§b§lParties en Cours",
            "§7Voir la liste des parties",
            "§7d'Arena actuellement en cours.",
            "",
            "§e➤ Cliquez pour voir"
        );
        inventory.setItem(13, currentGames);
        
        // Item: Statistiques
        ItemStack stats = createItem(
            Material.BOOK,
            "§6§lMes Statistiques",
            "§7Consultez vos statistiques",
            "§7personnelles en Arena.",
            "",
            "§e➤ Cliquez pour consulter"
        );
        inventory.setItem(15, stats);
        
        // Item: Fermer
        ItemStack close = createItem(
            Material.BARRIER,
            "§c§lFermer",
            "§7Fermer ce menu",
            "",
            "§e➤ Cliquez pour fermer"
        );
        inventory.setItem(22, close);
    }
    
    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }
    
    public void open(Player player) {
        player.openInventory(inventory);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) {
            return;
        }
        
        event.setCancelled(true);
        
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }
        
        if (!clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName()) {
            return;
        }
        
        String displayName = clickedItem.getItemMeta().getDisplayName();
        
        switch (displayName) {
            case "§a§lRejoindre une Partie":
                player.closeInventory();
                // TODO: Logique pour rejoindre une partie
                player.sendMessage("§a[Arena] §7Recherche d'une partie...");
                player.performCommand("arena join");
                break;
                
            case "§b§lParties en Cours":
                player.closeInventory();
                // TODO: Ouvrir le menu des parties en cours
                player.sendMessage("§b[Arena] §7Affichage des parties en cours...");
                player.performCommand("arena list");
                break;
                
            case "§6§lMes Statistiques":
                player.closeInventory();
                // TODO: Ouvrir le menu des statistiques
                player.sendMessage("§6[Arena] §7Affichage de vos statistiques...");
                player.performCommand("arena stats");
                break;
                
            case "§c§lFermer":
                player.closeInventory();
                break;
        }
    }
}
