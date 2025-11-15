package com.nerysia.plugin.lobby.handlers;

import com.nerysia.plugin.Nerysia;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

public class LobbyJoinHandler implements Listener {

    private Nerysia plugin;

    public LobbyJoinHandler(Nerysia plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        
        // Attendre 1 tick pour que l'inventaire soit bien chargé
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                // Téléporter le joueur au spawn du monde Lobby
                World lobbyWorld = Bukkit.getWorld("Lobby");
                if (lobbyWorld != null) {
                    Location spawnLocation = new Location(lobbyWorld, -139.5, 38, -226.5);
                    // Les joueurs regardent vers l'est (yaw = -90)
                    spawnLocation.setYaw(-90f);
                    spawnLocation.setPitch(0f);
                    player.teleport(spawnLocation);
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
                ItemMeta meta = compass.getItemMeta();
                meta.setDisplayName("§a§lMenu Principal");
                compass.setItemMeta(meta);
                
                // Donner le compas au slot 5 (index 4, car les slots commencent à 0)
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
                
                // Créer le scoreboard pour le joueur
                plugin.getScoreboardTask().createScoreboardForPlayer(player);
                
                // Appliquer les paramètres de visibilité pour le nouveau joueur
                plugin.getPlayerVisibilityManager().applyVisibilityForNewPlayer(player.getUniqueId());
                
                // Update l'inventaire
                player.updateInventory();
            }
        }, 1L);
    }
}
