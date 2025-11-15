package com.nerysia.plugin.game.focus.listeners;

import com.nerysia.plugin.game.focus.FocusGame;
import com.nerysia.plugin.game.focus.FocusGameController;
import com.nerysia.plugin.game.focus.FocusGameManager;
import com.nerysia.plugin.game.focus.FocusShopGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;

/**
 * Gère l'interaction avec l'item Shop dans l'inventaire
 */
public class FocusShopItemListener implements Listener {
    
    private final FocusGameManager gameManager;
    private final FocusShopGUI shopGUI;
    
    public FocusShopItemListener(FocusGameManager gameManager, FocusShopGUI shopGUI) {
        this.gameManager = gameManager;
        this.shopGUI = shopGUI;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        // Vérifier si le joueur est dans une partie Focus
        FocusGame game = gameManager.getPlayerGame(player.getUniqueId());
        if (game == null) return;
        
        // Vérifier si c'est un clic droit
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        // Vérifier si l'item dans la main est le Shop
        if (event.getItem() == null) return;
        if (event.getItem().getType() != Material.CHEST) return;
        if (!event.getItem().hasItemMeta()) return;
        if (!event.getItem().getItemMeta().hasDisplayName()) return;
        
        String displayName = ChatColor.stripColor(event.getItem().getItemMeta().getDisplayName());
        if (!displayName.equals("Shop Focus")) return;
        
        event.setCancelled(true);
        
        // Récupérer le controller
        FocusGameController controller = gameManager.getGameController(game);
        if (controller == null) return;
        
        // Ouvrir le shop
        shopGUI.open(player, controller);
    }
}
