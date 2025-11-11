package com.nerysia.plugin.game.npc;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.nerysia.plugin.game.GameMode;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;

public class NPCManager {
    
    private final Map<String, GameNPC> npcs; // Clé = nom du NPC
    private final Map<Integer, String> npcIds; // Clé = Entity ID, Valeur = nom du NPC
    private final Map<String, EntityPlayer> npcEntities; // Stocker les EntityPlayer
    private final Map<String, GameProfile> npcProfiles; // Stocker les GameProfile
    
    public NPCManager() {
        this.npcs = new HashMap<>();
        this.npcIds = new HashMap<>();
        this.npcEntities = new HashMap<>();
        this.npcProfiles = new HashMap<>();
    }
    
    /**
     * Créer un NPC joueur pour un mode de jeu
     */
    public GameNPC createNPC(Location location, GameMode gameMode, String displayName, String skinTexture, String skinSignature) {
        // Créer un profil de jeu avec une texture de skin
        GameProfile profile = new GameProfile(UUID.randomUUID(), displayName);
        profile.getProperties().put("textures", new Property("textures", skinTexture, skinSignature));
        
        // Créer le NPC entity
        MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
        EntityPlayer npc = new EntityPlayer(nmsServer, nmsWorld, profile, new PlayerInteractManager(nmsWorld));
        
        // Positionner le NPC
        npc.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        
        int entityId = npc.getId();
        Bukkit.getLogger().info("[NPC-CREATION] NPC créé: " + displayName + " avec entityId: " + entityId + " pour mode: " + gameMode.name());
        
        // Créer l'objet GameNPC
        GameNPC gameNPC = new GameNPC(entityId, gameMode, displayName, location);
        
        // Stocker le NPC
        npcs.put(displayName, gameNPC);
        npcIds.put(entityId, displayName);
        npcEntities.put(displayName, npc);
        npcProfiles.put(displayName, profile);
        
        Bukkit.getLogger().info("[NPC-CREATION] NPC stocké dans npcIds. Total NPCs: " + npcIds.size());
        Bukkit.getLogger().info("[NPC-CREATION] Liste des entityIds: " + npcIds.keySet());
        
        // Envoyer les packets à tous les joueurs en ligne pour spawner le NPC
        spawnNPCForAllPlayers(npc, profile);
        
        return gameNPC;
    }
    
    /**
     * Spawner tous les NPC pour un joueur
     */
    public void spawnAllNPCsForPlayer(Player player) {
        Bukkit.getLogger().info("[NPC-SPAWN] Début du spawn de " + npcs.size() + " NPCs pour " + player.getName());
        for (String npcName : npcs.keySet()) {
            try {
                EntityPlayer npc = npcEntities.get(npcName);
                GameProfile profile = npcProfiles.get(npcName);
                if (npc != null && profile != null) {
                    Bukkit.getLogger().info("[NPC-SPAWN] Spawning NPC: " + npcName + " (EntityID: " + npc.getId() + ") at X:" + npc.locX + " Y:" + npc.locY + " Z:" + npc.locZ);
                    spawnNPCForPlayer(player, npc, profile);
                    Bukkit.getLogger().info("[NPC-SPAWN] ✓ NPC " + npcName + " spawné avec succès !");
                } else {
                    Bukkit.getLogger().warning("[NPC-SPAWN] ✗ NPC manquant: " + npcName + " (npc=" + npc + ", profile=" + profile + ")");
                }
            } catch (Exception e) {
                Bukkit.getLogger().severe("[NPC-SPAWN] ✗ ERREUR lors du spawn de " + npcName + " pour " + player.getName());
                e.printStackTrace();
            }
        }
        Bukkit.getLogger().info("[NPC-SPAWN] ✓ Spawn terminé pour " + player.getName());
    }
    
    /**
     * Spawner le NPC pour tous les joueurs
     */
    private void spawnNPCForAllPlayers(EntityPlayer npc, GameProfile profile) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            spawnNPCForPlayer(player, npc, profile);
        }
    }
    
    /**
     * Spawner le NPC pour un joueur spécifique
     */
    public void spawnNPCForPlayer(Player player, EntityPlayer npc, GameProfile profile) {
        try {
            if (player == null || !player.isOnline()) {
                Bukkit.getLogger().warning("[NPC-SPAWN] Impossible de spawner le NPC: joueur null ou hors ligne");
                return;
            }
            
            PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
            
            if (connection == null) {
                Bukkit.getLogger().warning("[NPC-SPAWN] PlayerConnection null pour " + player.getName());
                return;
            }
            
            // Packet pour ajouter le joueur à la tab list
            PacketPlayOutPlayerInfo addPlayer = new PacketPlayOutPlayerInfo(
                PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, npc);
            
            // Packet pour spawner l'entité
            PacketPlayOutNamedEntitySpawn spawnPacket = new PacketPlayOutNamedEntitySpawn(npc);
            
            // Packet pour la rotation de la tête
            PacketPlayOutEntityHeadRotation headRotation = new PacketPlayOutEntityHeadRotation(
                npc, (byte) ((npc.yaw * 256.0F) / 360.0F));
            
            // Packet pour retirer de la tab list (envoyé immédiatement)
            PacketPlayOutPlayerInfo removePlayer = new PacketPlayOutPlayerInfo(
                PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, npc);
            
            // Envoyer les packets dans l'ordre
            connection.sendPacket(addPlayer);
            connection.sendPacket(spawnPacket);
            connection.sendPacket(headRotation);
            connection.sendPacket(removePlayer); // Retrait immédiat de la tab list
            
        } catch (Exception e) {
            Bukkit.getLogger().severe("[NPC-SPAWN] Erreur lors du spawn du NPC pour " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Vérifier si un entity ID correspond à un NPC de jeu
     */
    public boolean isGameNPC(int entityId) {
        boolean result = npcIds.containsKey(entityId);
        Bukkit.getLogger().info("[NPC-CHECK] Vérification entityId: " + entityId + ", résultat: " + result);
        Bukkit.getLogger().info("[NPC-CHECK] EntityIds enregistrés: " + npcIds.keySet());
        return result;
    }
    
    /**
     * Vérifier si une entité Bukkit est un NPC
     */
    public boolean isGameNPC(Entity entity) {
        return npcIds.containsKey(entity.getEntityId());
    }
    
    /**
     * Récupérer un NPC par son entity ID
     */
    public GameNPC getNPCByEntityId(int entityId) {
        String name = npcIds.get(entityId);
        return name != null ? npcs.get(name) : null;
    }
    
    /**
     * Récupérer un NPC à partir d'une entité
     */
    public GameNPC getNPCFromEntity(Entity entity) {
        return getNPCByEntityId(entity.getEntityId());
    }
    
    /**
     * Récupérer tous les NPC
     */
    public Map<String, GameNPC> getAllNPCs() {
        return new HashMap<>(npcs);
    }
    
    /**
     * Nettoyer tous les NPC
     */
    public void cleanupAll() {
        // Détruire tous les NPCs pour tous les joueurs
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (Integer entityId : npcIds.keySet()) {
                destroyNPCForPlayer(player, entityId);
            }
        }
        
        npcs.clear();
        npcIds.clear();
        npcEntities.clear();
        npcProfiles.clear();
    }
    
    /**
     * Détruire un NPC pour un joueur
     */
    private void destroyNPCForPlayer(Player player, int entityId) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(entityId);
        connection.sendPacket(destroyPacket);
    }
    
    /**
     * Supprimer tous les villageois du monde Lobby (pour éviter les duplications)
     */
    public void removeAllLobbyVillagers() {
        org.bukkit.World lobby = Bukkit.getWorld("Lobby");
        if (lobby != null) {
            for (Entity entity : lobby.getEntities()) {
                if (entity.getType() == org.bukkit.entity.EntityType.VILLAGER) {
                    entity.remove();
                }
            }
        }
    }
}
