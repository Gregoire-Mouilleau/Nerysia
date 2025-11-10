package com.nerysia.plugin.lobby.listeners;

import com.nerysia.plugin.Nerysia;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LobbyInventoryListener implements Listener {

    private Nerysia plugin;

    public LobbyInventoryListener(Nerysia plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            
            // Vérifier si le joueur est dans le monde Lobby et clique dans son propre inventaire
            if (player.getWorld().getName().equals("Lobby")) {
                // Vérifier si le joueur clique dans l'inventaire "Paramètres"
                if (event.getView().getTitle().equals("§6Paramètres")) {
                    event.setCancelled(true); // Empêcher de prendre l'item
                    
                    // Vérifier si le joueur a cliqué sur le slot 8 (bloc de visibilité)
                    if (event.getSlot() == 8 && event.getCurrentItem() != null) {
                        Material currentMaterial = event.getCurrentItem().getType();
                        
                        // Basculer entre bloc d'émeraude et bloc de redstone
                        if (currentMaterial == Material.EMERALD_BLOCK || currentMaterial == Material.REDSTONE_BLOCK) {
                            boolean newState = plugin.getPlayerVisibilityManager().toggleVisibility(player.getUniqueId());
                            
                            // Mettre à jour l'item dans l'inventaire
                            Material newMaterial = newState ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK;
                            ItemStack newItem = new ItemStack(newMaterial, 1);
                            ItemMeta meta = newItem.getItemMeta();
                            
                            if (newState) {
                                meta.setDisplayName("§a§lJoueurs Visibles");
                                player.sendMessage("§a✓ Les joueurs sont maintenant visibles");
                            } else {
                                meta.setDisplayName("§c§lJoueurs Masqués");
                                player.sendMessage("§c✗ Les joueurs sont maintenant masqués");
                            }
                            
                            newItem.setItemMeta(meta);
                            event.getInventory().setItem(8, newItem);
                        }
                    }
                    return;
                }
                
                // Autoriser seulement si le joueur est en gamemode créatif (1)
                if (player.getGameMode() != GameMode.CREATIVE) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        
        // Vérifier si le joueur est dans le monde Lobby
        if (player.getWorld().getName().equals("Lobby")) {
            // Autoriser seulement si le joueur est en gamemode créatif (1)
            if (player.getGameMode() != GameMode.CREATIVE) {
                event.setCancelled(true);
            }
        }
    }
}
