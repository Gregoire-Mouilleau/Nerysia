package com.nerysia.plugin.game.focus.gui;

import com.nerysia.plugin.Nerysia;
import com.nerysia.plugin.game.focus.FocusGame;
import com.nerysia.plugin.game.focus.FocusGameManager;
import com.nerysia.plugin.game.focus.FocusGameState;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FocusPlayerManagementGUI implements Listener {
    
    private final Nerysia plugin;
    private final FocusGameManager gameManager;
    private Inventory inventory;
    private Player viewer;
    private boolean confirmDelete = false;
    
    public FocusPlayerManagementGUI(Nerysia plugin, FocusGameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }
    
    public void open(Player player) {
        FocusGame game = gameManager.getPlayerGame(player.getUniqueId());
        
        if (game == null || !game.isHost(player.getUniqueId())) {
            player.sendMessage("§c[Focus] §7Vous n'êtes pas l'hôte d'une partie !");
            return;
        }
        
        this.viewer = player;
        this.confirmDelete = false;
        this.inventory = Bukkit.createInventory(null, 54, "§b§lFocus - Gestion Joueurs");
        
        updateItems(game);
        player.openInventory(inventory);
    }
    
    private void updateItems(FocusGame game) {
        inventory.clear();
        
        // Toggle Public/Privé
        Material visibilityMaterial;
        String visibilityTitle;
        
        if (game.getState() == FocusGameState.PREPARATION) {
            visibilityMaterial = Material.GOLD_BLOCK;
            visibilityTitle = "§e§lChoisir le Mode";
        } else {
            visibilityMaterial = game.isPublic() ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK;
            visibilityTitle = game.isPublic() ? "§a§lPartie Publique" : "§c§lPartie Privée";
        }
        
        ItemStack visibility = new ItemStack(visibilityMaterial);
        ItemMeta visibilityMeta = visibility.getItemMeta();
        visibilityMeta.setDisplayName(visibilityTitle);
        List<String> visibilityLore = new ArrayList<>();
        
        if (game.getState() == FocusGameState.PREPARATION) {
            visibilityLore.add("§7État: §eEn préparation");
            visibilityLore.add("");
            visibilityLore.add("§7Choisissez le mode de votre partie:");
            visibilityLore.add("");
            visibilityLore.add("§a§lPublic");
            visibilityLore.add("§7• Visible dans la liste des parties");
            visibilityLore.add("§7• N'importe qui peut rejoindre");
            visibilityLore.add("");
            visibilityLore.add("§c§lPrivé");
            visibilityLore.add("§7• Invisible dans la liste");
            visibilityLore.add("§7• Sur invitation uniquement");
            visibilityLore.add("");
            visibilityLore.add("§e➤ Clic gauche: Passer en §aPublic");
            visibilityLore.add("§e➤ Clic droit: Passer en §cPrivé");
        } else {
            visibilityLore.add("§7Mode: " + (game.isPublic() ? "§aPublic" : "§cPrivé"));
            visibilityLore.add("§7État: §e" + game.getState().name());
            visibilityLore.add("");
            if (game.isPublic()) {
                visibilityLore.add("§7N'importe qui peut rejoindre");
                visibilityLore.add("§7votre partie");
            } else {
                visibilityLore.add("§7Seuls les joueurs invités");
                visibilityLore.add("§7peuvent rejoindre");
            }
            visibilityLore.add("");
            visibilityLore.add("§e➤ Cliquez pour passer en " + (game.isPublic() ? "§cPrivé" : "§aPublic"));
        }
        visibilityMeta.setLore(visibilityLore);
        visibility.setItemMeta(visibilityMeta);
        inventory.setItem(11, visibility);
        
        // Inviter des joueurs
        ItemStack invite = new ItemStack(Material.PAPER);
        ItemMeta inviteMeta = invite.getItemMeta();
        inviteMeta.setDisplayName("§d§lInviter des Joueurs");
        List<String> inviteLore = new ArrayList<>();
        inviteLore.add("§7Invitez des joueurs en ligne");
        inviteLore.add("§7à rejoindre votre partie");
        inviteLore.add("");
        inviteLore.add("§e➤ Cliquez pour voir la liste");
        inviteMeta.setLore(inviteLore);
        invite.setItemMeta(inviteMeta);
        inventory.setItem(13, invite);
        
        // Supprimer la partie
        ItemStack delete = new ItemStack(confirmDelete ? Material.TNT : Material.BARRIER);
        ItemMeta deleteMeta = delete.getItemMeta();
        deleteMeta.setDisplayName(confirmDelete ? "§c§l⚠ CONFIRMER LA SUPPRESSION ⚠" : "§4§lSupprimer la Partie");
        List<String> deleteLore = new ArrayList<>();
        if (confirmDelete) {
            deleteLore.add("§c§lATTENTION!");
            deleteLore.add("§7Cliquez à nouveau pour");
            deleteLore.add("§7supprimer définitivement");
            deleteLore.add("§7votre partie!");
            deleteLore.add("");
            deleteLore.add("§c➤ CLIQUEZ POUR CONFIRMER");
        } else {
            deleteLore.add("§7Supprimez votre partie");
            deleteLore.add("§7et renvoyez tous les joueurs");
            deleteLore.add("§7au lobby");
            deleteLore.add("");
            deleteLore.add("§c➤ Cliquez pour supprimer");
        }
        deleteMeta.setLore(deleteLore);
        delete.setItemMeta(deleteMeta);
        inventory.setItem(15, delete);
        
        // Liste des joueurs
        List<UUID> players = game.getPlayers();
        int slot = 27;
        for (UUID playerId : players) {
            Player p = Bukkit.getPlayer(playerId);
            if (p != null) {
                ItemStack playerHead = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
                SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
                skullMeta.setOwner(p.getName());
                skullMeta.setDisplayName("§e" + p.getName());
                
                List<String> playerLore = new ArrayList<>();
                if (game.isHost(playerId)) {
                    playerLore.add("§6§l★ HÔTE §6§l★");
                } else {
                    playerLore.add("§7Joueur");
                }
                playerLore.add("");
                
                if (!game.isHost(playerId)) {
                    playerLore.add("§c➤ Clic pour expulser");
                }
                
                skullMeta.setLore(playerLore);
                playerHead.setItemMeta(skullMeta);
                inventory.setItem(slot, playerHead);
                slot++;
                
                if (slot >= 53) break;
            }
        }
        
        // Bouton retour
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
        
        if (game == null || !game.isHost(player.getUniqueId())) {
            player.closeInventory();
            return;
        }
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }
        
        switch (event.getSlot()) {
            case 11: // Toggle Public/Privé
                if (game.getState() == FocusGameState.PREPARATION) {
                    // En préparation : clic gauche = public, clic droit = privé
                    boolean clickRight = event.isRightClick();
                    game.setPublic(!clickRight);
                    
                    if (clickRight) {
                        player.sendMessage("§e[Focus] §7Partie passée en mode §cPrivé");
                        player.sendMessage("§e[Focus] §7Seuls les joueurs invités pourront rejoindre");
                    } else {
                        player.sendMessage("§e[Focus] §7Partie passée en mode §aPublic");
                        player.sendMessage("§e[Focus] §7Votre partie est maintenant visible par tous !");
                    }
                } else {
                    // Toggle normal
                    boolean wasPublic = game.isPublic();
                    game.setPublic(!wasPublic);
                    String newMode = game.isPublic() ? "§aPublic" : "§cPrivé";
                    player.sendMessage("§e[Focus] §7Partie passée en mode " + newMode);
                }
                player.sendMessage("§e[Focus] §7État: §e" + game.getState().name());
                updateItems(game);
                break;
                
            case 13: // Inviter des joueurs
                player.sendMessage("§e[Focus] §7Fonctionnalité d'invitation à venir...");
                // TODO: Ouvrir un GUI avec la liste des joueurs en ligne
                break;
                
            case 15: // Supprimer la partie
                if (confirmDelete) {
                    // Confirmation - supprimer la partie
                    player.closeInventory();
                    deleteGame(player, game);
                } else {
                    // Première demande - demander confirmation
                    confirmDelete = true;
                    updateItems(game);
                    player.sendMessage("§c[Focus] §7Cliquez à nouveau pour confirmer la suppression !");
                }
                break;
                
            case 49: // Retour
                player.closeInventory();
                confirmDelete = false;
                break;
                
            default:
                // Clic sur la tête d'un joueur pour l'expulser
                if (event.getSlot() >= 27 && event.getSlot() < 54) {
                    if (clicked.getType() == Material.SKULL_ITEM) {
                        SkullMeta skullMeta = (SkullMeta) clicked.getItemMeta();
                        String playerName = skullMeta.getOwner();
                        Player target = Bukkit.getPlayer(playerName);
                        
                        if (target != null && !game.isHost(target.getUniqueId())) {
                            kickPlayer(player, game, target);
                        }
                    }
                }
                break;
        }
    }
    
    private void deleteGame(Player host, FocusGame game) {
        // Téléporter tous les joueurs au lobby
        for (UUID playerId : game.getPlayers()) {
            Player p = Bukkit.getPlayer(playerId);
            if (p != null) {
                teleportToLobby(p);
                if (!playerId.equals(host.getUniqueId())) {
                    p.sendMessage("§c[Focus] §7La partie a été supprimée par l'hôte !");
                }
            }
        }
        
        // Supprimer la partie
        gameManager.deleteGame(game.getGameId());
        host.sendMessage("§a[Focus] §7Partie supprimée avec succès !");
    }
    
    private void kickPlayer(Player host, FocusGame game, Player target) {
        game.removePlayer(target.getUniqueId());
        teleportToLobby(target);
        
        target.sendMessage("§c[Focus] §7Vous avez été expulsé de la partie !");
        host.sendMessage("§e[Focus] §7" + target.getName() + " a été expulsé.");
        
        updateItems(game);
    }
    
    private void teleportToLobby(Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            org.bukkit.World lobby = Bukkit.getWorld("Lobby");
            if (lobby != null) {
                player.teleport(lobby.getSpawnLocation());
                player.getInventory().clear();
                
                // Exécuter la commande /lobby pour réinitialiser et revoir les NPCs
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    player.performCommand("lobby");
                }, 5L); // Petit délai pour s'assurer que la téléportation est effectuée
            }
        });
    }
}
