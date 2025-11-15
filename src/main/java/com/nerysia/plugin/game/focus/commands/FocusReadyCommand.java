package com.nerysia.plugin.game.focus.commands;

import com.nerysia.plugin.Nerysia;
import com.nerysia.plugin.game.focus.FocusGame;
import com.nerysia.plugin.game.focus.FocusGameController;
import com.nerysia.plugin.game.focus.FocusGameManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Commande /ready pour se déclarer prêt à commencer un round
 */
public class FocusReadyCommand implements CommandExecutor {
    
    private final Nerysia plugin;
    
    public FocusReadyCommand(Nerysia plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Seuls les joueurs peuvent utiliser cette commande !");
            return true;
        }
        
        Player player = (Player) sender;
        FocusGameManager manager = plugin.getFocusGameManager();
        FocusGame game = manager.getPlayerGame(player.getUniqueId());
        
        if (game == null) {
            player.sendMessage(ChatColor.RED + "Vous n'êtes pas dans une partie Focus !");
            return true;
        }
        
        FocusGameController controller = manager.getGameController(game);
        if (controller == null) {
            player.sendMessage(ChatColor.RED + "Erreur: Contrôleur de jeu introuvable !");
            return true;
        }
        
        controller.toggleReady(player);
        return true;
    }
}
