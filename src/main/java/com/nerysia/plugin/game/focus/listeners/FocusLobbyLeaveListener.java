package com.nerysia.plugin.game.focus.listeners;

import com.nerysia.plugin.Nerysia;
import com.nerysia.plugin.game.focus.FocusGame;
import com.nerysia.plugin.game.focus.FocusGameManager;
import com.nerysia.plugin.game.focus.gui.FocusSettingsGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gère l'item de retour au lobby avec timer et l'item de visualisation des configs
 */
public class FocusLobbyLeaveListener implements Listener {
    
    private final Nerysia plugin;
    private final FocusGameManager gameManager;
    private final FocusSettingsGUI settingsGUI;
    private final Map<UUID, TeleportTask> activeTeleports;
    
    public FocusLobbyLeaveListener(Nerysia plugin, FocusGameManager gameManager, FocusSettingsGUI settingsGUI) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.settingsGUI = settingsGUI;
        this.activeTeleports = new HashMap<>();
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
        
        String displayName = item.getItemMeta().getDisplayName();
        
        // Item Retour au Lobby
        if (displayName.equals("§c§lRetour au Lobby")) {
            event.setCancelled(true);
            startTeleportCountdown(player);
            return;
        }
        
        // Item Voir les Paramètres (pour les non-host)
        if (displayName.equals("§e§lVoir les Paramètres")) {
            event.setCancelled(true);
            
            FocusGame game = gameManager.getPlayerGame(player.getUniqueId());
            if (game != null) {
                // Ouvrir le GUI en mode lecture seule
                settingsGUI.open(player);
                player.sendMessage("§7§o(Vous ne pouvez pas modifier les paramètres)");
            }
            return;
        }
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // Vérifier si le joueur a un téléport actif
        if (!activeTeleports.containsKey(playerId)) {
            return;
        }
        
        // Vérifier si le joueur a bougé (pas juste rotation de la tête)
        Location from = event.getFrom();
        Location to = event.getTo();
        
        if (from.getBlockX() != to.getBlockX() || 
            from.getBlockY() != to.getBlockY() || 
            from.getBlockZ() != to.getBlockZ()) {
            
            // Le joueur a bougé, annuler le téléport
            TeleportTask task = activeTeleports.remove(playerId);
            if (task != null) {
                task.cancel();
                player.sendMessage(ChatColor.RED + "✘ Téléportation annulée - Vous avez bougé !");
                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0f, 1.0f);
            }
        }
    }
    
    private void startTeleportCountdown(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Vérifier si le joueur est dans une partie Focus
        FocusGame game = gameManager.getPlayerGame(playerId);
        if (game == null) {
            player.sendMessage(ChatColor.RED + "Vous n'êtes pas dans une partie Focus !");
            return;
        }
        
        // Annuler le téléport précédent s'il existe
        if (activeTeleports.containsKey(playerId)) {
            activeTeleports.get(playerId).cancel();
            activeTeleports.remove(playerId);
        }
        
        // Créer et démarrer la tâche de téléportation
        TeleportTask task = new TeleportTask(player, 3);
        activeTeleports.put(playerId, task);
        task.runTaskTimer(plugin, 0L, 20L); // Toutes les secondes
        
        player.sendMessage(ChatColor.YELLOW + "⏱ Téléportation au lobby dans 3 secondes...");
        player.sendMessage(ChatColor.GRAY + "Ne bougez pas !");
        player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);
    }
    
    /**
     * Tâche de téléportation avec compte à rebours
     */
    private class TeleportTask extends BukkitRunnable {
        private final Player player;
        private int secondsLeft;
        
        public TeleportTask(Player player, int seconds) {
            this.player = player;
            this.secondsLeft = seconds;
        }
        
        @Override
        public void run() {
            if (!player.isOnline()) {
                cancel();
                activeTeleports.remove(player.getUniqueId());
                return;
            }
            
            if (secondsLeft <= 0) {
                // Téléporter au lobby
                player.performCommand("lobby");
                player.sendMessage(ChatColor.GREEN + "✓ Téléportation réussie !");
                player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1.0f, 1.0f);
                
                activeTeleports.remove(player.getUniqueId());
                cancel();
                return;
            }
            
            // Afficher le compte à rebours au-dessus de la hotbar
            String actionBar = ChatColor.GOLD + "⏱ Téléportation dans " + ChatColor.YELLOW + secondsLeft + ChatColor.GOLD + "s...";
            
            // Envoyer un message au-dessus de la hotbar (action bar)
            try {
                Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer");
                Object craftPlayer = craftPlayerClass.cast(player);
                Object packet = Class.forName("net.minecraft.server.v1_8_R3.PacketPlayOutChat")
                    .getConstructor(Class.forName("net.minecraft.server.v1_8_R3.IChatBaseComponent"), byte.class)
                    .newInstance(
                        Class.forName("net.minecraft.server.v1_8_R3.IChatBaseComponent$ChatSerializer")
                            .getMethod("a", String.class)
                            .invoke(null, "{\"text\":\"" + actionBar + "\"}"),
                        (byte) 2
                    );
                Object handle = craftPlayerClass.getMethod("getHandle").invoke(craftPlayer);
                Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
                playerConnection.getClass().getMethod("sendPacket", Class.forName("net.minecraft.server.v1_8_R3.Packet"))
                    .invoke(playerConnection, packet);
            } catch (Exception e) {
                // Fallback: message normal
                player.sendMessage(actionBar);
            }
            
            // Son
            float pitch = 1.0f + (0.2f * (3 - secondsLeft));
            player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, pitch);
            
            secondsLeft--;
        }
    }
}
