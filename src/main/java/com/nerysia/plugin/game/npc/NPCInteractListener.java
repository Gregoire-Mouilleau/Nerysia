package com.nerysia.plugin.game.npc;

import com.nerysia.plugin.Nerysia;
import com.nerysia.plugin.game.gui.GameModeMenuGUI;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

public class NPCInteractListener implements Listener {
    
    private final Nerysia plugin;
    private final NPCManager npcManager;
    
    public NPCInteractListener(Nerysia plugin, NPCManager npcManager) {
        this.plugin = plugin;
        this.npcManager = npcManager;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Injecter le packet listener immédiatement
        org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
            injectPlayer(player);
            plugin.getLogger().info("Packet listener injecté pour " + player.getName());
        }, 5L);
        
        // Spawner tous les NPCs pour ce joueur après un court délai
        org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
            npcManager.spawnAllNPCsForPlayer(player);
            plugin.getLogger().info("NPCs spawnés pour " + player.getName());
        }, 10L); // 0.5 secondes de délai
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        removePlayer(event.getPlayer());
    }
    
    private void injectPlayer(Player player) {
        try {
            CraftPlayer craftPlayer = (CraftPlayer) player;
            Channel channel = craftPlayer.getHandle().playerConnection.networkManager.channel;
            
            if (channel.pipeline().get("npc_interact") != null) {
                plugin.getLogger().info("Packet interceptor déjà présent pour " + player.getName());
                return;
            }
            
            channel.pipeline().addAfter("decoder", "npc_interact", new MessageToMessageDecoder<net.minecraft.server.v1_8_R3.Packet<?>>() {
                @Override
                protected void decode(ChannelHandlerContext ctx, net.minecraft.server.v1_8_R3.Packet<?> packet, List<Object> out) throws Exception {
                    out.add(packet);
                    
                    // Vérifier si c'est un packet d'interaction avec une entité
                    if (packet instanceof PacketPlayInUseEntity) {
                        PacketPlayInUseEntity usePacket = (PacketPlayInUseEntity) packet;
                        int entityId = getEntityId(usePacket);
                        
                        plugin.getLogger().info("========================================");
                        plugin.getLogger().info("[NPC-DEBUG] Interaction avec entité ID: " + entityId);
                        plugin.getLogger().info("[NPC-DEBUG] Vérification si c'est un NPC...");
                        
                        if (npcManager.isGameNPC(entityId)) {
                            plugin.getLogger().info("[NPC-DEBUG] C'est un NPC de jeu !");
                            PacketPlayInUseEntity.EnumEntityUseAction action = getAction(usePacket);
                            
                            plugin.getLogger().info("[NPC-DEBUG] Action: " + action);
                            
                            // Accepter toutes les actions (INTERACT, INTERACT_AT et même ATTACK)
                            GameNPC npc = npcManager.getNPCByEntityId(entityId);
                            if (npc != null) {
                                plugin.getLogger().info("[NPC-DEBUG] NPC trouvé: " + npc.getDisplayName() + ", Mode: " + npc.getGameMode().name());
                                // Ouvrir le menu dans le thread principal
                                plugin.getServer().getScheduler().runTask(plugin, () -> {
                                    try {
                                        plugin.getLogger().info("[NPC-DEBUG] Création du GUI pour " + npc.getGameMode().getDisplayName());
                                        GameModeMenuGUI gui = new GameModeMenuGUI(plugin, npc.getGameMode());
                                        plugin.getLogger().info("[NPC-DEBUG] Ouverture du GUI...");
                                        gui.open(player);
                                        plugin.getLogger().info("[NPC-DEBUG] GUI ouvert avec succès !");
                                        player.sendMessage("§aOuverture du menu " + npc.getGameMode().getDisplayName());
                                    } catch (Exception e) {
                                        plugin.getLogger().severe("[NPC-DEBUG] Erreur lors de l'ouverture du GUI:");
                                        e.printStackTrace();
                                        player.sendMessage("§cErreur lors de l'ouverture du menu !");
                                    }
                                });
                            } else {
                                plugin.getLogger().warning("[NPC-DEBUG] NPC null pour l'entityId: " + entityId);
                            }
                        } else {
                            plugin.getLogger().warning("[NPC-DEBUG] ❌ Ce n'est pas un NPC de jeu (entityId: " + entityId + ")");
                            plugin.getLogger().warning("[NPC-DEBUG] NPCs enregistrés: " + npcManager.getAllNPCs().keySet());
                        }
                        plugin.getLogger().info("========================================");
                    }
                }
            });
            
            plugin.getLogger().info("Packet interceptor ajouté avec succès pour " + player.getName());
            
        } catch (Exception e) {
            plugin.getLogger().severe("Erreur lors de l'injection du packet interceptor:");
            e.printStackTrace();
        }
    }
    
    private void removePlayer(Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        Channel channel = craftPlayer.getHandle().playerConnection.networkManager.channel;
        
        if (channel.pipeline().get("npc_interact") != null) {
            channel.pipeline().remove("npc_interact");
        }
    }
    
    private int getEntityId(PacketPlayInUseEntity packet) {
        try {
            java.lang.reflect.Field field = PacketPlayInUseEntity.class.getDeclaredField("a");
            field.setAccessible(true);
            return (int) field.get(packet);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    
    private PacketPlayInUseEntity.EnumEntityUseAction getAction(PacketPlayInUseEntity packet) {
        try {
            java.lang.reflect.Field field = PacketPlayInUseEntity.class.getDeclaredField("action");
            field.setAccessible(true);
            return (PacketPlayInUseEntity.EnumEntityUseAction) field.get(packet);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
