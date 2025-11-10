package com.nerysia.plugin.grades.commands;

import com.nerysia.plugin.Nerysia;
import com.nerysia.plugin.grades.Grade;
import com.nerysia.plugin.grades.GradeManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GradeCommand implements CommandExecutor {

    private GradeManager gradeManager;
    private Nerysia plugin;

    public GradeCommand(GradeManager gradeManager, Nerysia plugin) {
        this.gradeManager = gradeManager;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("nerysia.grade")) {
            sender.sendMessage("§cVous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /grade <joueur> <grade>");
            sender.sendMessage("§cGrades disponibles: Fondateur, Responsable, Administrateur, Moderateur, ModérateurTest, Streameur, Joueur");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("§cLe joueur §e" + args[0] + " §cn'est pas connecté.");
            return true;
        }

        Grade grade = Grade.getByName(args[1]);
        if (grade == null) {
            sender.sendMessage("§cGrade invalide. Grades disponibles:");
            for (Grade g : Grade.values()) {
                sender.sendMessage("§7- §e" + g.getName());
            }
            return true;
        }

        gradeManager.setGrade(target.getUniqueId(), grade);
        sender.sendMessage("§aVous avez défini le grade de §e" + target.getName() + " §aà " + grade.getDisplayName());
        target.sendMessage("§aVotre grade a été défini à " + grade.getDisplayName());

        // Mettre à jour le display name du joueur
        String displayName = grade.getPrefix() + "[" + grade.getTabName() + "] §f" + target.getName() + "§r";
        target.setDisplayName(displayName);
        
        // Forcer la mise à jour du scoreboard pour tous les joueurs
        if (target.getWorld().getName().equals("Lobby")) {
            plugin.getScoreboardTask().createScoreboardForPlayer(target);
        }

        return true;
    }
}
