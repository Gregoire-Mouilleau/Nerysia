package com.nerysia.plugin.game.focus;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Gère les scoreboards individuels pour chaque joueur en partie Focus
 */
public class FocusScoreboardManager {
    
    private final String[] arrowFrames = {
        ">", ">>", ">>>", ">>>>", ">>>>>", ">>>>", ">>>", ">>", ">"
    };
    private int arrowIndex = 0;
    private final String[] arrowColors = {"§1", "§b"}; // bleu foncé et bleu clair
    
    private final Map<UUID, Scoreboard> playerScoreboards = new HashMap<>();
    private boolean isTitleVelmorya = true;
    
    private static final String SERVER_NAME = "Nérysia";
    private static final String MODE_NAME = "Focus";
    
    private final FocusGame game;
    private final FocusPointsManager pointsManager;
    
    public FocusScoreboardManager(FocusGame game, FocusPointsManager pointsManager) {
        this.game = game;
        this.pointsManager = pointsManager;
    }
    
    /**
     * Crée un scoreboard unique pour un joueur
     */
    public void createScoreboard(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("focus", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        player.setScoreboard(scoreboard);
        playerScoreboards.put(player.getUniqueId(), scoreboard);
        
        updateScoreboard(player, scoreboard);
    }
    
    /**
     * Met à jour le scoreboard d'un joueur
     */
    public void updateScoreboard(Player player, Scoreboard scoreboard) {
        Objective objective = scoreboard.getObjective("focus");
        if (objective == null) {
            objective = scoreboard.registerNewObjective("focus", "dummy");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        }
        
        // Clear les anciennes entrées
        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }
        
        // Titre animé
        String core = isTitleVelmorya ? SERVER_NAME : MODE_NAME;
        String color = arrowColors[arrowIndex % arrowColors.length];
        String left = color + arrowFrames[arrowIndex];
        String right = color + arrowFrames[arrowIndex].replace('>', '<');
        objective.setDisplayName(left + " §f" + core + " " + right);
        
        // Récupérer la couleur du grade du joueur (depuis le scoreboard global)
        String colorPrefix = "§f";
        Team globalTeam = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        if (globalTeam != null && globalTeam.getPrefix() != null) {
            colorPrefix = globalTeam.getPrefix();
        }
        
        // Créer une team locale pour colorier le nom du joueur
        Team localTeam = scoreboard.getTeam("self");
        if (localTeam == null) {
            localTeam = scoreboard.registerNewTeam("self");
        }
        localTeam.addEntry(player.getName());
        localTeam.setPrefix(colorPrefix);
        
        int line = 18;
        objective.getScore("§7             <-------->").setScore(line--);
        objective.getScore("        ").setScore(line--);
        objective.getScore(colorPrefix + "★ " + player.getName()).setScore(line--);
        line--; // espace
        objective.getScore("§8☠ Kills: " + pointsManager.getKills(player)).setScore(line--);
        objective.getScore("§b» Rounds: " + pointsManager.getRoundsWon(player)).setScore(line--);
        objective.getScore("§6⚡ Points: " + pointsManager.getPoints(player)).setScore(line--);
        objective.getScore("  ").setScore(line--);
        
        // Séparateur
        objective.getScore("§7              <-------->").setScore(line--);
        objective.getScore("          ").setScore(line--);
        
        // Condition de victoire
        FocusGameSettings settings = game.getSettings();
        String winCondition = settings.getVictoryCondition().name().toLowerCase();
        int killsReq = settings.getKillsToWin();
        int roundsReq = settings.getRoundsToWin();
        
        if (winCondition.equals("kills")) {
            objective.getScore("§eVictoire : §8☠ " + killsReq + " kills").setScore(line--);
        } else if (winCondition.equals("rounds")) {
            objective.getScore("§eVictoire : §b⇪ " + roundsReq + " rounds").setScore(line--);
        } else {
            objective.getScore("§eVictoire : " + killsReq + "k & " + roundsReq + "R").setScore(line--);
        }
        
        objective.getScore("           ").setScore(line--);
        
        // Top 3
        List<Player> topPlayers = getTop3Players(settings.getVictoryCondition());
        for (int rank = 1; rank <= 3; rank++) {
            String prefix = "§6Top " + rank + " : ";
            
            if (rank <= topPlayers.size()) {
                Player p = topPlayers.get(rank - 1);
                
                // Récupérer la couleur du grade
                String topColor = "§f";
                Team t = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(p.getName());
                if (t != null && t.getPrefix() != null) {
                    topColor = t.getPrefix();
                }
                
                String stat = "";
                if (winCondition.equals("kills")) {
                    stat = " §8☠ " + pointsManager.getKills(p);
                } else if (winCondition.equals("rounds")) {
                    stat = " §b⇪ " + pointsManager.getRoundsWon(p);
                }
                // Pour "both", on affiche juste le pseudo
                
                objective.getScore(prefix + topColor + p.getName() + stat).setScore(line--);
            } else {
                objective.getScore(prefix).setScore(line--);
            }
        }
    }
    
    /**
     * Met à jour ou crée le scoreboard d'un joueur
     */
    public void updateOrCreateScoreboard(Player player) {
        Scoreboard scoreboard = playerScoreboards.get(player.getUniqueId());
        
        if (scoreboard == null || scoreboard.getObjective("focus") == null) {
            createScoreboard(player);
        } else {
            updateScoreboard(player, scoreboard);
        }
    }
    
    /**
     * Met à jour tous les scoreboards
     */
    public void updateAllScoreboards() {
        for (UUID playerId : game.getPlayers()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                updateOrCreateScoreboard(player);
            }
        }
    }
    
    /**
     * Avance l'animation des flèches
     */
    public void tickAnimation() {
        int previousArrowIndex = arrowIndex;
        arrowIndex = (arrowIndex + 1) % arrowFrames.length;
        
        String previousFrame = arrowFrames[previousArrowIndex];
        String currentFrame = arrowFrames[arrowIndex];
        
        // Switch le titre quand on passe de >>>>> vers >>>>
        if (previousFrame.equals(">>>>>") && currentFrame.equals(">>>>")) {
            isTitleVelmorya = !isTitleVelmorya;
        }
        
        updateAllScoreboards();
    }
    
    /**
     * Retire le scoreboard d'un joueur
     */
    public void removeScoreboard(Player player) {
        playerScoreboards.remove(player.getUniqueId());
        // Remettre le scoreboard par défaut
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }
    
    /**
     * Nettoie tous les scoreboards
     */
    public void cleanup() {
        for (UUID playerId : new ArrayList<>(playerScoreboards.keySet())) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                removeScoreboard(player);
            }
        }
        playerScoreboards.clear();
    }
    
    /**
     * Récupère le top 3 des joueurs selon la condition de victoire
     */
    private List<Player> getTop3Players(FocusGameSettings.VictoryCondition condition) {
        List<Player> allPlayers = game.getPlayers().stream()
            .map(Bukkit::getPlayer)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
        allPlayers.sort((p1, p2) -> {
            int val1 = getScoreValue(p1, condition);
            int val2 = getScoreValue(p2, condition);
            return Integer.compare(val2, val1); // Tri décroissant
        });
        
        return allPlayers.subList(0, Math.min(3, allPlayers.size()));
    }
    
    /**
     * Calcule le score d'un joueur selon la condition de victoire
     */
    private int getScoreValue(Player player, FocusGameSettings.VictoryCondition condition) {
        int kills = pointsManager.getKills(player);
        int rounds = pointsManager.getRoundsWon(player);
        
        switch (condition) {
            case KILLS:
                return kills;
            case ROUNDS:
                return rounds;
            case KILLS_AND_ROUNDS:
                FocusGameSettings settings = game.getSettings();
                int killRequirement = settings.getKillsToWin();
                int roundRequirement = settings.getRoundsToWin();
                double killRatio = killRequirement > 0 ? (double) kills / killRequirement : 0;
                double roundRatio = roundRequirement > 0 ? (double) rounds / roundRequirement : 0;
                return (int) ((killRatio + roundRatio) * 1000);
            default:
                return 0;
        }
    }
}
