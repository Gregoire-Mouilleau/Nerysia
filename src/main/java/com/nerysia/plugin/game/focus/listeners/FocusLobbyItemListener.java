package com.nerysia.plugin.game.focus.listeners;

import com.nerysia.plugin.Nerysia;
import com.nerysia.plugin.game.focus.FocusGame;
import com.nerysia.plugin.game.focus.FocusGameController;
import com.nerysia.plugin.game.focus.FocusGameManager;
import com.nerysia.plugin.game.focus.gui.FocusPlayerManagementGUI;
import com.nerysia.plugin.game.focus.gui.FocusSettingsGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class FocusLobbyItemListener implements Listener {
    
    private final Nerysia plugin;
    private final FocusGameManager gameManager;
    private final FocusSettingsGUI settingsGUI;
    private final FocusPlayerManagementGUI playerManagementGUI;
    
    public FocusLobbyItemListener(Nerysia plugin, FocusGameManager gameManager, 
                                  FocusSettingsGUI settingsGUI, FocusPlayerManagementGUI playerManagementGUI) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.settingsGUI = settingsGUI;
        this.playerManagementGUI = playerManagementGUI;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return;
        }
        
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        // Vérifier si le joueur est dans un monde spawn_minijeux (original ou dupliqué)
        String worldName = player.getWorld().getName();
        boolean isInSpawnMinijeux = worldName.equals("spawn_minijeux") || worldName.endsWith("_spawn");
        
        if (!isInSpawnMinijeux) {
            return;
        }
        
        String displayName = item.getItemMeta().getDisplayName();
        
        // Item Paramètres du jeu
        if (displayName.equals("§e§lParamètres du Jeu")) {
            event.setCancelled(true);
            settingsGUI.open(player);
            return;
        }
        
        // Item Gestion des joueurs
        if (displayName.equals("§b§lGestion des Joueurs")) {
            event.setCancelled(true);
            playerManagementGUI.open(player);
            return;
        }
        
        // Item Lancer la partie
        if (displayName.equals("§a§lLancer la Partie")) {
            event.setCancelled(true);
            startGame(player);
            return;
        }
    }
    
    private void startGame(Player host) {
        FocusGame game = gameManager.getPlayerGame(host.getUniqueId());
        
        if (game == null) {
            host.sendMessage("§c[Focus] §7Vous n'êtes pas dans une partie !");
            return;
        }
        
        if (!game.isHost(host.getUniqueId())) {
            host.sendMessage("§c[Focus] §7Seul l'hôte peut lancer la partie !");
            return;
        }
        
        if (game.getPlayerCount() < 2) {
            host.sendMessage("§c[Focus] §7Minimum 2 joueurs requis pour lancer la partie !");
            host.sendMessage("§e[Focus] §7Joueurs actuels: §c" + game.getPlayerCount() + "§7/§e" + game.getSettings().getMaxPlayers());
            return;
        }
        
        // Récupérer le controller
        FocusGameController controller = gameManager.getGameController(game);
        if (controller == null) {
            host.sendMessage("§c[Focus] §7Erreur: Contrôleur de jeu introuvable !");
            return;
        }
        
        // Passer en mode IN_PROGRESS
        game.setState(com.nerysia.plugin.game.focus.FocusGameState.IN_PROGRESS);
        
        // Message à tous les joueurs
        for (java.util.UUID playerId : game.getPlayers()) {
            Player p = Bukkit.getPlayer(playerId);
            if (p != null) {
                p.sendMessage("§a§l========================================");
                p.sendMessage("§e§l          PARTIE FOCUS LANCÉE");
                p.sendMessage("§a§l========================================");
                p.sendMessage("§7Joueurs: §e" + game.getPlayerCount());
                p.sendMessage("§7Mode: §eBattle Royale par Rounds");
                p.sendMessage("§a§l========================================");
            }
        }
        
        // Lancer la phase de préparation
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            controller.startPreparation();
        }, 40L); // 2 secondes
    }
}
