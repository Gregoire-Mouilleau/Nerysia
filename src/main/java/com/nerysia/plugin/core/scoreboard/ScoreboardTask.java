package com.nerysia.plugin.core.scoreboard;

import com.nerysia.plugin.Nerysia;
import com.nerysia.plugin.grades.Grade;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class ScoreboardTask implements Runnable {

    private Nerysia plugin;
    private int animationState = 0;
    private String[] titleAnimation = {
        "§6> §fNérysia §6<",
        "§6>> §fNérysia §6<<",
        "§6>>> §fNérysia §6<<<",
        "§6>>>> §fNérysia §6<<<<",
        "§6>>> §fNérysia §6<<<",
        "§6>> §fNérysia §6<<",
    };

    public ScoreboardTask(Nerysia plugin) {
        this.plugin = plugin;
    }

    public void start() {
        // Lancer la tâche toutes les 20 ticks (1s) pour l'animation
        Bukkit.getScheduler().runTaskTimer(plugin, this, 0L, 20L);
    }

    @Override
    public void run() {
        // Mettre à jour le titre animé
        String currentTitle = titleAnimation[animationState];
        animationState = (animationState + 1) % titleAnimation.length;

        for (Player player : Bukkit.getOnlinePlayers()) {
            // Vérifier si le joueur est dans le monde Lobby
            if (player.getWorld().getName().equals("Lobby")) {
                updateScoreboard(player, currentTitle);
            } else {
                // Retirer le scoreboard si le joueur n'est pas dans le Lobby
                player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            }
        }
    }

    private void updateScoreboard(Player player, String title) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("nerysia", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(title);

        // Ligne vide en haut
        objective.getScore("§1").setScore(12);
        
        // Pseudo du joueur avec étoile
        objective.getScore("§e✦ §f" + player.getName()).setScore(11);
        
        // Séparateur
        objective.getScore("§8     <------->").setScore(10);
        
        // Ligne vide
        objective.getScore("§2").setScore(9);
        
        // Grade
        Grade grade = plugin.getGradeManager().getGrade(player.getUniqueId());
        objective.getScore("§b⚙ §bGrade §f: " + grade.getPrefix() + grade.getTabName()).setScore(8);
        
        // Coins
        objective.getScore("§e⛃ §eCoins §f: §a0").setScore(7);
        
        // Ligne vide
        objective.getScore("§3").setScore(6);
        
        // En ligne
        objective.getScore("§8     <-------> ").setScore(5);
        objective.getScore("§a⚔ §aEn ligne §f: §a" + Bukkit.getOnlinePlayers().size()).setScore(4);
        
        // Ligne vide en bas
        objective.getScore("§4").setScore(3);

        // Ajouter les teams pour les nametags
        setupPlayerTeams(scoreboard);

        player.setScoreboard(scoreboard);
    }

    private void setupPlayerTeams(Scoreboard scoreboard) {
        // Créer les teams pour chaque grade et ajouter tous les joueurs
        for (Player online : Bukkit.getOnlinePlayers()) {
            Grade grade = plugin.getGradeManager().getGrade(online.getUniqueId());
            String teamName = "g_" + grade.ordinal(); // Utiliser un nom court
            Team team = scoreboard.getTeam(teamName);
            
            if (team == null) {
                team = scoreboard.registerNewTeam(teamName);
                // Utiliser le nom court pour le nametag
                team.setPrefix(grade.getPrefix() + "[" + grade.getShortName() + "] §r");
                team.setSuffix("");
            }
            
            team.addEntry(online.getName());
        }
    }

    public void createScoreboardForPlayer(Player player) {
        // Créer le scoreboard seulement si le joueur est dans le Lobby
        if (player.getWorld().getName().equals("Lobby")) {
            updateScoreboard(player, titleAnimation[0]);
        }
    }
}
