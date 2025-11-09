package com.nerysia.plugin;

import com.nerysia.plugin.core.scoreboard.ScoreboardTask;
import com.nerysia.plugin.grades.GradeManager;
import com.nerysia.plugin.grades.commands.GradeCommand;
import com.nerysia.plugin.grades.commands.GradeTabCompleter;
import com.nerysia.plugin.grades.listeners.GradeDisplayListener;
import com.nerysia.plugin.lobby.handlers.LobbyItemInteractHandler;
import com.nerysia.plugin.lobby.handlers.LobbyJoinHandler;
import com.nerysia.plugin.lobby.listeners.LobbyBlockListener;
import com.nerysia.plugin.lobby.listeners.LobbyInventoryListener;
import com.nerysia.plugin.lobby.listeners.LobbyProtectionListener;
import com.nerysia.plugin.lobby.listeners.LobbyWeatherListener;
import org.bukkit.plugin.java.JavaPlugin;

public class Nerysia extends JavaPlugin {

    private ScoreboardTask scoreboardTask;
    private GradeManager gradeManager;

    @Override
    public void onEnable() {
        // Initialiser le système de grades
        gradeManager = new GradeManager(this);
        
        // Enregistrement des commandes
        getCommand("grade").setExecutor(new GradeCommand(gradeManager, this));
        getCommand("grade").setTabCompleter(new GradeTabCompleter());
        
        // Enregistrement des listeners de grades
        getServer().getPluginManager().registerEvents(new GradeDisplayListener(this), this);
        
        // Enregistrement des listeners du Lobby
        getServer().getPluginManager().registerEvents(new LobbyJoinHandler(this), this);
        getServer().getPluginManager().registerEvents(new LobbyItemInteractHandler(), this);
        getServer().getPluginManager().registerEvents(new LobbyBlockListener(), this);
        getServer().getPluginManager().registerEvents(new LobbyInventoryListener(), this);
        getServer().getPluginManager().registerEvents(new LobbyProtectionListener(), this);
        getServer().getPluginManager().registerEvents(new LobbyWeatherListener(), this);
        
        // Démarrer la tâche de scoreboard
        scoreboardTask = new ScoreboardTask(this);
        scoreboardTask.start();
        
        getLogger().info("Plugin Nérysia activé avec succès !");
    }

    public ScoreboardTask getScoreboardTask() {
        return scoreboardTask;
    }

    public GradeManager getGradeManager() {
        return gradeManager;
    }

    @Override
    public void onDisable() {
        // Sauvegarder les grades
        if (gradeManager != null) {
            gradeManager.saveGrades();
        }
        getLogger().info("NerysiaPlugin désactivé !");
    }
}
