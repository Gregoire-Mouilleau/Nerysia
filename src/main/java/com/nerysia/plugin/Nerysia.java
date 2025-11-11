package com.nerysia.plugin;

import com.nerysia.plugin.core.scoreboard.ScoreboardTask;
import com.nerysia.plugin.game.GameManager;
import com.nerysia.plugin.game.GameMode;
import com.nerysia.plugin.game.gui.GameGUIListener;
import com.nerysia.plugin.game.npc.NPCInteractListener;
import com.nerysia.plugin.game.npc.NPCManager;
import com.nerysia.plugin.grades.GradeManager;
import com.nerysia.plugin.grades.commands.GradeCommand;
import com.nerysia.plugin.grades.commands.GradeTabCompleter;
import com.nerysia.plugin.grades.listeners.GradeDisplayListener;
import com.nerysia.plugin.lobby.commands.LobbyCommand;
import com.nerysia.plugin.lobby.handlers.LobbyItemInteractHandler;
import com.nerysia.plugin.lobby.handlers.LobbyJoinHandler;
import com.nerysia.plugin.lobby.handlers.PlayerVisibilityManager;
import com.nerysia.plugin.lobby.listeners.LobbyBlockListener;
import com.nerysia.plugin.lobby.listeners.LobbyInventoryListener;
import com.nerysia.plugin.lobby.listeners.LobbyProtectionListener;
import com.nerysia.plugin.lobby.listeners.LobbyWeatherListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public class Nerysia extends JavaPlugin {

    private ScoreboardTask scoreboardTask;
    private GradeManager gradeManager;
    private PlayerVisibilityManager playerVisibilityManager;
    private GameManager gameManager;
    private NPCManager npcManager;

    @Override
    public void onEnable() {
        // Initialiser le système de grades
        gradeManager = new GradeManager(this);
        
        // Initialiser le système de visibilité des joueurs
        playerVisibilityManager = new PlayerVisibilityManager(this);
        
        // Initialiser le système de jeu
        gameManager = new GameManager(this);
        npcManager = new NPCManager();
        
        // Enregistrement des commandes
        getCommand("grade").setExecutor(new GradeCommand(gradeManager, this));
        getCommand("grade").setTabCompleter(new GradeTabCompleter());
        getCommand("lobby").setExecutor(new LobbyCommand());
        
        // Enregistrement des listeners de grades
        getServer().getPluginManager().registerEvents(new GradeDisplayListener(this), this);
        
        // Enregistrement des listeners du Lobby
        getServer().getPluginManager().registerEvents(new LobbyJoinHandler(this), this);
        getServer().getPluginManager().registerEvents(new LobbyItemInteractHandler(this), this);
        getServer().getPluginManager().registerEvents(new LobbyBlockListener(), this);
        getServer().getPluginManager().registerEvents(new LobbyInventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new LobbyProtectionListener(), this);
        getServer().getPluginManager().registerEvents(new LobbyWeatherListener(), this);
        
        // Enregistrement des listeners de jeu
        getServer().getPluginManager().registerEvents(new NPCInteractListener(this, npcManager), this);
        getServer().getPluginManager().registerEvents(new GameGUIListener(this), this);
        
        // Démarrer la tâche de scoreboard
        scoreboardTask = new ScoreboardTask(this);
        scoreboardTask.start();
        
        getLogger().info("[INIT] Planification du spawn des NPCs dans 2 secondes...");
        
        // Spawner les PNJ de jeu après un délai
        Bukkit.getScheduler().runTaskLater(this, new Runnable() {
            @Override
            public void run() {
                try {
                    getLogger().info("[INIT] Exécution du spawn des NPCs maintenant...");
                    spawnGameNPCs();
                    getLogger().info("[INIT] ✓ Spawn des NPCs terminé avec succès !");
                } catch (Exception e) {
                    getLogger().severe("[INIT] ❌ ERREUR lors du spawn des NPCs:");
                    e.printStackTrace();
                }
            }
        }, 40L); // 2 secondes de délai
        
        getLogger().info("Plugin Nérysia activé avec succès !");
    }
    
    private void spawnGameNPCs() {
        getLogger().info("========================================");
        getLogger().info("[SPAWN-NPC] Début du spawn des NPCs...");
        getLogger().info("[SPAWN-NPC] Recherche du monde 'Lobby'...");
        
        World lobby = Bukkit.getWorld("Lobby");
        if (lobby == null) {
            getLogger().severe("[SPAWN-NPC] ❌ Le monde Lobby n'existe pas !");
            getLogger().severe("[SPAWN-NPC] Mondes disponibles: " + Bukkit.getWorlds());
            return;
        }
        
        getLogger().info("[SPAWN-NPC] ✓ Monde Lobby trouvé !");
        
        // Supprimer tous les anciens villageois du lobby pour éviter les duplications
        npcManager.removeAllLobbyVillagers();
        getLogger().info("[SPAWN-NPC] Anciens villageois supprimés !");
        
        // Position de base (selon ton screenshot)
        double baseX = -203.5;
        double baseY = 27.0;
        double baseZ = -178.0;
        
        // Texture de skin KephasAnthropos pour LOL UHC
        String lolUhcTexture = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzk2N2QxOTkyMWJlZDNkYWQxMzU5YmNjZmNiZDQxOTVlOGExOWYwYTU1NTkyZDFhOGU3OTU5MDI4Njk3YyJ9fX0=";
        String lolUhcSignature = "";
        
        // Texture de skin par défaut (Steve) pour les autres
        String defaultTexture = "eyJ0aW1lc3RhbXAiOjE0MTEyNjg3OTI3NjUsInByb2ZpbGVJZCI6IjNmYmVjN2RkMGE1ZjQwYmY5ZDExODg1YTU0NTA3MTEyIiwicHJvZmlsZU5hbWUiOiJsYXN0X3VzZXJuYW1lIiwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzYwYjVlYzkxZGQ2M2I1NWM5MzE4ZGI4YmViNmI4ZDY5ZmQxMzQ1MmU0YzViOWUzZGExNGQ0NjZkY2U5NTgwIn19fQ==";
        String defaultSignature = "";
        
        getLogger().info("[SPAWN-NPC] Création du NPC LOL UHC...");
        // Spawner le PNJ LOL UHC avec la texture par défaut pour tester
        Location lolUhcLoc = new Location(lobby, baseX, baseY, baseZ, 180, 0);
        npcManager.createNPC(lolUhcLoc, GameMode.LOL_UHC, "§c§lLOL UHC", defaultTexture, defaultSignature);
        
        // Spawner le NPC Arena (regardant vers LOL UHC)
        Location arenaLoc = new Location(lobby, -203.5, 27.0, -205.4, 0, 0);
        npcManager.createNPC(arenaLoc, GameMode.ARENA, "§4§lArena", defaultTexture, defaultSignature);
        
        // Spawner le NPC Focus (à la position indiquée, regardant vers l'est)
        Location focusLoc = new Location(lobby, -217.9, 27.0, -191.5, -90, 0);
        npcManager.createNPC(focusLoc, GameMode.FOCUS, "§e§lFocus", defaultTexture, defaultSignature);
        
        // Spawner le NPC Fallen Kingdoms (regardant vers l'ouest)
        Location fkLoc = new Location(lobby, -189.6, 27.0, -191.5, 90, 0);
        npcManager.createNPC(fkLoc, GameMode.FALLEN_KINGDOMS, "§d§lFK", defaultTexture, defaultSignature);
        
        // Spawner le NPC The Tower (regardant vers le nord-ouest)
        Location towerLoc = new Location(lobby, -193.5, 27.0, -181.5, 135, 0);
        npcManager.createNPC(towerLoc, GameMode.THE_TOWER, "§6§lThe Tower", defaultTexture, defaultSignature);
        
        // Spawner le NPC Sky Defender (regardant vers le sud-ouest)
        Location skyDefLoc = new Location(lobby, -193.5, 27.0, -201.4, -45, 0);
        npcManager.createNPC(skyDefLoc, GameMode.SKY_DEFENDER, "§b§lSky Def", defaultTexture, defaultSignature);
        
        // Spawner le NPC Ball of Steels (regardant vers le sud-est)
        Location ballLoc = new Location(lobby, -213.5, 27.0, -201.5, -135, 0);
        npcManager.createNPC(ballLoc, GameMode.BALL_OF_STEELS, "§7§lBall Steel", defaultTexture, defaultSignature);
        
        // Spawner le NPC Rush (regardant vers le nord-est)
        Location rushLoc = new Location(lobby, -213.5, 27.0, -181.6, -135, 0);
        npcManager.createNPC(rushLoc, GameMode.RUSH, "§a§lRush", defaultTexture, defaultSignature);
        
        getLogger().info("PNJ de jeu spawnés avec succès !");
    }

    public ScoreboardTask getScoreboardTask() {
        return scoreboardTask;
    }

    public GradeManager getGradeManager() {
        return gradeManager;
    }

    public PlayerVisibilityManager getPlayerVisibilityManager() {
        return playerVisibilityManager;
    }
    
    public GameManager getGameManager() {
        return gameManager;
    }
    
    public NPCManager getNPCManager() {
        return npcManager;
    }

    @Override
    public void onDisable() {
        // Nettoyer tous les NPCs (supprimer du monde et de la mémoire)
        if (npcManager != null) {
            npcManager.cleanupAll();
            getLogger().info("Tous les NPCs ont été supprimés !");
        }
        
        // Sauvegarder les grades
        if (gradeManager != null) {
            gradeManager.saveGrades();
        }
        getLogger().info("NerysiaPlugin désactivé !");
    }
}
