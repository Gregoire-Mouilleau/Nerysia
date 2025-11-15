package com.nerysia.plugin.game.focus;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Gère la duplication et suppression des maps Focus
 */
public class FocusMapManager {
    
    private static final String MAPS_FOLDER = "maps";
    private static final String SPAWN_FOLDER = "spawn";
    private static final String DUPLICATED_MAPS_FOLDER = "maps_dupli";
    private static final String TEMPLATE_MAP = "Focus";
    private static final String SPAWN_MINIJEUX_TEMPLATE = "spawn_minijeux";
    
    /**
     * Duplique la map Focus template pour une partie
     * @param gameId ID de la partie
     * @return Le monde créé, ou null si erreur
     */
    public World duplicateMap(String gameId) {
        try {
            File serverFolder = Bukkit.getWorldContainer();
            File mapsFolder = new File(serverFolder, MAPS_FOLDER);
            File templateFolder = new File(mapsFolder, TEMPLATE_MAP);
            
            // Vérifier que le template existe
            if (!templateFolder.exists() || !templateFolder.isDirectory()) {
                Bukkit.getLogger().severe("[FocusMap] Le dossier template maps/Focus n'existe pas !");
                return null;
            }
            
            // Créer le dossier maps_dupli s'il n'existe pas
            File duplicatedMapsFolder = new File(serverFolder, DUPLICATED_MAPS_FOLDER);
            if (!duplicatedMapsFolder.exists()) {
                duplicatedMapsFolder.mkdirs();
            }
            
            // Créer le nom du monde (ID de la partie)
            String worldName = gameId;
            File worldFolder = new File(duplicatedMapsFolder, worldName);
            
            // Vérifier que le monde n'existe pas déjà
            if (worldFolder.exists()) {
                Bukkit.getLogger().warning("[FocusMap] Le monde " + worldName + " existe déjà, suppression...");
                deleteWorld(worldName);
            }
            
            // Copier le dossier
            Bukkit.getLogger().info("[FocusMap] Copie de la map Focus vers " + worldName + "...");
            copyDirectory(templateFolder.toPath(), worldFolder.toPath());
            
            // Supprimer le fichier uid.dat pour éviter les conflits
            File uidFile = new File(worldFolder, "uid.dat");
            if (uidFile.exists()) {
                uidFile.delete();
            }
            
            // Charger le monde
            Bukkit.getLogger().info("[FocusMap] Chargement du monde " + worldName + "...");
            // Utiliser le chemin relatif depuis maps_dupli
            String worldPath = DUPLICATED_MAPS_FOLDER + "/" + worldName;
            WorldCreator creator = new WorldCreator(worldPath);
            World world = Bukkit.createWorld(creator);
            
            if (world != null) {
                // Configurer le monde pour le Focus
                world.setAutoSave(false);
                world.setKeepSpawnInMemory(true);
                world.setPVP(true);
                world.setTime(6000); // Midi
                world.setGameRuleValue("doDaylightCycle", "false"); // Eternal day
                world.setGameRuleValue("doMobSpawning", "false"); // Pas de mobs
                world.setGameRuleValue("doWeatherCycle", "false"); // Pas de météo
                world.setGameRuleValue("naturalRegeneration", "false"); // Pas de régén naturelle
                world.setGameRuleValue("keepInventory", "true"); // Garder l'inventaire à la mort
                world.setStorm(false);
                world.setThundering(false);
                world.setWeatherDuration(999999);
                Bukkit.getLogger().info("[FocusMap] Monde " + worldName + " créé et configuré avec succès !");
            } else {
                Bukkit.getLogger().severe("[FocusMap] Échec du chargement du monde " + worldName);
            }
            
            return world;
            
        } catch (IOException e) {
            Bukkit.getLogger().severe("[FocusMap] Erreur lors de la copie de la map : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Duplique la map spawn_minijeux pour une partie
     * @param gameId ID de la partie
     * @return Le monde créé, ou null si erreur
     */
    public World duplicateSpawnMinijeux(String gameId) {
        try {
            File serverFolder = Bukkit.getWorldContainer();
            File spawnFolder = new File(serverFolder, SPAWN_FOLDER);
            File templateFolder = new File(spawnFolder, SPAWN_MINIJEUX_TEMPLATE);
            
            // Vérifier que le template existe
            if (!templateFolder.exists() || !templateFolder.isDirectory()) {
                Bukkit.getLogger().severe("[FocusMap] Le dossier template spawn/spawn_minijeux n'existe pas !");
                return null;
            }
            
            // Créer le dossier maps_dupli s'il n'existe pas
            File duplicatedMapsFolder = new File(serverFolder, DUPLICATED_MAPS_FOLDER);
            if (!duplicatedMapsFolder.exists()) {
                duplicatedMapsFolder.mkdirs();
            }
            
            // Créer le nom du monde (ID de la partie + suffix)
            String worldName = gameId + "_spawn";
            File worldFolder = new File(duplicatedMapsFolder, worldName);
            
            // Vérifier que le monde n'existe pas déjà
            if (worldFolder.exists()) {
                Bukkit.getLogger().warning("[FocusMap] Le monde " + worldName + " existe déjà, suppression...");
                deleteWorld(worldName);
            }
            
            // Copier le dossier
            Bukkit.getLogger().info("[FocusMap] Copie de spawn_minijeux vers " + worldName + "...");
            copyDirectory(templateFolder.toPath(), worldFolder.toPath());
            
            // Supprimer le fichier uid.dat pour éviter les conflits
            File uidFile = new File(worldFolder, "uid.dat");
            if (uidFile.exists()) {
                uidFile.delete();
            }
            
            // Charger le monde
            Bukkit.getLogger().info("[FocusMap] Chargement du monde " + worldName + "...");
            // Utiliser le chemin relatif depuis maps_dupli
            String worldPath = DUPLICATED_MAPS_FOLDER + "/" + worldName;
            WorldCreator creator = new WorldCreator(worldPath);
            World world = Bukkit.createWorld(creator);
            
            if (world != null) {
                // Configurer le monde spawn
                world.setAutoSave(false);
                world.setKeepSpawnInMemory(true);
                world.setPVP(false);
                world.setTime(6000); // Midi
                world.setGameRuleValue("doDaylightCycle", "false");
                world.setGameRuleValue("doMobSpawning", "false");
                world.setGameRuleValue("doWeatherCycle", "false");
                world.setGameRuleValue("keepInventory", "true");
                world.setStorm(false);
                world.setThundering(false);
                world.setWeatherDuration(999999);
                Bukkit.getLogger().info("[FocusMap] Monde spawn " + worldName + " créé et configuré avec succès !");
            } else {
                Bukkit.getLogger().severe("[FocusMap] Échec du chargement du monde spawn " + worldName);
            }
            
            return world;
            
        } catch (IOException e) {
            Bukkit.getLogger().severe("[FocusMap] Erreur lors de la copie du spawn : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Supprime un monde et son dossier
     * @param worldName Nom du monde
     */
    public void deleteWorld(String worldName) {
        try {
            World world = Bukkit.getWorld(worldName);
            
            // Décharger le monde s'il est chargé
            if (world != null) {
                Bukkit.getLogger().info("[FocusMap] Déchargement du monde " + worldName + "...");
                
                // Téléporter tous les joueurs hors du monde
                world.getPlayers().forEach(player -> {
                    World lobby = Bukkit.getWorld("Lobby");
                    if (lobby != null) {
                        player.teleport(lobby.getSpawnLocation());
                    }
                });
                
                // Décharger le monde
                Bukkit.unloadWorld(world, false);
            }
            
            // Supprimer le dossier
            File serverFolder = Bukkit.getWorldContainer();
            File duplicatedMapsFolder = new File(serverFolder, DUPLICATED_MAPS_FOLDER);
            File worldFolder = new File(duplicatedMapsFolder, worldName);
            
            if (worldFolder.exists()) {
                Bukkit.getLogger().info("[FocusMap] Suppression du dossier " + worldName + "...");
                deleteDirectory(worldFolder.toPath());
                Bukkit.getLogger().info("[FocusMap] Monde " + worldName + " supprimé avec succès !");
            }
            
        } catch (IOException e) {
            Bukkit.getLogger().severe("[FocusMap] Erreur lors de la suppression du monde : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Copie récursive d'un dossier
     */
    private void copyDirectory(Path source, Path target) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                // Ignorer le dossier "playerdata" et "stats"
                String dirName = dir.getFileName().toString();
                if (dirName.equals("playerdata") || dirName.equals("stats")) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                
                Path targetDir = target.resolve(source.relativize(dir));
                Files.createDirectories(targetDir);
                return FileVisitResult.CONTINUE;
            }
            
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                // Ignorer certains fichiers
                String fileName = file.getFileName().toString();
                if (fileName.equals("uid.dat") || fileName.equals("session.lock")) {
                    return FileVisitResult.CONTINUE;
                }
                
                Path targetFile = target.resolve(source.relativize(file));
                Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }
    
    /**
     * Suppression récursive d'un dossier
     */
    private void deleteDirectory(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }
        
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
            
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
