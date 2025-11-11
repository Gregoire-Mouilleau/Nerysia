package com.nerysia.plugin.game.npc;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;

public class NPCProtectionListener implements Listener {
    
    private final NPCManager npcManager;
    
    public NPCProtectionListener(NPCManager npcManager) {
        this.npcManager = npcManager;
    }
    
    /**
     * Empêcher les dégâts sur les PNJ
     */
    @EventHandler
    public void onNPCDamage(EntityDamageEvent event) {
        if (event.getEntityType() == EntityType.VILLAGER) {
            if (npcManager.isGameNPC(event.getEntity())) {
                event.setCancelled(true);
            }
        }
    }
    
    /**
     * Empêcher les dégâts par entité sur les PNJ
     */
    @EventHandler
    public void onNPCDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntityType() == EntityType.VILLAGER) {
            if (npcManager.isGameNPC(event.getEntity())) {
                event.setCancelled(true);
            }
        }
    }
    
    /**
     * Empêcher les PNJ de cibler des entités
     */
    @EventHandler
    public void onNPCTarget(EntityTargetEvent event) {
        if (event.getEntityType() == EntityType.VILLAGER) {
            if (npcManager.isGameNPC(event.getEntity())) {
                event.setCancelled(true);
            }
        }
    }
    
    /**
     * Empêcher de monter sur les PNJ
     */
    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent event) {
        Entity vehicle = event.getVehicle();
        if (vehicle.getType() == EntityType.VILLAGER) {
            if (npcManager.isGameNPC(vehicle)) {
                event.setCancelled(true);
            }
        }
    }
}
