package com.nerysia.plugin.game.focus.gui;

import com.nerysia.plugin.Nerysia;
import com.nerysia.plugin.game.focus.FocusGame;
import com.nerysia.plugin.game.focus.FocusGameManager;
import com.nerysia.plugin.game.focus.FocusGameState;
import com.nerysia.plugin.game.focus.FocusGameController;
import com.nerysia.plugin.game.focus.FocusShopGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class FocusGameListGUI implements Listener {
    
    private final Nerysia plugin;
    private final FocusGameManager gameManager;
    private Inventory inventory;
    
    public FocusGameListGUI(Nerysia plugin, FocusGameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }
    
    public void open(Player player) {
        inventory = Bukkit.createInventory(null, 54, "§e§lFocus - Parties");
        
        // Bouton créer une partie
        ItemStack createGame = createItem(
            Material.EMERALD,
            "§a§lCréer une Partie",
            "§7Créez votre propre partie Focus",
            "",
            "§e➤ Cliquez pour créer"
        );
        inventory.setItem(49, createGame);
        
        // Afficher les parties existantes
        List<FocusGame> games = gameManager.getVisibleGames();
        int slot = 10;
        
        for (FocusGame game : games) {
            if (slot >= 44) break; // Limite de l'affichage
            
            ItemStack gameItem = createGameItem(game);
            inventory.setItem(slot, gameItem);
            
            slot++;
            if (slot % 9 == 8) slot += 2; // Sauter les bordures
        }
        
        // Bouton fermer
        ItemStack close = createItem(
            Material.BARRIER,
            "§c§lFermer",
            "§7Fermer ce menu"
        );
        inventory.setItem(45, close);
        
        player.openInventory(inventory);
    }
    
    private ItemStack createGameItem(FocusGame game) {
        Material material;
        String stateName;
        
        switch (game.getState()) {
            case PREPARATION:
                material = Material.STAINED_GLASS_PANE;
                stateName = "§eEn préparation";
                break;
            case AVAILABLE:
                material = Material.STAINED_GLASS_PANE;
                stateName = "§aDisponible";
                break;
            case PRIVATE:
                material = Material.STAINED_GLASS_PANE;
                stateName = "§6Privée / Complète";
                break;
            case IN_PROGRESS:
                material = Material.STAINED_GLASS_PANE;
                stateName = "§cEn cours";
                break;
            case FINISHED:
                material = Material.STAINED_GLASS_PANE;
                stateName = "§8Terminée";
                break;
            default:
                material = Material.STAINED_GLASS_PANE;
                stateName = "§7Inconnu";
        }
        
        ItemStack item = new ItemStack(material, 1, getGlassPaneData(game.getState()));
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName("§6" + game.getDisplayName());
        
        List<String> lore = new ArrayList<>();
        lore.add("§7Hôte: §e" + game.getHostName());
        lore.add("§7Joueurs: §e" + game.getPlayerCount() + "§7/§e" + game.getSettings().getMaxPlayers());
        lore.add("§7État: " + stateName);
        lore.add("");
        
        if (game.getState() == FocusGameState.AVAILABLE) {
            lore.add("§a➤ Cliquez pour rejoindre");
        } else if (game.getState() == FocusGameState.PREPARATION) {
            lore.add("§e➤ En attente de joueurs");
        } else if (game.getState() == FocusGameState.PRIVATE) {
            if (!game.isPublic()) {
                lore.add("§c✗ Partie privée");
            } else {
                lore.add("§c✗ Partie complète");
            }
        } else if (game.getState() == FocusGameState.IN_PROGRESS) {
            lore.add("§c✗ Partie en cours");
        } else if (game.getState() == FocusGameState.FINISHED) {
            lore.add("§8✗ Partie terminée");
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
    
    private short getGlassPaneData(FocusGameState state) {
        switch (state) {
            case PREPARATION:
                return 4; // Jaune
            case AVAILABLE:
                return 5; // Vert
            case PRIVATE:
                return 1; // Orange
            case IN_PROGRESS:
                return 14; // Rouge
            case FINISHED:
                return 7; // Gris foncé
            default:
                return 0; // Blanc
        }
    }
    
    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        
        List<String> loreList = new ArrayList<>();
        for (String line : lore) {
            loreList.add(line);
        }
        meta.setLore(loreList);
        
        item.setItemMeta(meta);
        return item;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().equals(inventory)) {
            event.setCancelled(true);
            
            Player player = (Player) event.getWhoClicked();
            ItemStack clicked = event.getCurrentItem();
            
            if (clicked == null || clicked.getType() == Material.AIR) {
                return;
            }
            
            if (!clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) {
                return;
            }
            
            String displayName = clicked.getItemMeta().getDisplayName();
            
            // Bouton créer une partie
            if (displayName.equals("§a§lCréer une Partie")) {
                player.closeInventory();
                createNewGame(player);
                return;
            }
            
            // Bouton fermer
            if (displayName.equals("§c§lFermer")) {
                player.closeInventory();
                return;
            }
            
            // Clic sur une partie
            if (clicked.getType() == Material.STAINED_GLASS_PANE) {
                // Récupérer la partie correspondante
                List<FocusGame> games = gameManager.getVisibleGames();
                int slot = event.getSlot();
                
                // Calculer l'index de la partie
                int index = -1;
                int currentSlot = 10;
                for (int i = 0; i < games.size(); i++) {
                    if (currentSlot == slot) {
                        index = i;
                        break;
                    }
                    currentSlot++;
                    if (currentSlot % 9 == 8) currentSlot += 2;
                }
                
                if (index >= 0 && index < games.size()) {
                    FocusGame game = games.get(index);
                    
                    // Permettre à l'hôte de rejoindre sa partie en PREPARATION
                    if (game.getState() == FocusGameState.PREPARATION && game.isHost(player.getUniqueId())) {
                        player.sendMessage("§a[Focus] §7Reconnexion à votre partie...");
                        reconnectToPreparation(player, game);
                    } else if (game.getState() == FocusGameState.AVAILABLE) {
                        // Rejoindre la partie
                        joinGame(player, game);
                    } else if (game.getState() == FocusGameState.IN_PROGRESS && game.getPlayers().contains(player.getUniqueId())) {
                        // Permettre la reconnexion pendant une partie en cours
                        player.sendMessage("§a[Focus] §7Reconnexion à la partie en cours...");
                        reconnectToGame(player, game);
                    } else {
                        player.sendMessage("§c[Focus] §7Cette partie n'est pas disponible !");
                    }
                }
            }
        }
    }
    
    private void createNewGame(Player player) {
        // Vérifier si le joueur n'est pas déjà dans une partie
        if (gameManager.isPlayerInGame(player.getUniqueId())) {
            player.sendMessage("§c[Focus] §7Vous êtes déjà dans une partie !");
            return;
        }
        
        // Créer la partie
        FocusGame game = gameManager.createGame(player);
        
        player.sendMessage("§a[Focus] §7Partie créée avec succès !");
        player.sendMessage("§e[Focus] §7Préparation des maps...");
        
        // Initialiser la partie (dupliquer les maps) de manière asynchrone
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            FocusGameController controller = gameManager.getGameController(game);
            if (controller == null) {
                player.sendMessage("§c[Focus] §7Erreur lors de l'initialisation !");
                return;
            }
            
            // Dupliquer les maps (sur le thread principal)
            Bukkit.getScheduler().runTask(plugin, () -> {
                boolean success = controller.initializeGame();
                
                if (!success) {
                    player.sendMessage("§c[Focus] §7Erreur lors de la création des maps !");
                    gameManager.deleteGame(game.getGameId());
                    return;
                }
                
                // Téléporter au spawn_minijeux dupliqué
                org.bukkit.Location spawnLoc = controller.getSpawnLocation();
                if (spawnLoc != null) {
                    player.teleport(spawnLoc);
                    player.sendMessage("§a[Focus] §7Vous avez été téléporté au lobby !");
                    
                    // Donner les items de l'hôte
                    giveHostItems(player);
                } else {
                    player.sendMessage("§c[Focus] §7Erreur: Impossible de trouver le spawn !");
                    gameManager.deleteGame(game.getGameId());
                }
            });
        });
    }
    
    private void joinGame(Player player, FocusGame game) {
        if (gameManager.addPlayerToGame(game.getGameId(), player)) {
            player.closeInventory();
            
            // Message unique à tous les joueurs de la partie (y compris celui qui rejoint)
            String message = "§a[Focus] §e" + player.getName() + " §7a rejoint la partie ! §8(§e" + game.getPlayerCount() + "§8/§e" + game.getSettings().getMaxPlayers() + "§8)";
            for (java.util.UUID playerId : game.getPlayers()) {
                Player p = Bukkit.getPlayer(playerId);
                if (p != null) {
                    p.sendMessage(message);
                }
            }
            
            // Téléporter au spawn_minijeux dupliqué
            Bukkit.getScheduler().runTask(plugin, () -> {
                FocusGameController controller = gameManager.getGameController(game);
                if (controller != null) {
                    org.bukkit.Location spawnLoc = controller.getSpawnLocation();
                    if (spawnLoc != null) {
                        player.teleport(spawnLoc);
                        
                        // Donner les items du joueur (non-host)
                        givePlayerItems(player);
                    } else {
                        player.sendMessage("§c[Focus] §7Erreur: Impossible de trouver le spawn !");
                    }
                } else {
                    player.sendMessage("§c[Focus] §7Erreur: Partie introuvable !");
                }
            });
        } else {
            player.sendMessage("§c[Focus] §7Impossible de rejoindre cette partie !");
        }
    }
    
    private void giveHostItems(Player player) {
        player.getInventory().clear();
        
        // Item 1: Paramètres du jeu (slot 0)
        ItemStack settingsItem = new ItemStack(Material.COMMAND);
        ItemMeta settingsMeta = settingsItem.getItemMeta();
        settingsMeta.setDisplayName("§e§lParamètres du Jeu");
        List<String> settingsLore = new ArrayList<>();
        settingsLore.add("§7Configurez les paramètres");
        settingsLore.add("§7de votre partie Focus");
        settingsLore.add("");
        settingsLore.add("§e➤ Clic droit pour ouvrir");
        settingsMeta.setLore(settingsLore);
        settingsItem.setItemMeta(settingsMeta);
        player.getInventory().setItem(0, settingsItem);
        
        // Item 2: Gestion des joueurs (slot 1)
        ItemStack playersItem = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        ItemMeta playersMeta = playersItem.getItemMeta();
        playersMeta.setDisplayName("§b§lGestion des Joueurs");
        List<String> playersLore = new ArrayList<>();
        playersLore.add("§7Invitez des joueurs");
        playersLore.add("§7Passez en Public/Privé");
        playersLore.add("§7Supprimez la partie");
        playersLore.add("");
        playersLore.add("§e➤ Clic droit pour ouvrir");
        playersMeta.setLore(playersLore);
        playersItem.setItemMeta(playersMeta);
        player.getInventory().setItem(1, playersItem);
        
        // Item 3: Lancer la partie (slot 4)
        ItemStack startItem = new ItemStack(Material.EMERALD);
        ItemMeta startMeta = startItem.getItemMeta();
        startMeta.setDisplayName("§a§lLancer la Partie");
        List<String> startLore = new ArrayList<>();
        startLore.add("§7Démarrez la partie Focus");
        startLore.add("§7avec les joueurs présents");
        startLore.add("");
        startLore.add("§c⚠ Minimum 2 joueurs requis");
        startLore.add("");
        startLore.add("§e➤ Clic droit pour lancer");
        startMeta.setLore(startLore);
        startItem.setItemMeta(startMeta);
        player.getInventory().setItem(4, startItem);
        
        // Item 4: Retour au Lobby (slot 8)
        ItemStack leaveItem = new ItemStack(Material.BED);
        ItemMeta leaveMeta = leaveItem.getItemMeta();
        leaveMeta.setDisplayName("§c§lRetour au Lobby");
        List<String> leaveLore = new ArrayList<>();
        leaveLore.add("§7Quittez la partie et");
        leaveLore.add("§7retournez au lobby");
        leaveLore.add("");
        leaveLore.add("§e➤ Clic droit pour partir");
        leaveLore.add("§c⚠ Timer de 3 secondes");
        leaveMeta.setLore(leaveLore);
        leaveItem.setItemMeta(leaveMeta);
        player.getInventory().setItem(8, leaveItem);
        
        player.sendMessage("§a[Focus] §7Vous êtes l'hôte de cette partie !");
        player.sendMessage("§e[Focus] §7Utilisez les items pour gérer votre partie.");
    }
    
    private void givePlayerItems(Player player) {
        player.getInventory().clear();
        
        // Item 1: Voir les Paramètres (slot 4)
        ItemStack settingsItem = new ItemStack(Material.BOOK);
        ItemMeta settingsMeta = settingsItem.getItemMeta();
        settingsMeta.setDisplayName("§e§lVoir les Paramètres");
        List<String> settingsLore = new ArrayList<>();
        settingsLore.add("§7Consultez les paramètres");
        settingsLore.add("§7de la partie");
        settingsLore.add("");
        settingsLore.add("§7§o(Lecture seule)");
        settingsLore.add("");
        settingsLore.add("§e➤ Clic droit pour voir");
        settingsMeta.setLore(settingsLore);
        settingsItem.setItemMeta(settingsMeta);
        player.getInventory().setItem(4, settingsItem);
        
        // Item 2: Retour au Lobby (slot 8)
        ItemStack leaveItem = new ItemStack(Material.BED);
        ItemMeta leaveMeta = leaveItem.getItemMeta();
        leaveMeta.setDisplayName("§c§lRetour au Lobby");
        List<String> leaveLore = new ArrayList<>();
        leaveLore.add("§7Quittez la partie et");
        leaveLore.add("§7retournez au lobby");
        leaveLore.add("");
        leaveLore.add("§e➤ Clic droit pour partir");
        leaveLore.add("§c⚠ Timer de 3 secondes");
        leaveMeta.setLore(leaveLore);
        leaveItem.setItemMeta(leaveMeta);
        player.getInventory().setItem(8, leaveItem);
        
        player.sendMessage("§a[Focus] §7Vous avez rejoint la partie !");
        player.sendMessage("§e[Focus] §7En attente du lancement par l'hôte...");
    }
    
    private void reconnectToPreparation(Player player, FocusGame game) {
        player.closeInventory();
        
        // Téléporter au spawn_minijeux
        Bukkit.getScheduler().runTask(plugin, () -> {
            org.bukkit.World world = Bukkit.getWorld("spawn_minijeux");
            if (world != null) {
                org.bukkit.Location spawn = new org.bukkit.Location(world, -10.5, 104, 0.5, 0, 0);
                player.teleport(spawn);
                
                // Vérifier si le joueur est l'hôte pour donner les bons items
                if (game.isHost(player.getUniqueId())) {
                    giveHostItems(player);
                    player.sendMessage("§a[Focus] §7Reconnexion réussie ! Vous êtes l'hôte.");
                } else {
                    givePlayerItems(player);
                    player.sendMessage("§a[Focus] §7Reconnexion réussie !");
                }
            } else {
                player.sendMessage("§c[Focus] §7Erreur: Le monde spawn_minijeux n'existe pas !");
            }
        });
    }
    
    private void reconnectToGame(Player player, FocusGame game) {
        player.closeInventory();
        
        // Récupérer le controller
        FocusGameController controller = gameManager.getGameController(game);
        if (controller == null) {
            player.sendMessage("§c[Focus] §7Erreur: Contrôleur de jeu introuvable !");
            return;
        }
        
        // Téléporter au monde de la partie
        Bukkit.getScheduler().runTask(plugin, () -> {
            org.bukkit.World gameWorld = Bukkit.getWorld(game.getGameId());
            if (gameWorld != null) {
                // Vérifier si le joueur est mort ou vivant
                if (controller.isAlive(player)) {
                    // Joueur vivant : téléporter au spawn du lobby du monde (attente du prochain round)
                    player.teleport(gameWorld.getSpawnLocation());
                    player.setGameMode(org.bukkit.GameMode.SURVIVAL);
                    player.sendMessage("§a[Focus] §7Reconnexion réussie ! Vous êtes en vie.");
                } else {
                    // Joueur mort : mettre en spectateur
                    player.teleport(gameWorld.getSpawnLocation());
                    player.setGameMode(org.bukkit.GameMode.SPECTATOR);
                    player.sendMessage("§a[Focus] §7Reconnexion réussie ! Vous êtes en mode spectateur.");
                }
                
                // Donner les items appropriés selon l'état du contrôleur
                if (controller.getState() == FocusGameController.State.LOBBY) {
                    // En phase de lobby entre les rounds, ouvrir le shop
                    FocusShopGUI shopGUI = new FocusShopGUI(
                        plugin.getFocusShopData(), 
                        plugin.getFocusPointsManager()
                    );
                    shopGUI.open(player, null);
                }
            } else {
                player.sendMessage("§c[Focus] §7Erreur: Le monde de la partie n'existe pas !");
            }
        });
    }
}
