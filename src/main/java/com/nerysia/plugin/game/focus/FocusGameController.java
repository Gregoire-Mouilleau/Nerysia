package com.nerysia.plugin.game.focus;

import com.nerysia.plugin.Nerysia;
import com.nerysia.plugin.game.focus.listeners.FocusGameplayListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Contrôle le déroulement d'une partie Focus (lobby → rounds → fin)
 */
public class FocusGameController {
    
    public enum State {
        WAITING,      // En attente dans spawn_minijeux
        LOBBY,        // Dans le lobby de la map Focus
        INGAME,       // Round en cours
        ROUND_END,    // Fin de round (affichage résultats)
        GAME_END      // Fin de partie (affichage winner)
    }
    
    private final FocusGame game;
    private final FocusPointsManager pointsManager;
    private final FocusShopData shopData;
    private final FocusShopGUI shopGUI;
    private final FocusMapManager mapManager;
    
    private State state;
    private final Set<UUID> readyPlayers;
    private List<Location> currentSpawns;
    private final Map<UUID, Boolean> alivePlayers; // true = alive, false = dead
    private World gameWorld; // Le monde dupliqué pour cette partie
    private World spawnWorld; // Le spawn_minijeux dupliqué pour cette partie
    
    // Settings
    private int killsToWin = 10;
    private int roundsToWin = 5;
    
    public FocusGameController(FocusGame game, FocusPointsManager pointsManager, FocusShopData shopData, FocusShopGUI shopGUI) {
        this.game = game;
        this.pointsManager = pointsManager;
        this.shopData = shopData;
        this.shopGUI = shopGUI;
        this.mapManager = new FocusMapManager();
        this.state = State.WAITING;
        this.readyPlayers = new HashSet<>();
        this.alivePlayers = new HashMap<>();
        this.gameWorld = null;
        this.spawnWorld = null;
    }
    
    public State getState() {
        return state;
    }
    
    public boolean isAlive(Player player) {
        return alivePlayers.getOrDefault(player.getUniqueId(), false);
    }
    
    public void setAlive(Player player, boolean alive) {
        alivePlayers.put(player.getUniqueId(), alive);
    }
    
    // ========== MAP MANAGEMENT ==========
    
    /**
     * Initialise la partie en dupliquant la map Focus
     * @return true si succès, false si erreur
     */
    public boolean initializeGame() {
        broadcast(ChatColor.GOLD + "Préparation des maps Focus...");
        
        // Dupliquer le spawn_minijeux
        spawnWorld = mapManager.duplicateSpawnMinijeux(game.getGameId());
        if (spawnWorld == null) {
            broadcast(ChatColor.RED + "Erreur lors de la création du spawn !");
            return false;
        }
        
        // Dupliquer la map de jeu
        gameWorld = mapManager.duplicateMap(game.getGameId());
        if (gameWorld == null) {
            broadcast(ChatColor.RED + "Erreur lors de la création de la map !");
            // Nettoyer le spawn créé
            if (spawnWorld != null) {
                mapManager.deleteWorld(spawnWorld.getName());
                spawnWorld = null;
            }
            return false;
        }
        
        broadcast(ChatColor.GREEN + "Maps Focus chargées avec succès !");
        return true;
    }
    
    /**
     * Nettoie et supprime la map dupliquée
     */
    public void cleanupGame() {
        if (spawnWorld != null) {
            String worldName = spawnWorld.getName();
            mapManager.deleteWorld(worldName);
            spawnWorld = null;
        }
        if (gameWorld != null) {
            String worldName = gameWorld.getName();
            mapManager.deleteWorld(worldName);
            gameWorld = null;
        }
    }
    
    // ========== READY SYSTEM ==========
    
    public void toggleReady(Player player) {
        if (state != State.WAITING && state != State.LOBBY) {
            player.sendMessage(ChatColor.RED + "La partie est déjà en cours !");
            return;
        }
        
        UUID id = player.getUniqueId();
        if (readyPlayers.contains(id)) {
            readyPlayers.remove(id);
            broadcast(ChatColor.YELLOW + player.getName() + " n'est plus prêt ! (" + ChatColor.GOLD + readyPlayers.size() + ChatColor.YELLOW + "/" + ChatColor.GOLD + game.getPlayers().size() + ChatColor.YELLOW + ")");
        } else {
            readyPlayers.add(id);
            broadcast(ChatColor.GREEN + player.getName() + " est prêt ! (" + ChatColor.GOLD + readyPlayers.size() + ChatColor.GREEN + "/" + ChatColor.GOLD + game.getPlayers().size() + ChatColor.GREEN + ")");
            
            // Si tous les joueurs sont prêts, on lance
            if (readyPlayers.size() >= 2 && readyPlayers.size() == game.getPlayers().size()) {
                startCountdown();
            }
        }
    }
    
    public boolean isPlayerReady(Player player) {
        return readyPlayers.contains(player.getUniqueId());
    }
    
    private void startCountdown() {
        broadcast(ChatColor.GREEN + "╔══════════════════════════════╗");
        broadcast(ChatColor.GOLD + "  Tous les joueurs sont prêts !");
        broadcast(ChatColor.YELLOW + "  Lancement du round dans 3s...");
        broadcast(ChatColor.GREEN + "╚══════════════════════════════╝");
        
        Bukkit.getScheduler().runTaskLater(Nerysia.getInstance(), () -> {
            startRound();
        }, 60L); // 3 secondes
    }
    
    // ========== ROUND MANAGEMENT ==========
    
    public void startRound() {
        state = State.INGAME;
        readyPlayers.clear();
        
        // Réinitialiser la liste des joueurs morts
        FocusGameplayListener.clearDeadPlayers();
        
        // Réinitialiser les mines
        FocusGameManager.clearMines();
        
        // Marquer tous les joueurs comme vivants
        for (UUID playerId : game.getPlayers()) {
            alivePlayers.put(playerId, true);
        }
        
        // Vérifier que le monde de jeu est chargé
        if (gameWorld == null) {
            broadcast(ChatColor.RED + "Erreur: Le monde de jeu n'est pas chargé !");
            return;
        }
        
        // Charger les spawns depuis le monde dupliqué
        if (currentSpawns == null || currentSpawns.isEmpty()) {
            currentSpawns = getMapSpawns();
        }
        
        // Téléporter aux spawns
        List<Player> players = getOnlinePlayers();
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            Location spawn = currentSpawns.get(i % currentSpawns.size());
            
            // Reset complet du joueur
            player.setGameMode(GameMode.SURVIVAL);
            player.setHealth(20.0);
            player.setFoodLevel(20);
            player.setSaturation(20.0f);
            
            // Retirer tous les effets de potion
            for (org.bukkit.potion.PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
            
            // Retirer metadata de mort si présent
            if (player.hasMetadata("focus_death_loc")) {
                player.removeMetadata("focus_death_loc", Nerysia.getInstance());
            }
            
            // Téléporter
            player.teleport(spawn);
            
            // Donner l'équipement (true = en jeu, donner les consommables)
            shopGUI.givePlayerEquipment(player, true);
        }
        
        broadcast(ChatColor.GOLD + "========== ROUND COMMENCE ==========");
        broadcast(ChatColor.YELLOW + "Dernier joueur en vie remporte le round !");
    }
    
    private List<Location> getMapSpawns() {
        // Spawns configurés pour la map Volcan (de l'ancien code)
        List<Location> spawns = new ArrayList<>();
        
        if (gameWorld != null) {
            spawns.add(new Location(gameWorld, 511, 37, 463));
            spawns.add(new Location(gameWorld, 544, 46, 499));
            spawns.add(new Location(gameWorld, 534, 42, 522));
            spawns.add(new Location(gameWorld, 518, 36, 543));
            spawns.add(new Location(gameWorld, 490, 33, 544));
            spawns.add(new Location(gameWorld, 454, 31, 532));
            spawns.add(new Location(gameWorld, 458, 33, 503));
            spawns.add(new Location(gameWorld, 473, 34, 463));
            spawns.add(new Location(gameWorld, 500, 32, 464));
            spawns.add(new Location(gameWorld, 527, 28, 484));
            spawns.add(new Location(gameWorld, 495, 20, 477));
            spawns.add(new Location(gameWorld, 471, 20, 504));
            spawns.add(new Location(gameWorld, 482, 16, 529));
            spawns.add(new Location(gameWorld, 517, 16, 514));
            spawns.add(new Location(gameWorld, 527, 16, 489));
            spawns.add(new Location(gameWorld, 503, 35, 500));
        }
        
        return spawns;
    }
    
    public void onPlayerDeath(Player victim, Player killer) {
        if (state != State.INGAME) {
            Bukkit.getLogger().warning("[Focus] onPlayerDeath appelé mais state != INGAME (state=" + state + ")");
            return;
        }
        
        alivePlayers.put(victim.getUniqueId(), false);
        Bukkit.getLogger().info("[Focus] " + victim.getName() + " marqué comme mort");
        
        // Donner des points au tueur
        if (killer != null && killer != victim) {
            pointsManager.registerKill(killer);
            pointsManager.addPoints(killer, 2); // 2 points par kill
            killer.sendMessage(ChatColor.GREEN + "+2 points pour le kill !");
        }
        
        // Vérifier s'il reste qu'un seul joueur en vie
        List<Player> alive = getAlivePlayers();
        Bukkit.getLogger().info("[Focus] Joueurs en vie après mort: " + alive.size() + " / " + game.getPlayers().size());
        
        // Logger tous les joueurs vivants
        for (Player p : alive) {
            Bukkit.getLogger().info("[Focus]   - Joueur vivant: " + p.getName());
        }
        
        if (alive.size() == 1) {
            Bukkit.getLogger().info("[Focus] Un seul joueur en vie, fin du round!");
            endRound(alive.get(0));
        } else if (alive.isEmpty()) {
            Bukkit.getLogger().info("[Focus] Aucun joueur en vie, fin du round!");
            endRound(null);
        } else {
            Bukkit.getLogger().info("[Focus] Round continue, " + alive.size() + " joueurs encore en vie");
        }
    }
    
    private void endRound(Player winner) {
        state = State.ROUND_END;
        
        Bukkit.getLogger().info("[Focus] Fin du round - Winner: " + (winner != null ? winner.getName() : "null"));
        
        if (winner != null) {
            pointsManager.incrementRoundsWon(winner);
            broadcast(ChatColor.GOLD + "========== FIN DU ROUND ==========");
            broadcast(ChatColor.YELLOW + winner.getName() + ChatColor.GREEN + " remporte le round !");
            
            // Points bonus pour le top 3
            List<Player> ranking = getPlayersByKills();
            if (ranking.size() >= 1) {
                pointsManager.addPoints(ranking.get(0), 8);
                ranking.get(0).sendMessage(ChatColor.GOLD + "+8 points (1er en kills)");
            }
            if (ranking.size() >= 2) {
                pointsManager.addPoints(ranking.get(1), 6);
                ranking.get(1).sendMessage(ChatColor.YELLOW + "+6 points (2ème en kills)");
            }
            if (ranking.size() >= 3) {
                pointsManager.addPoints(ranking.get(2), 5);
                ranking.get(2).sendMessage(ChatColor.GRAY + "+5 points (3ème en kills)");
            }
            
            // Vérifier si quelqu'un a gagné la partie
            int winnerKills = pointsManager.getKills(winner);
            int winnerRounds = pointsManager.getRoundsWon(winner);
            
            Bukkit.getLogger().info("[Focus] Vérification victoire - Kills: " + winnerKills + "/" + killsToWin + " - Rounds: " + winnerRounds + "/" + roundsToWin);
            
            if (winnerKills >= killsToWin || winnerRounds >= roundsToWin) {
                Bukkit.getLogger().info("[Focus] Fin de partie déclenchée!");
                endGame(winner);
                return;
            }
        } else {
            broadcast(ChatColor.RED + "Aucun gagnant pour ce round !");
        }
        
        // Retour au lobby après 3 secondes
        broadcast(ChatColor.YELLOW + "Retour au spawn dans 3 secondes...");
        Bukkit.getLogger().info("[Focus] Planification du retour au lobby dans 3 secondes...");
        Bukkit.getScheduler().runTaskLater(Nerysia.getInstance(), () -> {
            Bukkit.getLogger().info("[Focus] Exécution du retour au lobby maintenant!");
            returnToLobby();
        }, 60L); // 3 secondes
    }
    
    private void returnToLobby() {
        state = State.LOBBY;
        readyPlayers.clear(); // Reset les ready pour le prochain round
        
        // Reset kills du round
        pointsManager.resetAllKills();
        
        // Sauvegarder les items consommables non utilisés de tous les joueurs (vivants et morts)
        for (Player player : getOnlinePlayers()) {
            saveUnusedConsumables(player);
        }
        
        // Téléporter au spawn_minijeux dupliqué
        Location spawnLocation = getSpawnMinijeux();
        
        Bukkit.getLogger().info("[Focus] Retour au lobby - Monde: " + spawnLocation.getWorld().getName() + " - Position: " + spawnLocation);
        
        for (Player player : getOnlinePlayers()) {
            player.teleport(spawnLocation);
            player.setHealth(20.0);
            player.setFoodLevel(20);
            player.setGameMode(GameMode.SURVIVAL);
            player.getInventory().clear();
            player.getInventory().setArmorContents(null); // Clear armure
            
            // Retirer tous les effets (invisibilité du mode spectateur)
            for (org.bukkit.potion.PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
            
            Bukkit.getLogger().info("[Focus] " + player.getName() + " téléporté au lobby dans le monde: " + player.getWorld().getName());
            
            // Donner l'item Shop dans le slot 5
            org.bukkit.inventory.ItemStack shopItem = new org.bukkit.inventory.ItemStack(org.bukkit.Material.CHEST);
            org.bukkit.inventory.meta.ItemMeta shopMeta = shopItem.getItemMeta();
            shopMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Shop Focus");
            List<String> shopLore = new java.util.ArrayList<>();
            shopLore.add(ChatColor.GRAY + "Clic droit pour ouvrir/fermer");
            shopMeta.setLore(shopLore);
            shopItem.setItemMeta(shopMeta);
            player.getInventory().setItem(4, shopItem);
            
            // Ouvrir automatiquement le shop
            shopGUI.open(player, this);
        }
        
        broadcast(ChatColor.GREEN + "╔══════════════════════════════╗");
        broadcast(ChatColor.GOLD + "  Shop ouvert ! Achetez vos améliorations");
        broadcast(ChatColor.YELLOW + "  Tapez /ready quand vous êtes prêt !");
        broadcast(ChatColor.GREEN + "╚══════════════════════════════╝");
    }
    
    private void endGame(Player winner) {
        state = State.GAME_END;
        
        broadcast(ChatColor.GOLD + "========================================");
        broadcast(ChatColor.YELLOW + "🏆 " + winner.getName() + " remporte la partie ! 🏆");
        broadcast(ChatColor.GOLD + "========================================");
        
        // Afficher les stats
        broadcast("");
        broadcast(ChatColor.AQUA + "Statistiques finales:");
        List<Player> ranking = getPlayersByPoints();
        for (int i = 0; i < Math.min(3, ranking.size()); i++) {
            Player p = ranking.get(i);
            int points = pointsManager.getPoints(p);
            int kills = pointsManager.getKills(p);
            int rounds = pointsManager.getRoundsWon(p);
            broadcast(ChatColor.YELLOW + "#" + (i + 1) + " " + p.getName() + ChatColor.GRAY + 
                      " - Points: " + points + " | Kills: " + kills + " | Rounds: " + rounds);
        }
        
        // Reset et retour au spawn après 10 secondes
        Bukkit.getScheduler().runTaskLater(Nerysia.getInstance(), () -> {
            cleanup();
        }, 200L); // 10 secondes
    }
    
    private void cleanup() {
        // Téléporter tous les joueurs au spawn avant de supprimer le monde
        Location spawnMinijeux = getSpawnMinijeux();
        
        for (UUID playerId : game.getPlayers()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                // Reset l'état du joueur
                player.setGameMode(GameMode.SURVIVAL);
                player.setHealth(20.0);
                player.setFoodLevel(20);
                player.setSaturation(20.0f);
                player.getInventory().clear();
                player.getInventory().setArmorContents(null);
                
                // Retirer invisibilité et effets
                player.removePotionEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY);
                for (org.bukkit.potion.PotionEffect effect : player.getActivePotionEffects()) {
                    player.removePotionEffect(effect.getType());
                }
                
                // Retirer les metadata
                if (player.hasMetadata("focus_death_loc")) {
                    player.removeMetadata("focus_death_loc", Nerysia.getInstance());
                }
                
                // Reset les données
                pointsManager.resetPlayer(player);
                shopData.resetPlayer(playerId);
                
                // Téléporter au spawn
                player.teleport(spawnMinijeux);
            }
        }
        
        readyPlayers.clear();
        alivePlayers.clear();
        state = State.WAITING;
        
        // Supprimer le monde dupliqué après un court délai
        Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("Nerysia"), () -> {
            cleanupGame();
        }, 20L); // 1 seconde
    }
    
    // ========== SPAWNS ==========
    
    public void setSpawns(List<Location> spawns) {
        this.currentSpawns = new ArrayList<>(spawns);
        Collections.shuffle(this.currentSpawns);
    }
    
    private Location getLobbyLocation() {
        // TODO: À remplacer par la vraie position du lobby Focus
        return new Location(Bukkit.getWorld("focus_lobby"), 0, 100, 0);
    }
    
    private Location getSpawnMinijeux() {
        // Utiliser le spawn dupliqué si disponible, sinon l'original
        World world = spawnWorld != null ? spawnWorld : Bukkit.getWorld("spawn_minijeux");
        
        if (spawnWorld != null) {
            Bukkit.getLogger().info("[Focus] Utilisation du spawn dupliqué: " + spawnWorld.getName());
        } else {
            Bukkit.getLogger().warning("[Focus] spawnWorld est NULL ! Utilisation de l'original spawn_minijeux");
        }
        
        Location loc = new Location(world, -11, 104, 0);
        loc.setYaw(90f); // Regarder vers l'ouest
        loc.setPitch(0f);
        return loc;
    }
    
    /**
     * Récupérer la location du spawn pour cette partie
     */
    public Location getSpawnLocation() {
        return getSpawnMinijeux();
    }
    
    // ========== UTILS ==========
    
    /**
     * Sauvegarder les items consommables d'un joueur (appelé à la mort ou fin de round)
     */
    public void savePlayerConsumables(Player player) {
        saveUnusedConsumables(player);
    }
    
    private void saveUnusedConsumables(Player player) {
        // Clear les items consommables actuels
        shopData.clearConsumableItems(player.getUniqueId());
        
        // Scanner l'inventaire pour les items consommables
        for (org.bukkit.inventory.ItemStack item : player.getInventory().getContents()) {
            if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) continue;
            
            String displayName = item.getItemMeta().getDisplayName();
            org.bukkit.Material material = item.getType();
            
            // Vérifier si c'est un item consommable du shop
            if (isConsumableItem(material, displayName)) {
                shopData.addConsumableItem(player.getUniqueId(), material, displayName);
            }
        }
    }
    
    private boolean isConsumableItem(org.bukkit.Material material, String displayName) {
        // Liste des items consommables du shop
        return (material == org.bukkit.Material.WEB && displayName.contains("Grenade Fumigène")) ||
               (material == org.bukkit.Material.FIREBALL && displayName.contains("Cocktail Molotov")) ||
               (material == org.bukkit.Material.STONE_PLATE && displayName.contains("Mine")) ||
               (material == org.bukkit.Material.FEATHER && displayName.contains("Grenade Propulse")) ||
               (material == org.bukkit.Material.IRON_AXE && displayName.contains("Axe Élémentaire"));
    }
    
    private List<Player> getOnlinePlayers() {
        return game.getPlayers().stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    private List<Player> getAlivePlayers() {
        return getOnlinePlayers().stream()
                .filter(p -> alivePlayers.getOrDefault(p.getUniqueId(), false))
                .collect(Collectors.toList());
    }
    
    private List<Player> getPlayersByKills() {
        return getOnlinePlayers().stream()
                .sorted((p1, p2) -> Integer.compare(pointsManager.getKills(p2), pointsManager.getKills(p1)))
                .collect(Collectors.toList());
    }
    
    private List<Player> getPlayersByPoints() {
        return getOnlinePlayers().stream()
                .sorted((p1, p2) -> Integer.compare(pointsManager.getPoints(p2), pointsManager.getPoints(p1)))
                .collect(Collectors.toList());
    }
    
    private void broadcast(String message) {
        for (Player player : getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }
}
