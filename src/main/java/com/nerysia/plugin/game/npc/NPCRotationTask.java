package com.nerysia.plugin.game.npc;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

/**
 * Tâche qui empêche les NPCs de tourner la tête ou le corps
 */
public class NPCRotationTask extends BukkitRunnable {
    
    private final NPCManager npcManager;
    
    public NPCRotationTask(NPCManager npcManager) {
        this.npcManager = npcManager;
    }
    
    @Override
    public void run() {
        // TODO: Implémenter la rotation des NPCs quand nécessaire
        // Pour chaque NPC, réinitialiser sa rotation à sa position d'origine
        // for (GameNPC npc : npcManager.getAllNPCs().values()) {
        //     // La méthode getEntityUuid() n'existe pas encore dans GameNPC
        // }
    }
}
