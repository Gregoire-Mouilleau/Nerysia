package com.nerysia.plugin.game.focus.listeners;

import com.nerysia.plugin.Nerysia;
import com.nerysia.plugin.game.focus.FocusGame;
import com.nerysia.plugin.game.focus.FocusGameController;
import com.nerysia.plugin.game.focus.FocusGameManager;
import com.nerysia.plugin.game.focus.FocusShopData;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Gère tous les items consommables du Focus (mines, grenades, molotov, etc.)
 * Basé sur FocusListener.java de l'ancien code
 */
public class FocusItemsListener implements Listener {
    
    private final FocusGameManager gameManager;
    private final FocusShopData shopData;
    
    // Mines
    private final Map<Location, UUID> placedMines = new HashMap<>();
    private final Map<Location, Long> mineArmedTime = new HashMap<>();
    private final Set<UUID> alreadyPlacedMineThisRound = new HashSet<>();
    
    // Projectiles trackés
    private final Set<Projectile> trackedProjectiles = Collections.newSetFromMap(new WeakHashMap<>());
    
    // Joueurs avec no fall damage temporaire (grenade propulsive)
    private final Set<UUID> noFallDamagePlayers = new HashSet<>();
    
    public FocusItemsListener(FocusGameManager gameManager, FocusShopData shopData) {
        this.gameManager = gameManager;
        this.shopData = shopData;
    }
    
    public void clearPlacedMinesThisRound() {
        alreadyPlacedMineThisRound.clear();
        placedMines.clear();
        mineArmedTime.clear();
    }
    
    // ==================== GRENADES FUMIGENES ====================
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        FocusGame game = gameManager.getPlayerGame(player.getUniqueId());
        if (game == null) return;
        
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;
        
        String displayName = item.getItemMeta().getDisplayName();
        String stripped = ChatColor.stripColor(displayName);
        
        // Grenade Fumigène (SNOW_BALL)
        if (item.getType() == Material.SNOW_BALL && stripped.contains("Grenade Fumigène")) {
            event.setCancelled(true);
            
            Snowball snowball = player.launchProjectile(Snowball.class);
            snowball.setVelocity(player.getLocation().getDirection().multiply(1.5));
            snowball.setMetadata("smoke_grenade", new FixedMetadataValue(Nerysia.getInstance(), true));
            trackedProjectiles.add(snowball);
            
            player.playSound(player.getLocation(), Sound.ARROW_HIT, 1.0f, 0.5f);
            
            // Retirer l'item
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                player.setItemInHand(null);
            }
        }
        
        // Cocktail Molotov (FIREBALL)
        if (item.getType() == Material.FIREBALL && stripped.contains("Cocktail Molotov")) {
            event.setCancelled(true);
            
            Snowball snowball = player.launchProjectile(Snowball.class);
            snowball.setVelocity(player.getLocation().getDirection().multiply(1.5));
            snowball.setMetadata("molotov", new FixedMetadataValue(Nerysia.getInstance(), true));
            trackedProjectiles.add(snowball);
            
            player.playSound(player.getLocation(), Sound.GLASS, 1.0f, 1.5f);
            
            // Retirer l'item
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                player.setItemInHand(null);
            }
        }
    }
    
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Snowball)) return;
        Snowball snowball = (Snowball) event.getEntity();
        if (!(snowball.getShooter() instanceof Player)) return;
        
        Player thrower = (Player) snowball.getShooter();
        FocusGame game = gameManager.getPlayerGame(thrower.getUniqueId());
        if (game == null) return;
        
        if (!snowball.hasMetadata("smoke_grenade") && !snowball.hasMetadata("molotov")) return;
        
        // Grenade fumigène - effets varient selon le tier (exactement comme ancien code)
        if (snowball.hasMetadata("smoke_grenade")) {
            int smokeTier = shopData.getSmokeTier(thrower.getUniqueId());
            
            // Paramètres selon tier (comme ancien code)
            int radius = 5;
            int durationTicks = 10 * 20; // 10 secondes
            if (smokeTier == 1) {
                radius = 7;
                durationTicks = 15 * 20; // 15 secondes
            } else if (smokeTier == 2) {
                radius = 10;
                durationTicks = 20 * 20; // 20 secondes
            }
            
            final Location impact = snowball.getLocation();
            final int finalRadius = radius;
            final int finalDuration = durationTicks;
            
            // BukkitRunnable qui tourne en boucle pour afficher les particules continuellement
            new BukkitRunnable() {
                int ticks = 0;
                final Set<UUID> affected = new HashSet<>();
                
                @Override
                public void run() {
                    if (ticks >= finalDuration) {
                        // Fin de la smoke, retirer blindness de tous les joueurs affectés
                        for (UUID id : affected) {
                            Player p = Bukkit.getPlayer(id);
                            if (p != null) p.removePotionEffect(PotionEffectType.BLINDNESS);
                        }
                        cancel();
                        return;
                    }
                    
                    // Afficher 750 particules de fumée chaque tick
                    for (int i = 0; i < 750; i++) {
                        Location loc = impact.clone().add(
                            (Math.random() - 0.5) * finalRadius * 2,
                            Math.random() * 2.5,
                            (Math.random() - 0.5) * finalRadius * 2
                        );
                        impact.getWorld().spigot().playEffect(loc, Effect.LARGE_SMOKE, 0, 0, 0, 0, 0, 0.08f, 1, 30);
                    }
                    
                    // Appliquer blindness aux joueurs dans la zone
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (!p.getWorld().equals(impact.getWorld())) continue;
                        double distSq = p.getLocation().distanceSquared(impact);
                        if (distSq <= finalRadius * finalRadius) {
                            if (!affected.contains(p.getUniqueId())) {
                                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, finalDuration - ticks, 0, false, false));
                                affected.add(p.getUniqueId());
                            }
                        } else {
                            if (affected.contains(p.getUniqueId())) {
                                p.removePotionEffect(PotionEffectType.BLINDNESS);
                                affected.remove(p.getUniqueId());
                            }
                        }
                    }
                    
                    ticks++;
                }
            }.runTaskTimer(Nerysia.getInstance(), 0L, 1L);
        }
        
        // Cocktail Molotov - effets varient selon le tier (exactement comme ancien code)
        if (snowball.hasMetadata("molotov")) {
            int molotovTier = shopData.getMolotovTier(thrower.getUniqueId());
            
            // Paramètres selon tier (comme ancien code)
            int radius = 4;
            int burnAfterExit = 0;
            if (molotovTier == 1) {
                radius = 6;
                burnAfterExit = 2;
            } else if (molotovTier == 2) {
                radius = 7;
                burnAfterExit = 4;
            }
            
            final Location impactLocation = snowball.getLocation();
            final int radiusFinal = radius;
            final int burnTimeFinal = burnAfterExit * 20;
            
            // BukkitRunnable qui tourne en boucle pour afficher les particules de feu continuellement
            new BukkitRunnable() {
                int ticks = 0;
                final Set<UUID> burning = new HashSet<>();
                
                @Override
                public void run() {
                    if (ticks >= 100) {
                        // Fin du molotov, appliquer burn final aux joueurs qui étaient dedans
                        for (UUID uuid : burning) {
                            Player p = Bukkit.getPlayer(uuid);
                            if (p != null) p.setFireTicks(burnTimeFinal);
                        }
                        cancel();
                        return;
                    }
                    
                    // Afficher 100 particules de flamme toutes les 5 ticks
                    for (int i = 0; i < 100; i++) {
                        Location loc = impactLocation.clone().add(
                            (Math.random() - 0.5) * radiusFinal * 2,
                            0.1,
                            (Math.random() - 0.5) * radiusFinal * 2
                        );
                        impactLocation.getWorld().spigot().playEffect(loc, Effect.FLAME, 0, 0, 0, 0, 0, 0.01f, 5, 30);
                    }
                    
                    // Mettre le feu aux joueurs dans la zone
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (!p.getWorld().equals(impactLocation.getWorld())) continue;
                        if (p.getLocation().distance(impactLocation) <= radiusFinal) {
                            p.setFireTicks(20);
                            burning.add(p.getUniqueId());
                        }
                    }
                    
                    ticks += 5;
                }
            }.runTaskTimer(Nerysia.getInstance(), 0L, 5L);
        }
    }
    
    // ==================== MINES ====================
    
    @EventHandler
    public void onPlayerPlaceMine(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack inHand = player.getItemInHand();
        if (inHand == null || inHand.getType() != Material.REDSTONE) return;
        
        FocusGame game = gameManager.getPlayerGame(player.getUniqueId());
        if (game == null) return;
        
        if (!inHand.hasItemMeta() || !inHand.getItemMeta().hasDisplayName()) return;
        String displayName = inHand.getItemMeta().getDisplayName();
        if (!ChatColor.stripColor(displayName).contains("Mine")) return;
        
        // Une seule mine par round
        if (alreadyPlacedMineThisRound.contains(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Tu as déjà posé une mine cette manche.");
            return;
        }
        
        event.setCancelled(true);
        
        Block targetBlock = player.getTargetBlock((Set<Material>) null, 5);
        if (targetBlock == null || targetBlock.getType() == Material.AIR || !targetBlock.getType().isSolid()) {
            player.sendMessage(ChatColor.RED + "Tu ne peux pas poser une mine ici.");
            return;
        }
        
        // Liste des blocs décoratifs à exclure
        Material type = targetBlock.getType();
        if (type == Material.LONG_GRASS ||
            type == Material.DOUBLE_PLANT ||
            type == Material.YELLOW_FLOWER ||
            type == Material.RED_ROSE ||
            type == Material.DEAD_BUSH ||
            type == Material.SAPLING ||
            type == Material.CARPET) {
            player.sendMessage(ChatColor.RED + "Tu ne peux pas poser une mine sur ce type de bloc.");
            return;
        }
        
        Location loc = targetBlock.getLocation();
        
        if (placedMines.containsKey(loc)) {
            player.sendMessage(ChatColor.RED + "Une mine est déjà posée ici !");
            return;
        }
        
        placedMines.put(loc, player.getUniqueId());
        mineArmedTime.put(loc, System.currentTimeMillis() + 3000); // 3 secondes
        alreadyPlacedMineThisRound.add(player.getUniqueId());
        
        ItemStack held = player.getItemInHand();
        if (held.getAmount() > 1) {
            held.setAmount(held.getAmount() - 1);
        } else {
            player.setItemInHand(null);
        }
        
        player.sendMessage(ChatColor.RED + "Mine posée !");
        
        // Effet visuel de particules rouges qui tournent
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!placedMines.containsKey(loc)) {
                    cancel();
                    return;
                }
                
                Player placer = Bukkit.getPlayer(placedMines.get(loc));
                if (placer != null) {
                    placer.spigot().playEffect(
                        loc.clone().add(0.5, 1.1, 0.5),
                        Effect.COLOURED_DUST,
                        1, 0,
                        0.8f, 0f, 0f,
                        1f, 0, 16
                    );
                }
            }
        }.runTaskTimer(Nerysia.getInstance(), 0L, 4L);
    }
    
    @EventHandler
    public void onPlayerStepOnMine(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        FocusGame game = gameManager.getPlayerGame(player.getUniqueId());
        if (game == null) return;
        
        Location underFeet = player.getLocation().clone().subtract(0, 1, 0).getBlock().getLocation();
        
        for (Map.Entry<Location, UUID> entry : placedMines.entrySet()) {
            Location mineLoc = entry.getKey();
            
            if (!mineLoc.equals(underFeet)) continue;
            
            long readyTime = mineArmedTime.getOrDefault(mineLoc, 0L);
            if (System.currentTimeMillis() < readyTime) continue;
            
            // Tier 0 par défaut
            int mineTier = 0;
            
            final int damage;
            final double radiusSquared;
            
            if (mineTier == 1) {
                damage = 20;
                radiusSquared = 1.5; // ≈ rayon 1 bloc
            } else if (mineTier == 2) {
                damage = 30;
                radiusSquared = 2.0;
            } else if (mineTier == 3) {
                damage = 40;
                radiusSquared = 2.25;
            } else {
                damage = 14;
                radiusSquared = 1.0;
            }
            
            Location explosionLoc = mineLoc.clone().add(0.5, 0.5, 0.5);
            mineLoc.getWorld().playSound(explosionLoc, Sound.CLICK, 1.0f, 0.5f);
            
            Bukkit.getScheduler().runTaskLater(Nerysia.getInstance(), () -> {
                explosionLoc.getWorld().playSound(explosionLoc, Sound.EXPLODE, 1.5f, 1.0f);
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!p.getWorld().equals(explosionLoc.getWorld())) continue;
                    if (p.getLocation().distanceSquared(explosionLoc) <= radiusSquared) {
                        p.damage(damage);
                    }
                }
                placedMines.remove(mineLoc);
                mineArmedTime.remove(mineLoc);
            }, 1L);
            
            break;
        }
    }
    
    // ==================== GRENADE PROPULSIVE ====================
    
    @EventHandler
    public void onPlayerPlacePropulseGrenade(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack inHand = player.getItemInHand();
        
        if (inHand == null || inHand.getType() != Material.FEATHER) return;
        if (!inHand.hasItemMeta() || !inHand.getItemMeta().hasDisplayName()) return;
        
        String displayName = inHand.getItemMeta().getDisplayName();
        if (!ChatColor.stripColor(displayName).contains("Grenade Propulse")) return;
        
        FocusGame game = gameManager.getPlayerGame(player.getUniqueId());
        if (game == null) return;
        
        event.setCancelled(true);
        
        // Retrait de l'item
        if (inHand.getAmount() > 1) {
            inHand.setAmount(inHand.getAmount() - 1);
        } else {
            player.setItemInHand(null);
        }
        
        // Effet d'explosion + propulsion immédiate (exactement comme ancien code)
        Location loc = player.getLocation();
        player.getWorld().playSound(loc, Sound.EXPLODE, 1.5f, 1.2f);
        player.getWorld().spigot().playEffect(loc.clone().add(0, 0.5, 0), Effect.EXPLOSION_LARGE, 0, 0, 0, 0, 0, 0, 1, 30);
        
        // Propulsion vers l'avant (exactement comme ancien code)
        player.setVelocity(player.getLocation().getDirection().multiply(1.5).setY(1));
        
        // Retirer les dégâts de chute pendant 3 secondes
        noFallDamagePlayers.add(player.getUniqueId());
        Bukkit.getScheduler().runTaskLater(Nerysia.getInstance(), () -> {
            noFallDamagePlayers.remove(player.getUniqueId());
        }, 60L); // 3 secondes
    }
    
    // ==================== PROTECTION FALL DAMAGE ====================
    
    @EventHandler
    public void onPlayerFallDamage(org.bukkit.event.entity.EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (event.getCause() != org.bukkit.event.entity.EntityDamageEvent.DamageCause.FALL) return;
        
        Player player = (Player) event.getEntity();
        
        // Protection pour les bottes (permanent)
        FocusGame game = gameManager.getPlayerGame(player.getUniqueId());
        if (game != null) {
            FocusGameController controller = gameManager.getGameController(game);
            if (controller != null) {
                FocusShopData shopData = controller.getShopData();
                if (shopData.getBootsLevel(player.getUniqueId()) > 0) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        
        // Protection temporaire pour grenade propulsive
        if (noFallDamagePlayers.contains(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }
    
    // ==================== HACHE ELEMENTAIRE ====================
    
    @EventHandler
    public void onPlayerHitWithAxe(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof Player)) return;
        
        Player attacker = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();
        
        FocusGame game = gameManager.getPlayerGame(attacker.getUniqueId());
        if (game == null) return;
        
        ItemStack item = attacker.getItemInHand();
        if (item == null || !isAxe(item.getType())) return;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;
        
        String displayName = item.getItemMeta().getDisplayName();
        String stripped = ChatColor.stripColor(displayName);
        
        if (stripped.contains("Hache de Feu")) {
            victim.setFireTicks(100);
            victim.sendMessage(ChatColor.RED + "Vous brûlez !");
        } else if (stripped.contains("Hache de Glace")) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 2));
            victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 100, 1));
            victim.sendMessage(ChatColor.AQUA + "Vous êtes ralenti !");
        } else if (stripped.contains("Hache de Foudre")) {
            attacker.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1));
            attacker.sendMessage(ChatColor.YELLOW + "Speed activé !");
        }
    }
    
    private boolean isAxe(Material material) {
        return material == Material.WOOD_AXE || material == Material.STONE_AXE ||
               material == Material.GOLD_AXE || material == Material.IRON_AXE ||
               material == Material.DIAMOND_AXE;
    }
}
