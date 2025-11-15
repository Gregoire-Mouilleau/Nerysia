package com.nerysia.plugin.game.focus.listeners;

import com.nerysia.plugin.Nerysia;
import com.nerysia.plugin.game.focus.FocusGame;
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
import org.bukkit.util.Vector;

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
        
        // Grenade Fumigène (WEB)
        if (item.getType() == Material.WEB && stripped.contains("Grenade Fumigène")) {
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
        
        Location hitLoc = snowball.getLocation();
        
        // Grenade fumigène - effets varient selon le tier
        if (snowball.hasMetadata("smoke_grenade")) {
            int smokeTier = shopData.getSmokeTier(thrower.getUniqueId());
            
            hitLoc.getWorld().playSound(hitLoc, Sound.FIZZ, 1.0f, 1.0f);
            
            // Nombre d'effets selon tier
            int effectCount = 50;
            if (smokeTier == 1) effectCount = 30;
            else if (smokeTier == 2) effectCount = 50;
            else if (smokeTier == 3) effectCount = 70;
            
            for (int i = 0; i < effectCount; i++) {
                hitLoc.getWorld().playEffect(hitLoc.clone().add(
                    Math.random() * 4 - 2,
                    Math.random() * 3,
                    Math.random() * 4 - 2
                ), Effect.SMOKE, 0);
            }
            
            // Rayon selon tier
            double radius = 4;
            if (smokeTier == 1) radius = 3;
            else if (smokeTier == 2) radius = 4;
            else if (smokeTier == 3) radius = 5;
            
            // Durée selon tier (en ticks)
            int duration = 80;
            if (smokeTier == 1) duration = 60;
            else if (smokeTier == 2) duration = 80;
            else if (smokeTier == 3) duration = 100;
            
            for (Player p : hitLoc.getWorld().getPlayers()) {
                if (p.getLocation().distance(hitLoc) <= radius) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, duration, 0));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, duration, 1));
                }
            }
        }
        
        // Cocktail Molotov - effets varient selon le tier
        if (snowball.hasMetadata("molotov")) {
            int molotovTier = shopData.getMolotovTier(thrower.getUniqueId());
            
            hitLoc.getWorld().playSound(hitLoc, Sound.GLASS, 1.0f, 0.8f);
            
            // Taille de la zone de feu selon tier
            int fireRadius = 2;
            if (molotovTier == 1) fireRadius = 1;
            else if (molotovTier == 2) fireRadius = 2;
            else if (molotovTier == 3) fireRadius = 3;
            
            List<Block> fireBlocks = new ArrayList<>();
            for (int x = -fireRadius; x <= fireRadius; x++) {
                for (int z = -fireRadius; z <= fireRadius; z++) {
                    Block b = hitLoc.clone().add(x, 0, z).getBlock();
                    if (b.getType() == Material.AIR) {
                        b.setType(Material.FIRE);
                        fireBlocks.add(b);
                    }
                }
            }
            
            // Durée du feu selon tier (en ticks)
            long fireDuration = 100L;
            if (molotovTier == 1) fireDuration = 80L;
            else if (molotovTier == 2) fireDuration = 100L;
            else if (molotovTier == 3) fireDuration = 120L;
            
            // Retirer le feu après le délai
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Block b : fireBlocks) {
                        if (b.getType() == Material.FIRE) {
                            b.setType(Material.AIR);
                        }
                    }
                }
            }.runTaskLater(Nerysia.getInstance(), fireDuration);
            
            // Dégâts et effet de feu selon tier
            double damageRadius = 3;
            double damage = 4.0;
            int fireTicks = 60;
            
            if (molotovTier == 1) {
                damageRadius = 2;
                damage = 3.0;
                fireTicks = 40;
            } else if (molotovTier == 2) {
                damageRadius = 3;
                damage = 4.0;
                fireTicks = 60;
            } else if (molotovTier == 3) {
                damageRadius = 4;
                damage = 5.0;
                fireTicks = 80;
            }
            
            for (Player p : hitLoc.getWorld().getPlayers()) {
                if (p.getLocation().distance(hitLoc) <= damageRadius) {
                    p.damage(damage);
                    p.setFireTicks(fireTicks);
                }
            }
        }
    }
    
    // ==================== MINES ====================
    
    @EventHandler
    public void onPlayerPlaceMine(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        FocusGame game = gameManager.getPlayerGame(player.getUniqueId());
        if (game == null) return;
        
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.STONE_PLATE) return;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;
        
        String displayName = item.getItemMeta().getDisplayName();
        if (!ChatColor.stripColor(displayName).contains("Mine")) return;
        
        event.setCancelled(true);
        
        // Une seule mine par round
        if (alreadyPlacedMineThisRound.contains(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Vous avez déjà posé une mine ce round !");
            return;
        }
        
        Block clickedBlock = event.getClickedBlock();
        Block mineBlock = clickedBlock.getRelative(event.getBlockFace());
        
        if (mineBlock.getType() != Material.AIR) {
            player.sendMessage(ChatColor.RED + "Vous ne pouvez pas placer la mine ici !");
            return;
        }
        
        // Placer la plaque de pression
        mineBlock.setType(Material.STONE_PLATE);
        
        Location mineLoc = mineBlock.getLocation();
        placedMines.put(mineLoc, player.getUniqueId());
        mineArmedTime.put(mineLoc, System.currentTimeMillis() + 2000); // Armée après 2 secondes
        alreadyPlacedMineThisRound.add(player.getUniqueId());
        
        // Retirer l'item de l'inventaire
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.setItemInHand(null);
        }
        
        player.sendMessage(ChatColor.GREEN + "Mine posée ! Elle s'armera dans 2 secondes...");
        player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 1.0f);
        
        // Effet visuel d'armement
        new BukkitRunnable() {
            @Override
            public void run() {
                if (mineBlock.getType() == Material.STONE_PLATE) {
                    mineBlock.getWorld().playSound(mineLoc, Sound.CLICK, 1.0f, 2.0f);
                    player.sendMessage(ChatColor.YELLOW + "Mine armée !");
                }
            }
        }.runTaskLater(Nerysia.getInstance(), 40L);
    }
    
    @EventHandler
    public void onPlayerStepOnMine(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        FocusGame game = gameManager.getPlayerGame(player.getUniqueId());
        if (game == null) return;
        
        Block blockBelow = player.getLocation().getBlock().getRelative(0, -1, 0);
        if (blockBelow.getType() != Material.STONE_PLATE) return;
        
        Location mineLoc = blockBelow.getLocation();
        if (!placedMines.containsKey(mineLoc)) return;
        
        // Vérifier si la mine est armée
        long armedTime = mineArmedTime.getOrDefault(mineLoc, 0L);
        if (System.currentTimeMillis() < armedTime) return;
        
        UUID placerId = placedMines.get(mineLoc);
        
        // Ne pas exploser sur le joueur qui l'a posée
        if (player.getUniqueId().equals(placerId)) return;
        
        // Explosion !
        blockBelow.setType(Material.AIR);
        placedMines.remove(mineLoc);
        mineArmedTime.remove(mineLoc);
        
        Location explosionLoc = mineLoc.clone().add(0.5, 0, 0.5);
        explosionLoc.getWorld().createExplosion(explosionLoc, 3.0f, false);
        
        player.damage(8.0);
        player.setVelocity(player.getLocation().getDirection().multiply(-1).setY(0.8));
        
        Player placer = Bukkit.getPlayer(placerId);
        if (placer != null) {
            placer.sendMessage(ChatColor.GREEN + "Votre mine a explosé sur " + player.getName() + " !");
        }
    }
    
    // ==================== GRENADE PROPULSIVE ====================
    
    @EventHandler
    public void onPlayerPlacePropulseGrenade(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        FocusGame game = gameManager.getPlayerGame(player.getUniqueId());
        if (game == null) return;
        
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.FEATHER) return;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;
        
        String displayName = item.getItemMeta().getDisplayName();
        if (!ChatColor.stripColor(displayName).contains("Grenade Propulse")) return;
        
        event.setCancelled(true);
        
        // Propulser le joueur
        Vector direction = player.getLocation().getDirection();
        player.setVelocity(direction.multiply(2).setY(1));
        
        player.playSound(player.getLocation(), Sound.FIREWORK_LAUNCH, 1.0f, 1.0f);
        player.getWorld().playEffect(player.getLocation(), Effect.SMOKE, 0);
        
        // Retirer l'item
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.setItemInHand(null);
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
