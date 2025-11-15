package com.nerysia.plugin.game.commands;

import com.nerysia.plugin.Nerysia;
import com.nerysia.plugin.game.gui.ArenaMenuGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArenaCommand implements CommandExecutor {
    
    private final Nerysia plugin;
    
    public ArenaCommand(Nerysia plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cCette commande ne peut être exécutée que par un joueur !");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Si aucun argument, ouvrir le menu principal
        if (args.length == 0) {
            ArenaMenuGUI gui = new ArenaMenuGUI(plugin);
            gui.open(player);
            return true;
        }
        
        // Sous-commandes
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "join":
                // TODO: Implémenter la logique pour rejoindre une partie
                player.sendMessage("§a[Arena] §7Recherche d'une partie en cours...");
                player.sendMessage("§c[Arena] §7Cette fonctionnalité sera bientôt disponible !");
                break;
                
            case "list":
                // TODO: Implémenter la liste des parties
                player.sendMessage("§b[Arena] §7=== Parties en cours ===");
                player.sendMessage("§7Aucune partie en cours pour le moment.");
                break;
                
            case "stats":
                // TODO: Implémenter les statistiques
                player.sendMessage("§6[Arena] §7=== Vos Statistiques ===");
                player.sendMessage("§7Victoires: §e0");
                player.sendMessage("§7Défaites: §e0");
                player.sendMessage("§7Kills: §e0");
                player.sendMessage("§7Morts: §e0");
                player.sendMessage("§7K/D Ratio: §e0.00");
                break;
                
            case "create":
                // TODO: Implémenter la création de partie
                player.sendMessage("§a[Arena] §7Création d'une nouvelle partie...");
                player.sendMessage("§c[Arena] §7Cette fonctionnalité sera bientôt disponible !");
                break;
                
            case "leave":
                // TODO: Implémenter le départ d'une partie
                player.sendMessage("§c[Arena] §7Vous n'êtes pas dans une partie !");
                break;
                
            default:
                player.sendMessage("§c[Arena] §7Sous-commande inconnue !");
                player.sendMessage("§7Usage: §e/arena [join|list|stats|create|leave]");
                break;
        }
        
        return true;
    }
}
