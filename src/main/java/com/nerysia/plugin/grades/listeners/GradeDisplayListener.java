package com.nerysia.plugin.grades.listeners;

import com.nerysia.plugin.Nerysia;
import com.nerysia.plugin.grades.Grade;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class GradeDisplayListener implements Listener {

    private Nerysia plugin;

    public GradeDisplayListener(Nerysia plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Si c'est la première connexion, enregistrer le grade Joueur
        if (!plugin.getGradeManager().hasGradeSet(player.getUniqueId())) {
            plugin.getGradeManager().setGrade(player.getUniqueId(), Grade.JOUEUR);
        }
        
        updatePlayerDisplay(player);
        
        // Mettre à jour l'affichage pour tous les joueurs en ligne
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            updatePlayerDisplay(online);
        }
    }

    public void updatePlayerDisplay(Player player) {
        Grade grade = plugin.getGradeManager().getGrade(player.getUniqueId());
        
        // Vérifier la longueur du pseudo avant d'ajouter le tag
        String prefix = grade.getPrefix() + "[" + grade.getTabName() + "] §f";
        String displayName = prefix + player.getName() + "§r";
        
        // Mettre à jour le nom affiché (displayName pour le chat)
        player.setDisplayName(displayName);
        
        // Mettre à jour le nom dans le tab
        player.setPlayerListName(displayName);
        
        // Mettre à jour le nametag au-dessus de la tête avec une team
        // Seulement si le tag + pseudo ne dépasse pas 16 caractères
        updatePlayerNameTag(player, grade);
    }

    private void updatePlayerNameTag(Player player, Grade grade) {
        Scoreboard scoreboard = player.getScoreboard();
        
        // Si le joueur n'a pas de scoreboard, en créer un
        if (scoreboard == Bukkit.getScoreboardManager().getMainScoreboard()) {
            scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            player.setScoreboard(scoreboard);
        }
        
        // Calculer la longueur du préfixe (en retirant les codes couleur)
        String prefixText = grade.getPrefix() + "[" + grade.getShortName() + "] ";
        
        // Compter les caractères visibles (sans les codes couleur §X)
        String prefixWithoutColor = prefixText.replaceAll("§.", "");
        
        // Si le préfixe seul dépasse 16 caractères, ne pas ajouter de tag
        if (prefixWithoutColor.length() > 16) {
            return;
        }
        
        String teamName = "grade_" + grade.name();
        Team team = scoreboard.getTeam(teamName);
        
        // Créer la team si elle n'existe pas
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
        }
        
        // Configurer le préfixe de la team
        team.setPrefix(prefixText);
        team.setSuffix("");
        
        // Retirer le joueur de toutes les autres teams
        for (Team t : scoreboard.getTeams()) {
            t.removeEntry(player.getName());
        }
        
        // Ajouter le joueur à sa team
        team.addEntry(player.getName());
    }
}

