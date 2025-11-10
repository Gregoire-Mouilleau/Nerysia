package com.nerysia.plugin.lobby.handlers;

import com.nerysia.plugin.Nerysia;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LobbyItemInteractHandler implements Listener {

    private Nerysia plugin;

    public LobbyItemInteractHandler(Nerysia plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getItemInHand();
        
        // Vérifier si le joueur a fait un clic droit ou gauche
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK ||
            event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            
            // Vérifier si l'item en main est un compas
            if (item != null && item.getType() == Material.COMPASS) {
                event.setCancelled(true);
                
                // Créer un inventaire de 54 slots (6 lignes de 9 slots)
                Inventory inv = Bukkit.createInventory(null, 54, "§6Menu Principal");
                
                // Vous pouvez ajouter des items dans le panneau ici
                // Exemple:
                // inv.setItem(0, new ItemStack(Material.DIAMOND));
                
                // Ouvrir l'inventaire au joueur
                player.openInventory(inv);
            }
            
            // Vérifier si l'item en main est un comparateur (paramètres)
            if (item != null && item.getType() == Material.REDSTONE_COMPARATOR) {
                event.setCancelled(true);
                
                // Créer un inventaire de 9 slots (1 ligne de 9 slots)
                Inventory inv = Bukkit.createInventory(null, 9, "§6Paramètres");
                
                // Ajouter le bloc de visibilité au slot 8 (dernier slot à droite)
                boolean playersVisible = plugin.getPlayerVisibilityManager().arePlayersVisible(player.getUniqueId());
                Material visibilityMaterial = playersVisible ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK;
                ItemStack visibilityItem = new ItemStack(visibilityMaterial, 1);
                ItemMeta visibilityMeta = visibilityItem.getItemMeta();
                
                if (playersVisible) {
                    visibilityMeta.setDisplayName("§a§lJoueurs Visibles");
                } else {
                    visibilityMeta.setDisplayName("§c§lJoueurs Masqués");
                }
                
                visibilityItem.setItemMeta(visibilityMeta);
                inv.setItem(8, visibilityItem);
                
                // Ouvrir l'inventaire au joueur
                player.openInventory(inv);
            }
        }
    }
}
