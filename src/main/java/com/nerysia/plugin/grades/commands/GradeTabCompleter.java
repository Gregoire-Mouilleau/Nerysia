package com.nerysia.plugin.grades.commands;

import com.nerysia.plugin.grades.Grade;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GradeTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Autocomplétion des noms de joueurs
            String partial = args[0].toLowerCase();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(partial)) {
                    completions.add(player.getName());
                }
            }
        } else if (args.length == 2) {
            // Autocomplétion des grades
            String partial = args[1].toLowerCase();
            
            // Ajouter les noms de grades
            for (Grade grade : Grade.values()) {
                String gradeName = grade.getName();
                if (gradeName.toLowerCase().startsWith(partial)) {
                    completions.add(gradeName);
                }
            }
        }

        return completions;
    }
}
