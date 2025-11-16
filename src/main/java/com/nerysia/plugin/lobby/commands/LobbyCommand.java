package com.nerysia.plugin.lobby.commands;

import com.nerysia.plugin.Nerysia;
import com.nerysia.plugin.game.focus.FocusGame;
import com.nerysia.plugin.game.focus.FocusGameController;
import com.nerysia.plugin.game.focus.FocusGameManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

public class LobbyCommand implements CommandExecutor {

    private final Nerysia plugin;

    public LobbyCommand(Nerysia plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cCette commande ne peut être exécutée que par un joueur.");
            return true;
        }

        Player player = (Player) sender;
        
        // Vérifier si le joueur est dans une partie Focus en cours
        FocusGameManager gameManager = plugin.getFocusGameManager();
        if (gameManager != null) {
            FocusGame game = gameManager.getPlayerGame(player.getUniqueId());
            if (game != null) {
                FocusGameController controller = gameManager.getGameController(game);
                
                // Si en jeu, retirer le joueur et vérifier si le round doit se terminer
                if (controller != null && controller.getState() == FocusGameController.State.INGAME) {
                    Bukkit.getLogger().info("[Focus] " + player.getName() + " a quitté la partie via /lobby pendant INGAME");
                    
                    // Message aux autres joueurs
                    String message = "§c[Focus] §e" + player.getName() + " §7a quitté la partie !";
                    for (java.util.UUID playerId : game.getPlayers()) {
                        Player p = Bukkit.getPlayer(playerId);
                        if (p != null && !p.equals(player)) {
                            p.sendMessage(message);
                        }
                    }
                    
                    // Retirer le joueur de la partie
                    gameManager.removePlayerFromGame(player.getUniqueId());
                    
                    // Vérifier si le round doit se terminer (après un petit délai)
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        controller.checkRoundEnd();
                    }, 5L);
                } else {
                    // Si pas en jeu, juste retirer le joueur normalement
                    gameManager.removePlayerFromGame(player.getUniqueId());
                }
            }
        }

        // Clear l'inventaire du joueur
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        
        // Clear tous les effets de potion
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        
        // Mettre la vie à 10 coeurs (20 points de vie)
        player.setHealth(20.0);
        player.setMaxHealth(20.0);
        player.setFoodLevel(20);
        
        // Créer le compas
        ItemStack compass = new ItemStack(Material.COMPASS, 1);
        ItemMeta compassMeta = compass.getItemMeta();
        compassMeta.setDisplayName("§a§lMenu Principal");
        compass.setItemMeta(compassMeta);
        
        // Donner le compas au slot 5 (index 4)
        player.getInventory().setItem(4, compass);
        
        // Créer le comparateur (paramètres)
        ItemStack comparator = new ItemStack(Material.REDSTONE_COMPARATOR, 1);
        ItemMeta comparatorMeta = comparator.getItemMeta();
        comparatorMeta.setDisplayName("§6§lParamètres");
        comparator.setItemMeta(comparatorMeta);
        
        // Donner le comparateur au slot 9 (index 8)
        player.getInventory().setItem(8, comparator);
        
        // Forcer le joueur à sélectionner le slot 5
        player.getInventory().setHeldItemSlot(4);

        // Téléporter le joueur au spawn du monde Lobby
        World lobbyWorld = Bukkit.getWorld("Lobby");
        if (lobbyWorld != null) {
            Location spawnLocation = new Location(lobbyWorld, -139.5, 38, -226.5);
            // Les joueurs regardent vers l'est (yaw = -90)
            spawnLocation.setYaw(-90f);
            spawnLocation.setPitch(0f);
            player.teleport(spawnLocation);
            player.sendMessage("§aTéléportation au lobby...");
            
            // Respawner les NPCs pour le joueur après un court délai
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                plugin.getNPCManager().spawnAllNPCsForPlayer(player);
            }, 10L);
            
        } else {
            player.sendMessage("§cLe monde Lobby n'existe pas.");
        }

        return true;
    }
}
