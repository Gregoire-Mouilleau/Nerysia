package com.nerysia.plugin.grades;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GradeManager {

    private Plugin plugin;
    private File gradeFile;
    private FileConfiguration gradeConfig;
    private Map<UUID, Grade> playerGrades;

    public GradeManager(Plugin plugin) {
        this.plugin = plugin;
        this.playerGrades = new HashMap<>();
        loadGrades();
    }

    private void loadGrades() {
        gradeFile = new File(plugin.getDataFolder(), "grades.yml");
        
        if (!gradeFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                gradeFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        gradeConfig = YamlConfiguration.loadConfiguration(gradeFile);
        
        // Charger tous les grades depuis le fichier
        for (String key : gradeConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                String gradeName = gradeConfig.getString(key);
                Grade grade = Grade.getByName(gradeName);
                if (grade != null) {
                    playerGrades.put(uuid, grade);
                }
            } catch (IllegalArgumentException e) {
                // UUID invalide, ignorer
            }
        }
    }

    public void saveGrades() {
        for (Map.Entry<UUID, Grade> entry : playerGrades.entrySet()) {
            gradeConfig.set(entry.getKey().toString(), entry.getValue().name());
        }
        
        try {
            gradeConfig.save(gradeFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Grade getGrade(UUID uuid) {
        return playerGrades.getOrDefault(uuid, Grade.JOUEUR);
    }

    public boolean hasGradeSet(UUID uuid) {
        return playerGrades.containsKey(uuid);
    }

    public void setGrade(UUID uuid, Grade grade) {
        playerGrades.put(uuid, grade);
        saveGrades();
    }

    public void removeGrade(UUID uuid) {
        playerGrades.remove(uuid);
        gradeConfig.set(uuid.toString(), null);
        saveGrades();
    }

    public boolean hasGrade(UUID uuid, Grade grade) {
        return getGrade(uuid) == grade;
    }

    public boolean hasGradeOrHigher(UUID uuid, Grade minGrade) {
        Grade playerGrade = getGrade(uuid);
        return playerGrade.getPower() >= minGrade.getPower();
    }
}
