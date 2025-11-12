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
import org.bukkit.craftbukkit.v1_8_R3.scoreboard.CraftScoreboard;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.*;

public class NPCManager {

    private final Map<String, GameNPC> npcs;
    private final Map<Integer, String> npcIds;
    private final Map<String, EntityPlayer> npcEntities;
    private final Map<String, GameProfile> npcProfiles;
    private final Map<String, ArmorStand> npcNameTags;

    public NPCManager() {
        this.npcs = new HashMap<>();
        this.npcIds = new HashMap<>();
        this.npcEntities = new HashMap<>();
        this.npcProfiles = new HashMap<>();
        this.npcNameTags = new HashMap<>();
    }

    /**
     * Créer un NPC joueur pour un mode de jeu
     */
    public GameNPC createNPC(Location location, GameMode gameMode, String displayName, String skinTexture, String skinSignature) {
        // Utiliser un nom interne non problématique (client a besoin d'un nom)
        String profileName = UUID.randomUUID().toString().substring(0, 8);

        // Créer le GameProfile et appliquer la texture
        GameProfile profile = new GameProfile(UUID.randomUUID(), profileName);
        if (skinSignature != null && !skinSignature.isEmpty()) {
            profile.getProperties().put("textures", new Property("textures", skinTexture, skinSignature));
            Bukkit.getLogger().info("[NPC] Texture avec signature appliquée");
        } else {
            profile.getProperties().put("textures", new Property("textures", skinTexture));
            Bukkit.getLogger().info("[NPC] Texture sans signature appliquée");
        }

        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
        EntityPlayer npc = new EntityPlayer(server, world, profile, new PlayerInteractManager(world));
        npc.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

        int id = npc.getId();
        GameNPC gameNPC = new GameNPC(id, gameMode, displayName, location);

        // Hologramme (ArmorStand) pour afficher le nom au-dessus si souhaité
        Location holoLoc = location.clone().add(0, 1.82, 0);
        ArmorStand holo = (ArmorStand) location.getWorld().spawnEntity(holoLoc, EntityType.ARMOR_STAND);
        holo.setCustomName(displayName);
        holo.setCustomNameVisible(true);
        holo.setVisible(false);
        holo.setGravity(false);
        holo.setSmall(true);
        holo.setMarker(true);
        holo.setBasePlate(false);

        // Stockage
        npcs.put(displayName, gameNPC);
        npcIds.put(id, displayName);
        npcEntities.put(displayName, npc);
        npcProfiles.put(displayName, profile);
        npcNameTags.put(displayName, holo);

        // Spawn auprès des joueurs
        spawnNPCForAllPlayers(npc, profile);

        Bukkit.getLogger().info("[NPC] NPC '" + displayName + "' créé (id=" + id + ") et spawné.");
        return gameNPC;
    }

    /**
     * Spawner tous les NPC pour un joueur
     */
    public void spawnAllNPCsForPlayer(Player player) {
        Bukkit.getLogger().info("[NPC-SPAWN] Spawning " + npcs.size() + " NPCs pour " + player.getName());
        for (String name : npcs.keySet()) {
            try {
                EntityPlayer npc = npcEntities.get(name);
                GameProfile profile = npcProfiles.get(name);
                if (npc != null && profile != null) {
                    spawnNPCForPlayer(player, npc, profile);
                    Bukkit.getLogger().info("[NPC-SPAWN] ✓ " + name + " spawné pour " + player.getName());
                } else {
                    Bukkit.getLogger().warning("[NPC-SPAWN] NPC manquant: " + name);
                }
            } catch (Exception e) {
                Bukkit.getLogger().severe("[NPC-SPAWN] Erreur lors du spawn de " + name + " pour " + player.getName());
                e.printStackTrace();
            }
        }
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
                Bukkit.getLogger().warning("[NPC-SPAWN] Joueur null/hors-ligne");
                return;
            }

            PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
            if (connection == null) {
                Bukkit.getLogger().warning("[NPC-SPAWN] PlayerConnection null pour " + player.getName());
                return;
            }

            try {
            ScoreboardTeam team = new ScoreboardTeam(
                    ((CraftScoreboard) Bukkit.getScoreboardManager().getMainScoreboard()).getHandle(),
                    "npcHide_" + npc.getId()
            );
            team.setNameTagVisibility(ScoreboardTeamBase.EnumNameTagVisibility.NEVER);
            team.getPlayerNameSet().add(npc.getName());
            connection.sendPacket(new PacketPlayOutScoreboardTeam(team, 0));
            connection.sendPacket(new PacketPlayOutScoreboardTeam(team, 3));
        } catch (Exception ex) {
            Bukkit.getLogger().severe("[NPC] Erreur création team hide: " + ex.getMessage());
        }


            // Packets nécessaires pour spawner un joueur (EntityPlayer)
            PacketPlayOutPlayerInfo addPlayer = new PacketPlayOutPlayerInfo(
                    PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, npc);
            PacketPlayOutNamedEntitySpawn spawnPacket = new PacketPlayOutNamedEntitySpawn(npc);
            PacketPlayOutEntityHeadRotation headRotation = new PacketPlayOutEntityHeadRotation(
                    npc, (byte) ((npc.yaw * 256.0F) / 360.0F));
            PacketPlayOutPlayerInfo removePlayer = new PacketPlayOutPlayerInfo(
                    PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, npc);

            // Envoi des packets (ordre important)
            connection.sendPacket(addPlayer);
            connection.sendPacket(spawnPacket);
            connection.sendPacket(headRotation);
            connection.sendPacket(removePlayer);

            // Envoi metadata (sécurité supplémentaire)
            hideNameTagWithMetadata(npc, player);

        } catch (Exception e) {
            Bukkit.getLogger().severe("[NPC-SPAWN] Erreur lors du spawn du NPC pour " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Cacher le nametag du NPC en utilisant PacketPlayOutScoreboardTeam
     * On envoie le packet en mode création (0) puis mode update (2) pour forcer le client 1.8 à appliquer la visibilité.
     */
    private void hideNameTag(EntityPlayer npc, Player player) {
        try {
            String teamName = "npcHide_" + npc.getId();
            String npcName = npc.getName();

            // Création du packet
            PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam();

            // Champs couramment présents dans la classe NMS 1.8_R3
            setField(packet, "a", teamName); // team name
            setField(packet, "b", teamName); // display name
            setField(packet, "c", ""); // prefix
            setField(packet, "d", ""); // suffix
            setField(packet, "e", "never"); // nameTagVisibility = never
            setField(packet, "f", "never"); // collisionRule = never (optionnel)
            setField(packet, "g", 0); // friendly flags
            setField(packet, "h", Collections.singletonList(npcName)); // members
            setField(packet, "i", 0); // mode = 0 (create)

            // Envoi création
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);

            // Mode update (2) pour forcer la mise à jour côté client
            setField(packet, "i", 2); // mode = 2 (update)
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);

            Bukkit.getLogger().info("[NPC-NAMETAG] Team créée/mise à jour pour NPC id=" + npc.getId() + " (team=" + teamName + ")");

        } catch (Exception ex) {
            Bukkit.getLogger().severe("[NPC-NAMETAG] Erreur hideNameTag: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Supprime visuellement le nametag via DataWatcher / metadata (sécurité supplémentaire)
     */
    private void hideNameTagWithMetadata(EntityPlayer npc, Player player) {
        try {
            DataWatcher watcher = new DataWatcher(npc);
            // Index 2 = custom name (String)
            watcher.a(2, "");
            // Index 3 = custom name visible (Byte) -> 0 = false
            watcher.a(3, (byte) 0);

            PacketPlayOutEntityMetadata meta = new PacketPlayOutEntityMetadata(npc.getId(), watcher, true);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(meta);
        } catch (Exception ex) {
            Bukkit.getLogger().severe("[NPC-METADATA] Erreur hideNameTagWithMetadata: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Méthode utilitaire pour définir un champ par réflexion
     */
    private void setField(Object packet, String fieldName, Object value) throws Exception {
        Field f = packet.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(packet, value);
    }

    /**
     * Vérifier si un entity ID correspond à un NPC de jeu
     */
    public boolean isGameNPC(int entityId) {
        return npcIds.containsKey(entityId);
    }

    /**
     * Vérifier si une entité Bukkit est un NPC
     */
    public boolean isGameNPC(Entity entity) {
        return npcIds.containsKey(entity.getEntityId());
    }

    /**
     * Récupérer un NPC par entity id
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
        // Détruire les NPC côté client
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (Integer entityId : npcIds.keySet()) {
                try {
                    PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(entityId);
                    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(destroy);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        // Supprimer les hologrammes
        for (ArmorStand holo : npcNameTags.values()) {
            if (holo != null && !holo.isDead()) holo.remove();
        }

        npcs.clear();
        npcIds.clear();
        npcEntities.clear();
        npcProfiles.clear();
        npcNameTags.clear();
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
