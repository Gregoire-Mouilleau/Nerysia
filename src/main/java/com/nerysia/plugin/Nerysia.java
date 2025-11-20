package com.nerysia.plugin;

import com.nerysia.plugin.core.scoreboard.ScoreboardTask;
import com.nerysia.plugin.game.GameManager;
import com.nerysia.plugin.game.GameMode;
import com.nerysia.plugin.game.focus.*;
import com.nerysia.plugin.game.focus.commands.FocusReadyCommand;
import com.nerysia.plugin.game.focus.commands.FocusShopCommand;
import com.nerysia.plugin.game.focus.gui.FocusGameListGUI;
import com.nerysia.plugin.game.focus.gui.FocusPlayerManagementGUI;
import com.nerysia.plugin.game.focus.gui.FocusSettingsGUI;
import com.nerysia.plugin.game.focus.listeners.FocusGameplayListener;
import com.nerysia.plugin.game.focus.listeners.FocusLobbyItemListener;
import com.nerysia.plugin.game.focus.listeners.FocusPlayerQuitListener;
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
import com.nerysia.plugin.lobby.listeners.LobbyQuitListener;
import com.nerysia.plugin.lobby.listeners.LobbyWeatherListener;
import com.nerysia.plugin.minigames.listeners.MinigamesLobbyBlockListener;
import com.nerysia.plugin.minigames.listeners.MinigamesLobbyInventoryListener;
import com.nerysia.plugin.minigames.listeners.MinigamesLobbyJoinHandler;
import com.nerysia.plugin.minigames.listeners.MinigamesLobbyProtectionListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public class Nerysia extends JavaPlugin {

    private static Nerysia instance;
    
    private ScoreboardTask scoreboardTask;
    private GradeManager gradeManager;
    private PlayerVisibilityManager playerVisibilityManager;
    private GameManager gameManager;
    private NPCManager npcManager;
    
    // Focus system
    private FocusPointsManager focusPointsManager;
    private FocusShopData focusShopData;
    private FocusShopGUI focusShopGUI;
    private FocusGameManager focusGameManager;
    private FocusGameListGUI focusGameListGUI;
    private FocusSettingsGUI focusSettingsGUI;
    private FocusPlayerManagementGUI focusPlayerManagementGUI;

    public static Nerysia getInstance() {
        return instance;
    }
    
    @Override
    public void onEnable() {
        instance = this;
        // Initialiser le système de grades
        gradeManager = new GradeManager(this);
        
        // Initialiser le système de visibilité des joueurs
        playerVisibilityManager = new PlayerVisibilityManager(this);
        
        // Initialiser le système de jeu
        gameManager = new GameManager(this);
        npcManager = new NPCManager();
        
        // Initialiser le système Focus
        focusPointsManager = new FocusPointsManager();
        focusShopData = new FocusShopData();
        focusShopGUI = new FocusShopGUI(focusShopData, focusPointsManager);
        focusGameManager = new FocusGameManager(focusPointsManager, focusShopData, focusShopGUI);
        focusGameListGUI = new FocusGameListGUI(this, focusGameManager);
        focusSettingsGUI = new FocusSettingsGUI(this, focusGameManager);
        focusPlayerManagementGUI = new FocusPlayerManagementGUI(this, focusGameManager);
        
        // Enregistrement des commandes
        getCommand("grade").setExecutor(new GradeCommand(gradeManager, this));
        getCommand("grade").setTabCompleter(new GradeTabCompleter());
        getCommand("lobby").setExecutor(new LobbyCommand(this));
        getCommand("ready").setExecutor(new FocusReadyCommand(this));
        getCommand("fshop").setExecutor(new FocusShopCommand(this));
        
        // Enregistrement des listeners de grades
        getServer().getPluginManager().registerEvents(new GradeDisplayListener(this), this);
        
        // Enregistrement des listeners du Lobby
        getServer().getPluginManager().registerEvents(new LobbyJoinHandler(this), this);
        getServer().getPluginManager().registerEvents(new LobbyItemInteractHandler(this), this);
        getServer().getPluginManager().registerEvents(new LobbyBlockListener(), this);
        getServer().getPluginManager().registerEvents(new LobbyInventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new LobbyProtectionListener(), this);
        getServer().getPluginManager().registerEvents(new LobbyWeatherListener(), this);
        getServer().getPluginManager().registerEvents(new LobbyQuitListener(), this);
        
        // Enregistrement des listeners du spawn_minijeux (protections similaires au lobby)
        getServer().getPluginManager().registerEvents(new MinigamesLobbyJoinHandler(), this);
        getServer().getPluginManager().registerEvents(new MinigamesLobbyBlockListener(), this);
        getServer().getPluginManager().registerEvents(new MinigamesLobbyInventoryListener(), this);
        getServer().getPluginManager().registerEvents(new MinigamesLobbyProtectionListener(), this);
        
        // Enregistrement des listeners de jeu
        getServer().getPluginManager().registerEvents(new NPCInteractListener(this, npcManager), this);
        getServer().getPluginManager().registerEvents(new GameGUIListener(this), this);
        
        // Enregistrement des listeners Focus
        getServer().getPluginManager().registerEvents(focusGameListGUI, this);
        getServer().getPluginManager().registerEvents(focusSettingsGUI, this);
        getServer().getPluginManager().registerEvents(focusPlayerManagementGUI, this);
        getServer().getPluginManager().registerEvents(focusShopGUI, this);
        getServer().getPluginManager().registerEvents(new FocusLobbyItemListener(this, focusGameManager, focusSettingsGUI, focusPlayerManagementGUI), this);
        getServer().getPluginManager().registerEvents(new FocusPlayerQuitListener(this, focusGameManager), this);
        
        // Enregistrer le listener des items et le stocker dans le manager
        com.nerysia.plugin.game.focus.listeners.FocusItemsListener itemsListener = 
            new com.nerysia.plugin.game.focus.listeners.FocusItemsListener(focusGameManager, focusShopData);
        getServer().getPluginManager().registerEvents(itemsListener, this);
        FocusGameManager.setItemsListener(itemsListener);
        
        // Enregistrer le gameplay listener avec la référence au items listener pour les kills de mines
        getServer().getPluginManager().registerEvents(new FocusGameplayListener(focusGameManager, itemsListener), this);
        getServer().getPluginManager().registerEvents(new com.nerysia.plugin.game.focus.listeners.FocusShopItemListener(focusGameManager, focusShopGUI), this);
        
        getServer().getPluginManager().registerEvents(new com.nerysia.plugin.game.focus.listeners.FocusWorldProtectionListener(focusGameManager), this);
        getServer().getPluginManager().registerEvents(new com.nerysia.plugin.game.focus.listeners.FocusLobbyLeaveListener(this, focusGameManager, focusSettingsGUI), this);
        
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
        
        // Position de base (selon ton screenshot)
        double baseX = -203.5;
        double baseY = 27.0;
        double baseZ = -178.0;
        
        // Texture de skin Teemo (League of Legends) pour LOL UHC
        // Récupéré depuis MineSkin.org - Skin Teemo avec signature valide
        String lolUhcTexture = "ewogICJ0aW1lc3RhbXAiIDogMTc0ODY3NDgwMTExNywKICAicHJvZmlsZUlkIiA6ICI1MDUzYTk3YTdiM2E0MTE5YTRkNjdmMDExMGIzYTZiZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJhbXhraWZpciIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS85ZjJmOTRkN2U0NmRjZWQ1M2ViZGNlYWMwYmE1MjExYmU0MzlhMDViN2NlNWU2ODQ5ZTlhN2YyZWQ1ZTdhZDkxIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=";
        String lolUhcSignature = "T7pj8qAUXaTVS1vlqYP3scoW5vGT+5eHWEEMc/dhMJEqH8rFfApaQz38E7dwT/jrapTWaSOKgQoLojD7k7n013QfXIq3TA/ryIB6JtzeoVDyI2mZE2vL+wuo874fo/pA//Pgw39EfoNKh0uyIz8e/eBdFH+JoT9xarGlKJWKaINixM7Dc8ZGrPMAdN5+BM+eN9t4HREMthHuic2Zpy1MbMqeFOOhf/16gkzhCEFPn/BpEkgWi+T8Cyd2KtquujxTilxfYeYSvfrV5++fLTkRoAIS8jvC8AjExkMZlULkS0l2qR42yowa5ltkLrUqvUlABGF1R6MHvXXralZkEY4mghpVwhg1J8ktLVVhSsj3VcXNG1xU29W72aqFEC8726KvSb1y4Jvdb/1+y2vOOxCN47lcWHwFEnmp6ZJ8FKdVwvmY762sNbZEL4eZ9ncWhhd7997wz5CyHfxob7oIJvPHJB0BaKf2UxNOCXyJ92cA8pLbbj+sxlLe3rCIYqp/cx/WJ6EnDZV5obQ3+n+z9KUWZHpUHqPjtcm4SaCG+qMd2ryIYm1f2bNeTU5qc2YCX17Sw+DOervEbpaboFYrKNnpGQIC7JlXraSHbeYJ4QmeQGvPG0QJYwIb3gpy96C67KRBGIC11x6/Wvr2n0Yau1m/0RllirbLuJ3sA6e5/XWJjow=";
        
        // Texture de skin Arena (guerrier)
        String arenaTexture = "ewogICJ0aW1lc3RhbXAiIDogMTcyNjkyMzk1Nzg5MiwKICAicHJvZmlsZUlkIiA6ICJlMTNhY2EzZWRlMDQ0YmI5YmY4NGNmMmIyNTM0ZGFjNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJNckxhZ1N3aXRjaGEiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmM4YzI2MTNhMzBmODk3YjEyOGFkMmZiZTE0YjdkN2RhZmY0MDhiNDA2ZmJmOWRhNjI1NDU0ZmJlMTdkZjg5NyIKICAgIH0KICB9Cn0=";
        String arenaSignature = "rzCuuxRRx633Go25XFGxY7Fx3IXqNtdW8KwZPOF0nSUc0Kz9vOXeL0uUais5EVnNRgpLXqWEOleWkBPJX4Iz47Yh29KJ3G+RLE6GQa88wW3mfKHOLS8Yd2kpqFdtD5VisUGmcW8mB2Jepo4AzyEpyclogTiiV0IcPFGLz31m00k0uBleKArsD0TAy7q9FxTiI+Ssz7Pr5jCuUcXukLXeZ3uqbbkD8b1967baM1iK4j/pzFYC028fuTfot039jdfY5HV/s3Gs67+vYbIdeKrM8c1AOdHtKPa3SuBvi8E60UhgfxJj6MI91TOaqA22AnI/WAKoEIaFvvPhg8w6wfI+FNVtuotEe2SGWz3vnBb8XhAWmAPAyHPdOxJ2HmfBDLedhWnS1gSGy4DI3CziI6zYUpyHg99oJGUuH9OMGiRvA9X2XlgomyQxjKOBjWxj6PIkRnoUNx0FUsQE3slIVwfztTMEdWQIPgvz+Fz/K+doF0swf56tWi0KGd7cC6u9V4JUZIp/tVUOD70J3B35nOCuKI09d8KsVHJCk178O6cD0qavfRs/NuVNScoZep+lDg6SEO8lf5tTdntnjgFYo2iU80cZlWMuQoALiPIUnV/axVjJcsacWCDZNQvZ5LV7SuyvAGyookBj7zp9oGy6zAQvnq05Pme1hEBDRIS4nibryag=";
        
        // Texture de skin Fallen Kingdoms (roi)
        String fkTexture = "ewogICJ0aW1lc3RhbXAiIDogMTY1NzM2NjExMjM5NSwKICAicHJvZmlsZUlkIiA6ICI5MWYwNGZlOTBmMzY0M2I1OGYyMGUzMzc1Zjg2ZDM5ZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJTdG9ybVN0b3JteSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS81OWMwOGI2MjM1NmE4NjdiNzc4ODZkODQ4NWJlYWM0OTU0NjhhNWQ3ZDUyZTI5NzdlZTIxOGQ3ZmVlODlkMDAiCiAgICB9CiAgfQp9";
        String fkSignature = "YW3SlzfuhuaL8tUQ2pqQBvDpedU81q4Q7E6bfyOjpsaSj4O2rEPdoN+voCA8nTLfheh+FNgLzcR9Qg6lgm1aE4NsGCfcM0htr6uGDnfkcv/ggm4HOMvWebabE+Yz7Iny8GK7JzdsoKnTIrFv0hhIWt0zR6t6zUlu8yWRDCQZ5oe0d3lNLX7GKcKZeWM2NGIc4dp3jttuplPsC9jq2HeqiuIMFU2E+qnyGi1M/iIiK4c1xAwCnGfo05PNZKTAsXmKWZAcvm8QbrpJEdQF2xv4PY7AUm0lkZmyx8qYb7+bGiZAGDi1H5PcyPJwA9ESW+ewDi2PN08VZMk6Rmi8CkpsNxwSYsSb8/A9Q12cGM167cZJpf/Ut+HUQTvKnE/AM4hLzGmMDOK8lTkLRiK6lNIxFhLaP4/WqENlfiDkYL8bP91i6lXhMJeImfr8VwLLylej3H2G4TOemtgsweBiERHw51nxEiWrVytyYBjE0HeBw2DxiRe8Qs7M8bz3ElBio6yltYMRjkR6W8R22Xjh+3DhLTg+QHVjpUdCqdcJcdFIiLicvS7tCBJJpUYpFU8AKhh6Tjsgm/vb8hal1IftvLigasLgxEehuP1oBpdwvXAQDV/VBx0moC7pHsP7YRn4N51SlSBDbHYCWXeHu/7+ag5b8WgVgBnzgXsWr3U41SsmkYA=";
        
        // Texture de skin par défaut (Steve) pour les autres
        String defaultTexture = "eyJ0aW1lc3RhbXAiOjE0MTEyNjg3OTI3NjUsInByb2ZpbGVJZCI6IjNmYmVjN2RkMGE1ZjQwYmY5ZDExODg1YTU0NTA3MTEyIiwicHJvZmlsZU5hbWUiOiJsYXN0X3VzZXJuYW1lIiwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzYwYjVlYzkxZGQ2M2I1NWM5MzE4ZGI4YmViNmI4ZDY5ZmQxMzQ1MmU0YzViOWUzZGExNGQ0NjZkY2U5NTgwIn19fQ==";
        String defaultSignature = null;
        
        getLogger().info("[SPAWN-NPC] Création du NPC LOL UHC...");
        // Spawner le PNJ LOL UHC
        Location lolUhcLoc = new Location(lobby, -80, 46.5, -229.0, 90, 0);
        npcManager.createNPC(lolUhcLoc, GameMode.LOL_UHC, "§c§lLOL UHC", lolUhcTexture, lolUhcSignature);
    
        Location arenaLoc = new Location(lobby, -80, 46.5, -224, 90, 0);
        npcManager.createNPC(arenaLoc, GameMode.ARENA, "§4§lArena", arenaTexture, arenaSignature);
        
        // Spawner le NPC Focus (à la position indiquée, regardant vers l'est)
        Location focusLoc = new Location(lobby, -85, 46.5, -234, 90, 0);
        npcManager.createNPC(focusLoc, GameMode.FOCUS, "§e§lFocus", defaultTexture, defaultSignature);
        
        // Spawner le NPC Fallen Kingdoms avec le skin de roi (regardant vers l'ouest)
        Location fkLoc = new Location(lobby, -81.1, 46.5, -221.0, 90, 0);
        npcManager.createNPC(fkLoc, GameMode.FALLEN_KINGDOMS, "§d§lFK", fkTexture, fkSignature);
        
        // Spawner le NPC The Tower (regardant vers le nord-ouest)
        Location towerLoc = new Location(lobby, -90, 46.5, -219, 90, 0);
        npcManager.createNPC(towerLoc, GameMode.THE_TOWER, "§6§lThe Tower", defaultTexture, defaultSignature);
        
        // Spawner le NPC Sky Defender (regardant vers le sud-ouest)
        Location skyDefLoc = new Location(lobby, -82, 46.5, -233, 90, 0);
        npcManager.createNPC(skyDefLoc, GameMode.SKY_DEFENDER, "§b§lSky Def", defaultTexture, defaultSignature);
        
        // Spawner le NPC Ball of Steels (regardant vers le sud-est)
        Location ballLoc = new Location(lobby, -90, 46.5, -234, 90, 0);
        npcManager.createNPC(ballLoc, GameMode.BALL_OF_STEELS, "§7§lBall Steel", defaultTexture, defaultSignature);
        
        // Spawner le NPC Rush (regardant vers le nord-est)
        Location rushLoc = new Location(lobby, -85, 46.5, -219, 90, 0);
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
    
    public FocusGameManager getFocusGameManager() {
        return focusGameManager;
    }
    
    public FocusGameListGUI getFocusGameListGUI() {
        return focusGameListGUI;
    }
    
    public FocusSettingsGUI getFocusSettingsGUI() {
        return focusSettingsGUI;
    }
    
    public FocusPlayerManagementGUI getFocusPlayerManagementGUI() {
        return focusPlayerManagementGUI;
    }
    
    public FocusShopGUI getFocusShopGUI() {
        return focusShopGUI;
    }
    
    public FocusPointsManager getFocusPointsManager() {
        return focusPointsManager;
    }
    
    public FocusShopData getFocusShopData() {
        return focusShopData;
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
