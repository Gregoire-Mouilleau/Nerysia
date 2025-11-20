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
        PREPARATION,  // Phase de préparation avant premier round
        INGAME,       // Round en cours
        ROUND_END,    // Fin de round (affichage résultats)
        GAME_END      // Fin de partie (affichage winner)
    }
    
    private static final int STARTING_POINTS = 10; // Points de départ
    
    private final FocusGame game;
    private final FocusPointsManager pointsManager;
    private final FocusShopData shopData;
    private final FocusShopGUI shopGUI;
    private final FocusMapManager mapManager;
    private final FocusGameManager gameManager; // Pour nettoyer les joueurs à la fin
    private final FocusScoreboardManager scoreboardManager;
    
    private State state;
    private final Set<UUID> readyPlayers;
    private List<Location> currentSpawns;
    private final Map<UUID, Boolean> alivePlayers; // true = alive, false = dead
    private World gameWorld; // Le monde dupliqué pour cette partie
    private World spawnWorld; // Le spawn_minijeux dupliqué pour cette partie
    
    // Tracking des conditions de victoire
    private final Map<UUID, Long> victoryTimestamp; // Timestamp quand le joueur remplit les conditions
    
    // Tracking du classement du round (ordre de mort)
    private final List<UUID> roundDeathOrder; // Ordre de mort (le premier mort est en position 0)
    
    public FocusGameController(FocusGame game, FocusPointsManager pointsManager, FocusShopData shopData, FocusShopGUI shopGUI, FocusGameManager gameManager) {
        this.game = game;
        this.pointsManager = pointsManager;
        this.shopData = shopData;
        this.shopGUI = shopGUI;
        this.mapManager = new FocusMapManager();
        this.gameManager = gameManager;
        this.scoreboardManager = new FocusScoreboardManager(game, pointsManager);
        this.state = State.WAITING;
        this.readyPlayers = new HashSet<>();
        this.alivePlayers = new HashMap<>();
        this.victoryTimestamp = new HashMap<>();
        this.roundDeathOrder = new ArrayList<>();
        this.gameWorld = null;
        this.spawnWorld = null;
        
        // Démarrer l'animation du scoreboard (1 seconde = 20 ticks)
        Bukkit.getScheduler().runTaskTimer(Nerysia.getInstance(), () -> {
            scoreboardManager.tickAnimation();
        }, 0L, 20L);
    }
    
    public State getState() {
        return state;
    }
    
    public FocusShopData getShopData() {
        return shopData;
    }
    
    public boolean isAlive(Player player) {
        return alivePlayers.getOrDefault(player.getUniqueId(), false);
    }
    
    public void setAlive(Player player, boolean alive) {
        alivePlayers.put(player.getUniqueId(), alive);
    }
    
    public FocusGame getGame() {
        return game;
    }
    
    /**
     * Met à jour le scoreboard d'un joueur spécifique
     */
    public void updatePlayerScoreboard(Player player) {
        scoreboardManager.updateOrCreateScoreboard(player);
    }
    
    /**
     * Vérifie si le round doit se terminer (1 seul joueur vivant)
     */
    public void checkRoundEnd() {
        // Seulement si on est en jeu
        if (state != State.INGAME) return;
        
        // Compter les joueurs vivants (non-spectateur et dans la partie)
        List<Player> alivePlayers = new ArrayList<>();
        for (UUID playerId : game.getPlayers()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline() && player.getGameMode() != GameMode.SPECTATOR) {
                alivePlayers.add(player);
            }
        }
        
        // Si 1 seul joueur vivant, il gagne le round
        if (alivePlayers.size() == 1) {
            Player winner = alivePlayers.get(0);
            Bukkit.getLogger().info("[Focus] Un seul joueur vivant, " + winner.getName() + " remporte le round !");
            endRound(winner);
        } else if (alivePlayers.size() == 0) {
            // Aucun joueur vivant (tous déco), retour au lobby sans winner
            Bukkit.getLogger().info("[Focus] Aucun joueur vivant, retour au lobby");
            returnToLobby();
        }
    }
    
    // ========== MAP MANAGEMENT ==========
    
    /**
     * Initialise la partie en dupliquant la map Focus
     * @return true si succès, false si erreur
     */
    public boolean initializeGame() {
        broadcast(ChatColor.GOLD + "⏳ Chargement de la map...");
        
        // Dupliquer le spawn_minijeux
        spawnWorld = mapManager.duplicateSpawnMinijeux(game.getGameId());
        if (spawnWorld == null) {
            broadcast(ChatColor.RED + "✘ Erreur lors du chargement !");
            return false;
        }
        
        // Dupliquer la map de jeu
        gameWorld = mapManager.duplicateMap(game.getGameId());
        if (gameWorld == null) {
            broadcast(ChatColor.RED + "✘ Erreur lors du chargement !");
            // Nettoyer le spawn créé
            if (spawnWorld != null) {
                mapManager.deleteWorld(spawnWorld.getName());
                spawnWorld = null;
            }
            return false;
        }
        
        broadcast(ChatColor.GREEN + "✔ Map chargée avec succès !");
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
        if (state != State.WAITING && state != State.LOBBY && state != State.PREPARATION) {
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
    
    // ========== PREPARATION PHASE ==========
    
    public void startPreparation() {
        state = State.PREPARATION;
        readyPlayers.clear();
        
        // Donner les points de départ à tous les joueurs
        for (UUID playerId : game.getPlayers()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                pointsManager.addPoints(player, STARTING_POINTS);
                // Créer le scoreboard pour le joueur
                scoreboardManager.createScoreboard(player);
            }
        }
        
        // Téléporter au spawn_minijeux et donner l'accès au shop
        Location spawnLocation = getSpawnMinijeux();
        
        for (Player player : getOnlinePlayers()) {
            player.teleport(spawnLocation);
            player.setHealth(20.0);
            player.setFoodLevel(20);
            player.setGameMode(GameMode.SURVIVAL);
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            
            // Retirer tous les effets
            for (org.bukkit.potion.PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
            
            // Donner l'équipement de lobby (false = pas en jeu, donne l'item du shop)
            shopGUI.givePlayerEquipment(player, false);
            
            // Ouvrir automatiquement le shop
            shopGUI.open(player, this);
        }
        
        broadcast("");
        broadcast(ChatColor.GOLD + "✦ Phase de préparation ✦");
        broadcast("");
        broadcast(ChatColor.YELLOW + "⚡ +" + STARTING_POINTS + " points de départ");
        broadcast(ChatColor.GRAY + "Équipez-vous puis tapez " + ChatColor.WHITE + "/ready");
        broadcast("");
    }
    
    private void startCountdown() {
        broadcast("");
        broadcast(ChatColor.GREEN + "✔ Tous les joueurs sont prêts !");
        broadcast(ChatColor.YELLOW + "⏱ Lancement dans 3 secondes...");
        broadcast("");
        
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
        
        // Réinitialiser l'ordre de mort du round
        roundDeathOrder.clear();
        
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
        
        broadcast("");
        broadcast(ChatColor.GOLD + "⚔ Le round commence !");
        broadcast(ChatColor.GRAY + "Soyez le dernier en vie");
        broadcast("");
        
        // Mettre à jour les scoreboards
        scoreboardManager.updateAllScoreboards();
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
        
        // Enregistrer l'ordre de mort (le premier mort sera en position 0)
        roundDeathOrder.add(victim.getUniqueId());
        
        // Donner des points au tueur (points configurables)
        if (killer != null && killer != victim) {
            int killPoints = game.getSettings().getPointsPerKill();
            pointsManager.registerKill(killer);
            pointsManager.addPoints(killer, killPoints);
            killer.sendMessage(ChatColor.GREEN + "⚡ " + ChatColor.GOLD + "+" + killPoints + " points");
            
            // Mettre à jour le scoreboard du tueur
            scoreboardManager.updateOrCreateScoreboard(killer);
        }
        
        // Mettre à jour aussi le scoreboard de la victime (pour voir le top 3 actualisé)
        scoreboardManager.updateAllScoreboards();
        
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
            broadcast("");
            broadcast(ChatColor.YELLOW + "🏆 Fin du round");
            broadcast("");
            broadcast(ChatColor.GREEN + "✦ " + winner.getName() + ChatColor.GREEN + " remporte le round !");
            broadcast("");
            
            // Distribuer les points selon le classement (ordre de survie)
            distributeRankingPoints(winner);
            broadcast("");
        } else {
            broadcast(ChatColor.RED + "✘ Aucun gagnant pour ce round");
        }
        
        // Mettre à jour les scoreboards après l'attribution des points
        scoreboardManager.updateAllScoreboards();
        
        // Vérifier si quelqu'un a gagné la partie (vérifier TOUS les joueurs, pas seulement le winner du round)
        Player gameWinner = checkAllPlayersForVictory();
        if (gameWinner != null) {
            Bukkit.getLogger().info("[Focus] Fin de partie déclenchée pour " + gameWinner.getName() + "!");
            endGame(gameWinner);
            return;
        }
        
        // Retour au lobby après 3 secondes
        broadcast(ChatColor.YELLOW + "⏱ Retour au spawn dans 3 secondes...");
        Bukkit.getLogger().info("[Focus] Planification du retour au lobby dans 3 secondes...");
        Bukkit.getScheduler().runTaskLater(Nerysia.getInstance(), () -> {
            Bukkit.getLogger().info("[Focus] Exécution du retour au lobby maintenant!");
            returnToLobby();
        }, 60L); // 3 secondes
    }
    
    /**
     * Vérifie tous les joueurs pour voir si quelqu'un a rempli les conditions de victoire
     * Retourne le joueur qui a gagné (celui qui a rempli les conditions en premier)
     */
    private Player checkAllPlayersForVictory() {
        FocusGameSettings.VictoryCondition condition = game.getSettings().getVictoryCondition();
        int killsToWin = game.getSettings().getKillsToWin();
        int roundsToWin = game.getSettings().getRoundsToWin();
        
        // Vérifier chaque joueur
        for (Player player : getOnlinePlayers()) {
            int playerKills = pointsManager.getKills(player);
            int playerRounds = pointsManager.getRoundsWon(player);
            
            Bukkit.getLogger().info("[Focus] Vérification victoire pour " + player.getName() + " - Condition: " + condition + 
                                    " - Kills: " + playerKills + "/" + killsToWin + " - Rounds: " + playerRounds + "/" + roundsToWin);
            
            boolean meetsCondition = false;
            
            switch (condition) {
                case KILLS:
                    meetsCondition = playerKills >= killsToWin;
                    break;
                case ROUNDS:
                    meetsCondition = playerRounds >= roundsToWin;
                    break;
                case KILLS_AND_ROUNDS:
                    meetsCondition = (playerKills >= killsToWin) && (playerRounds >= roundsToWin);
                    break;
            }
            
            if (meetsCondition) {
                // Enregistrer le moment où le joueur a rempli les conditions (si pas déjà fait)
                if (!victoryTimestamp.containsKey(player.getUniqueId())) {
                    victoryTimestamp.put(player.getUniqueId(), System.currentTimeMillis());
                    Bukkit.getLogger().info("[Focus] " + player.getName() + " a rempli les conditions de victoire!");
                }
            }
        }
        
        // Si au moins un joueur a rempli les conditions, retourner celui qui les a remplies en premier
        if (!victoryTimestamp.isEmpty()) {
            long earliestTime = Long.MAX_VALUE;
            UUID winnerId = null;
            
            for (Map.Entry<UUID, Long> entry : victoryTimestamp.entrySet()) {
                if (entry.getValue() < earliestTime) {
                    earliestTime = entry.getValue();
                    winnerId = entry.getKey();
                }
            }
            
            if (winnerId != null) {
                Player winner = Bukkit.getPlayer(winnerId);
                if (winner != null) {
                    Bukkit.getLogger().info("[Focus] Vainqueur final: " + winner.getName() + " (a rempli les conditions en premier)");
                }
                return winner;
            }
        }
        
        return null;
    }
    
    private void returnToLobby() {
        state = State.LOBBY;
        readyPlayers.clear(); // Reset les ready pour le prochain round
        
        // Reset kills du round (pas les kills totaux)
        pointsManager.resetRoundKills();
        
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
            
            // Donner l'équipement de lobby (false = pas en jeu, donne l'item du shop)
            shopGUI.givePlayerEquipment(player, false);
            
            // Ouvrir automatiquement le shop
            shopGUI.open(player, this);
        }
        
        // Mettre à jour les scoreboards
        scoreboardManager.updateAllScoreboards();
        
        broadcast("");
        broadcast(ChatColor.GOLD + "🛒 Shop ouvert !");
        broadcast(ChatColor.GRAY + "Améliorez votre équipement");
        broadcast(ChatColor.YELLOW + "Tapez " + ChatColor.WHITE + "/ready" + ChatColor.YELLOW + " quand vous êtes prêt");
        broadcast("");
    }
    
    private void endGame(Player winner) {
        state = State.GAME_END;
        
        // Laisser le round se terminer normalement
        broadcast("");
        broadcast(ChatColor.YELLOW + "🏆 " + winner.getName() + ChatColor.YELLOW + " remporte la partie ! 🏆");
        broadcast("");
        
        // Afficher les stats
        broadcast(ChatColor.AQUA + "📊 Classement final:");
        broadcast("");
        
        FocusGameSettings.VictoryCondition condition = game.getSettings().getVictoryCondition();
        List<Player> ranking = getPlayersByPoints();
        
        for (int i = 0; i < Math.min(3, ranking.size()); i++) {
            Player p = ranking.get(i);
            int points = pointsManager.getPoints(p);
            int kills = pointsManager.getKills(p);
            int rounds = pointsManager.getRoundsWon(p);
            String medal = i == 0 ? "🥇" : i == 1 ? "🥈" : "🥉";
            
            // Afficher les stats selon la condition de victoire
            String stats = "";
            switch (condition) {
                case KILLS:
                    stats = ChatColor.GRAY + String.valueOf(kills) + " kills " + ChatColor.DARK_GRAY + "• " + 
                           ChatColor.GRAY + String.valueOf(points) + " pts";
                    break;
                case ROUNDS:
                    stats = ChatColor.GRAY + String.valueOf(rounds) + " rounds " + ChatColor.DARK_GRAY + "• " + 
                           ChatColor.GRAY + String.valueOf(points) + " pts";
                    break;
                case KILLS_AND_ROUNDS:
                    stats = ChatColor.GRAY + String.valueOf(kills) + " kills " + ChatColor.DARK_GRAY + "• " +
                           ChatColor.GRAY + String.valueOf(rounds) + " rounds " + ChatColor.DARK_GRAY + "• " +
                           ChatColor.GRAY + String.valueOf(points) + " pts";
                    break;
            }
            
            broadcast(ChatColor.YELLOW + medal + " " + p.getName() + ChatColor.DARK_GRAY + " • " + stats);
        }
        broadcast("");
        
        // Lancer des feux d'artifice pour le gagnant
        Bukkit.getScheduler().runTask(Nerysia.getInstance(), () -> {
            launchFireworks(winner.getLocation(), 5);
        });
        
        // Téléporter au spawn_minijeux et nettoyer après 10 secondes
        Bukkit.getScheduler().runTaskLater(Nerysia.getInstance(), () -> {
            teleportAllToSpawn();
            
            // Message visible uniquement dans ce monde
            Location spawnLoc = getSpawnMinijeux();
            for (Player p : spawnLoc.getWorld().getPlayers()) {
                p.sendMessage("");
                p.sendMessage(ChatColor.YELLOW + "✨ Partie terminée !");
                p.sendMessage("");
                p.sendMessage(ChatColor.GREEN + "Vainqueur: " + ChatColor.GOLD + winner.getName());
                p.sendMessage("");
                p.sendMessage(ChatColor.GRAY + "⏱ Retour au lobby dans 10s...");
                p.sendMessage("");
            }
            
            // Fireworks au spawn
            launchFireworks(spawnLoc, 10);
            
            // Retour au lobby après 10 secondes supplémentaires
            Bukkit.getScheduler().runTaskLater(Nerysia.getInstance(), () -> {
                cleanup();
            }, 200L); // 10 secondes
        }, 100L); // 5 secondes pour voir le round se terminer
    }
    
    /**
     * Lance des feux d'artifice à une location
     */
    private void launchFireworks(Location location, int count) {
        for (int i = 0; i < count; i++) {
            Bukkit.getScheduler().runTaskLater(Nerysia.getInstance(), () -> {
                org.bukkit.entity.Firework firework = location.getWorld().spawn(location, org.bukkit.entity.Firework.class);
                org.bukkit.inventory.meta.FireworkMeta meta = firework.getFireworkMeta();
                
                org.bukkit.FireworkEffect effect = org.bukkit.FireworkEffect.builder()
                    .with(org.bukkit.FireworkEffect.Type.BALL_LARGE)
                    .withColor(org.bukkit.Color.YELLOW, org.bukkit.Color.ORANGE, org.bukkit.Color.RED)
                    .withFade(org.bukkit.Color.WHITE)
                    .trail(true)
                    .flicker(true)
                    .build();
                
                meta.addEffect(effect);
                meta.setPower(1);
                firework.setFireworkMeta(meta);
            }, i * 10L); // Échelonner les feux d'artifice
        }
    }
    
    /**
     * Téléporte tous les joueurs au spawn_minijeux
     */
    private void teleportAllToSpawn() {
        Location spawnLocation = getSpawnMinijeux();
        
        for (UUID playerId : game.getPlayers()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.teleport(spawnLocation);
                player.setGameMode(GameMode.SURVIVAL);
                player.setHealth(20.0);
                player.setFoodLevel(20);
                player.getInventory().clear();
                player.getInventory().setArmorContents(null);
                
                // Retirer tous les effets
                for (org.bukkit.potion.PotionEffect effect : player.getActivePotionEffects()) {
                    player.removePotionEffect(effect.getType());
                }
            }
        }
    }
    
    private void cleanup() {
        // Copier la liste des joueurs car on va modifier la map pendant l'itération
        List<UUID> playerIds = new ArrayList<>(game.getPlayers());
        
        for (UUID playerId : playerIds) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                // Reset complet de l'état du joueur
                player.setGameMode(GameMode.SURVIVAL);
                player.setHealth(20.0);
                player.setMaxHealth(20.0);
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
                
                // Retirer le scoreboard Focus
                scoreboardManager.removeScoreboard(player);
                
                // Faire exécuter la commande /lobby pour téléporter avec tous les effets (NPC, etc.)
                player.performCommand("lobby");
            }
            
            // IMPORTANT: Retirer le joueur de la map playerToGame
            gameManager.removePlayerFromGame(playerId);
        }
        
        // Marquer la partie comme terminée
        game.setState(FocusGameState.FINISHED);
        
        readyPlayers.clear();
        alivePlayers.clear();
        victoryTimestamp.clear();
        state = State.WAITING;
        
        // Supprimer les mondes dupliqués après un court délai
        Bukkit.getScheduler().runTaskLater(Nerysia.getInstance(), () -> {
            cleanupGame();
            
            // NE PAS supprimer la partie - elle reste dans l'historique avec l'état FINISHED
            Bukkit.getLogger().info("[Focus] Partie " + game.getGameId() + " terminée et conservée dans l'historique");
        }, 40L); // 2 secondes
    }
    
    // ========== SPAWNS ==========
    
    public void setSpawns(List<Location> spawns) {
        this.currentSpawns = new ArrayList<>(spawns);
        Collections.shuffle(this.currentSpawns);
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
               (material == org.bukkit.Material.REDSTONE && displayName.contains("Mine")) ||
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
    
    /**
     * Distribue les points selon le classement du round
     * Le gagnant (dernier en vie) est 1er, l'avant-dernier mort est 2ème, etc.
     */
    private void distributeRankingPoints(Player winner) {
        FocusGameSettings settings = game.getSettings();
        
        // Créer la liste des placements (du dernier au premier)
        // roundDeathOrder contient l'ordre de mort (index 0 = premier mort)
        List<UUID> placementOrder = new ArrayList<>();
        
        // Le gagnant est 1er (dernier en vie)
        placementOrder.add(winner.getUniqueId());
        
        // Ajouter les joueurs dans l'ordre inverse de mort (du dernier mort au premier mort)
        for (int i = roundDeathOrder.size() - 1; i >= 0; i--) {
            placementOrder.add(roundDeathOrder.get(i));
        }
        
        // Distribuer les points selon le placement
        for (int i = 0; i < placementOrder.size(); i++) {
            UUID playerId = placementOrder.get(i);
            Player player = Bukkit.getPlayer(playerId);
            if (player == null) continue;
            
            int placement = i + 1; // 1 = 1er, 2 = 2ème, etc.
            int points = settings.getPointsForPlacement(placement);
            
            pointsManager.addPoints(player, points);
            
            // Message avec emoji selon le placement
            String medal = "";
            if (placement == 1) medal = "🥇";
            else if (placement == 2) medal = "🥈";
            else if (placement == 3) medal = "🥉";
            
            String message = ChatColor.GOLD + medal + " +" + points + " points (" + getOrdinal(placement) + " place)";
            player.sendMessage(message);
        }
    }
    
    /**
     * Retourne le suffixe ordinal (1er, 2ème, 3ème, etc.)
     */
    private String getOrdinal(int number) {
        if (number == 1) return "1er";
        return number + "ème";
    }
    
    private void broadcast(String message) {
        for (Player player : getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }
}
