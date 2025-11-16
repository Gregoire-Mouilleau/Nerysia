package com.nerysia.plugin.game.focus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.nerysia.plugin.Nerysia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Gère le shop Focus - reproduction exacte de FocusShopCommand.java
 */
public class FocusShopGUI implements Listener {
    
    private static final Random RANDOM = new Random();
    
    private static final String INVENTORY_NAME = ChatColor.DARK_PURPLE + "Shop Focus";
    
    // Progressions d'items
    private static final Material[] SWORD_PROGRESSION = {
        Material.STICK, Material.WOOD_SWORD, Material.STONE_SWORD, 
        Material.GOLD_SWORD, Material.IRON_SWORD, Material.DIAMOND_SWORD
    };
    
    private static final Material[] HELMET_PROGRESSION = {
        Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET, Material.GOLD_HELMET,
        Material.IRON_HELMET
    };
    
    private static final Material[] CHESTPLATE_PROGRESSION = {
        Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.GOLD_CHESTPLATE,
        Material.IRON_CHESTPLATE
    };
    
    private static final Material[] LEGGINGS_PROGRESSION = {
        Material.LEATHER_LEGGINGS, Material.CHAINMAIL_LEGGINGS, Material.GOLD_LEGGINGS,
        Material.IRON_LEGGINGS
    };
    
    private static final Material[] BOOTS_PROGRESSION = {
        Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS, Material.GOLD_BOOTS,
        Material.IRON_BOOTS
    };
    
    private static final Material[] AXE_PROGRESSION = {
        Material.WOOD_AXE, Material.STONE_AXE, Material.GOLD_AXE,
        Material.IRON_AXE, Material.DIAMOND_AXE
    };
    
    private static final int MAX_BOW_TIER = 5;
    private static final int MAX_ARROW_TIER = 3;
    
    // Coûts des upgrades (index = niveau actuel)
    private static final int[] SWORD_COSTS = {1, 2, 4, 5, 8};
    private static final int[] HELMET_COSTS = {1, 2, 3, 4};
    private static final int[] CHESTPLATE_COSTS = {1, 2, 3, 4};
    private static final int[] LEGGINGS_COSTS = {1, 2, 3, 4};
    private static final int[] BOOTS_COSTS = {1, 2, 3, 4};
    private static final int[] BOW_COSTS = {1, 2, 3, 4, 5, 6};
    private static final int[] ARROW_COSTS = {1, 2, 3};
    private static final int[] AXE_COSTS = {1, 2, 3, 4, 5};
    private static final int[] SMOKE_COSTS = {1, 2, 3};
    private static final int[] MOLOTOV_COSTS = {1, 2, 3};
    private static final int[] MINE_COSTS = {2, 3}; // Niveau 0->1, 1->2
    private static final int[] REPULSIVE_COSTS = {2, 3}; // Niveau 0->1, 1->2
    
    private static final int MAX_SMOKE_TIER = 3;
    private static final int MAX_MOLOTOV_TIER = 3;
    private static final int MAX_MINE_TIER = 2;
    private static final int MAX_REPULSIVE_TIER = 2;
    
    // Coûts d'achat des consommables
    private static final int SMOKE_PURCHASE_COST = 2;
    private static final int MOLOTOV_PURCHASE_COST = 3;
    private static final int MINE_PURCHASE_COST = 4;
    private static final int REPULSIVE_PURCHASE_COST = 5;
    
    private final FocusShopData shopData;
    private final FocusPointsManager pointsManager;
    private FocusGameController currentController;
    
    public FocusShopGUI(FocusShopData shopData, FocusPointsManager pointsManager) {
        this.shopData = shopData;
        this.pointsManager = pointsManager;
    }
    
    public void open(Player player, FocusGameController controller) {
        this.currentController = controller;
        Inventory inv = Bukkit.createInventory(null, 54, INVENTORY_NAME);
        
        UUID playerId = player.getUniqueId();
        int points = pointsManager.getPoints(player);
        
        // Vitre grise partout
        ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, glass);
        }
        
        // Épée (slot 12)
        inv.setItem(12, createUpgradeItem(
            "Épée",
            SWORD_PROGRESSION,
            shopData.getSwordLevel(playerId),
            SWORD_PROGRESSION.length - 1,
            SWORD_COSTS,
            points,
            ChatColor.YELLOW
        ));
        
        // Casque (slot 11)
        inv.setItem(10, createUpgradeItem(
            "Casque",
            HELMET_PROGRESSION,
            shopData.getHelmetLevel(playerId),
            HELMET_PROGRESSION.length - 1,
            HELMET_COSTS,
            points,
            ChatColor.AQUA
        ));
        
        // Plastron (slot 19)
        inv.setItem(19, createUpgradeItem(
            "Plastron",
            CHESTPLATE_PROGRESSION,
            shopData.getChestplateLevel(playerId),
            CHESTPLATE_PROGRESSION.length - 1,
            CHESTPLATE_COSTS,
            points,
            ChatColor.AQUA
        ));
        
        // Jambières (slot 28)
        inv.setItem(28, createUpgradeItem(
            "Jambières",
            LEGGINGS_PROGRESSION,
            shopData.getLeggingsLevel(playerId),
            LEGGINGS_PROGRESSION.length - 1,
            LEGGINGS_COSTS,
            points,
            ChatColor.AQUA
        ));
        
        // Bottes (slot 37)
        inv.setItem(37, createUpgradeItem(
            "Bottes",
            BOOTS_PROGRESSION,
            shopData.getBootsLevel(playerId),
            BOOTS_PROGRESSION.length - 1,
            BOOTS_COSTS,
            points,
            ChatColor.AQUA
        ));
        
        // Arc (slot 30)
        inv.setItem(30, createBowUpgradeItem(shopData.getBowLevel(playerId), points));
        
        // Flèches (slot 39)
        inv.setItem(39, createArrowUpgradeItem(shopData.getArrowLevel(playerId), points));
        
        // Hache (slot 21) - affichage avec élément dans le nom
        inv.setItem(21, createAxeUpgradeItem(shopData.getAxeLevel(playerId), shopData.getAxeElement(playerId), points));
        
        // Ender Pearl (slot 32)
        inv.setItem(32, createConsumableItem(Material.ENDER_PEARL, ChatColor.DARK_PURPLE + "Ender Pearl", 2, points));
        
        // Pomme d'or (slot 23)
        inv.setItem(23, createConsumableItem(Material.GOLDEN_APPLE, ChatColor.GOLD + "Pomme d'Or", 3, points));
        
        // Items combinés (achat + upgrade)
        inv.setItem(16, createCombinedConsumableItem(Material.SNOW_BALL, "Grenade Fumigène", ChatColor.GRAY, SMOKE_PURCHASE_COST, shopData.getSmokeTier(playerId), MAX_SMOKE_TIER, SMOKE_COSTS, points));
        inv.setItem(25, createCombinedConsumableItem(Material.FIREBALL, "Cocktail Molotov", ChatColor.GOLD, MOLOTOV_PURCHASE_COST, shopData.getMolotovTier(playerId), MAX_MOLOTOV_TIER, MOLOTOV_COSTS, points));
        inv.setItem(34, createCombinedConsumableItem(Material.REDSTONE, "Mine", ChatColor.RED, MINE_PURCHASE_COST, shopData.getMineTier(playerId), MAX_MINE_TIER, MINE_COSTS, points));
        inv.setItem(43, createCombinedConsumableItem(Material.FEATHER, "Grenade Propulse", ChatColor.AQUA, REPULSIVE_PURCHASE_COST, shopData.getRepulsiveTier(playerId), MAX_REPULSIVE_TIER, REPULSIVE_COSTS, points));
        
        // Points display (slot 49)
        ItemStack pointsItem = new ItemStack(Material.GOLD_INGOT);
        ItemMeta pointsMeta = pointsItem.getItemMeta();
        pointsMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Vos Points: " + points);
        pointsItem.setItemMeta(pointsMeta);
        inv.setItem(49, pointsItem);
        
        // Ready button (slot 53)
        boolean isReady = controller != null && controller.isPlayerReady(player);
        ItemStack readyItem = new ItemStack(isReady ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK);
        ItemMeta readyMeta = readyItem.getItemMeta();
        if (isReady) {
            readyMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "✓ VOUS ÊTES PRÊT");
            readyMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Cliquez pour vous mettre",
                ChatColor.GRAY + "en mode " + ChatColor.RED + "PAS PRÊT"
            ));
        } else {
            readyMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "✗ VOUS N'ÊTES PAS PRÊT");
            readyMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Cliquez pour vous mettre",
                ChatColor.GRAY + "en mode " + ChatColor.GREEN + "PRÊT"
            ));
        }
        readyItem.setItemMeta(readyMeta);
        inv.setItem(53, readyItem);
        
        player.openInventory(inv);
    }
    
    private ItemStack createUpgradeItem(String name, Material[] progression, int currentLevel, 
                                        int maxLevel, int[] costs, int points, ChatColor color) {
        Material material = currentLevel < progression.length ? progression[currentLevel] : progression[progression.length - 1];
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color + "Améliorer: " + name);
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Niveau actuel: " + ChatColor.WHITE + currentLevel);
        
        if (currentLevel < maxLevel) {
            int cost = costs[currentLevel];
            lore.add("");
            lore.add(ChatColor.GRAY + "Coût: " + ChatColor.GOLD + cost + " points");
            lore.add(points >= cost ? ChatColor.GREEN + "✔ Cliquez pour acheter" : ChatColor.RED + "✘ Points insuffisants");
        } else {
            lore.add(ChatColor.GREEN + "✔ Niveau maximum atteint");
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createBowUpgradeItem(int currentLevel, int points) {
        ItemStack item = new ItemStack(Material.BOW);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Améliorer: Arc");
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Niveau actuel: " + ChatColor.WHITE + currentLevel);
        
        if (currentLevel < MAX_BOW_TIER) {
            int cost = BOW_COSTS[currentLevel];
            lore.add("");
            lore.add(ChatColor.GRAY + "Coût: " + ChatColor.GOLD + cost + " points");
            lore.add(points >= cost ? ChatColor.GREEN + "✔ Cliquez pour acheter" : ChatColor.RED + "✘ Points insuffisants");
        } else {
            lore.add(ChatColor.GREEN + "✔ Niveau maximum atteint");
        }
        
        meta.setLore(lore);
        if (currentLevel > 0) {
            item.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, currentLevel);
        }
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createArrowUpgradeItem(int currentLevel, int points) {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Améliorer: Flèches");
        
        List<String> lore = new ArrayList<>();
        String[] arrowTypes = {"Normales", "Effet I", "Effet II", "Effet III"};
        lore.add(ChatColor.GRAY + "Niveau actuel: " + ChatColor.WHITE + arrowTypes[Math.min(currentLevel, 3)]);
        
        if (currentLevel < MAX_ARROW_TIER) {
            int cost = ARROW_COSTS[currentLevel];
            lore.add("");
            lore.add(ChatColor.GRAY + "Coût: " + ChatColor.GOLD + cost + " points");
            lore.add(points >= cost ? ChatColor.GREEN + "✔ Cliquez pour acheter" : ChatColor.RED + "✘ Points insuffisants");
        } else {
            lore.add(ChatColor.GREEN + "✔ Niveau maximum atteint");
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createAxeUpgradeItem(int currentLevel, String currentElement, int points) {
        Material material = currentLevel < AXE_PROGRESSION.length ? AXE_PROGRESSION[currentLevel] : AXE_PROGRESSION[AXE_PROGRESSION.length - 1];
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        // Nom avec élément si disponible
        String displayName = ChatColor.DARK_RED + "Améliorer: Hache";
        if (currentElement != null && currentLevel >= 4) {
            switch (currentElement) {
                case "fire":
                    displayName = ChatColor.RED + "Améliorer: Hache de Feu";
                    break;
                case "ice":
                    displayName = ChatColor.AQUA + "Améliorer: Hache de Glace";
                    break;
                case "lightning":
                    displayName = ChatColor.YELLOW + "Améliorer: Hache de Foudre";
                    break;
            }
        }
        meta.setDisplayName(displayName);
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Niveau actuel: " + ChatColor.WHITE + currentLevel);
        
        // Info sur l'élément
        if (currentElement == null && currentLevel < 4) {
            lore.add(ChatColor.GRAY + "Élément: " + ChatColor.DARK_GRAY + "Débloquer au tier Fer");
        } else if (currentElement != null) {
            String elementDisplay = "";
            switch (currentElement) {
                case "fire":
                    elementDisplay = ChatColor.RED + "Feu";
                    break;
                case "ice":
                    elementDisplay = ChatColor.AQUA + "Glace";
                    break;
                case "lightning":
                    elementDisplay = ChatColor.YELLOW + "Foudre";
                    break;
            }
            lore.add(ChatColor.GRAY + "Élément: " + elementDisplay);
        }
        
        if (currentLevel < AXE_PROGRESSION.length - 1) {
            int cost = AXE_COSTS[currentLevel];
            lore.add("");
            lore.add(ChatColor.GRAY + "Coût: " + ChatColor.GOLD + cost + " points");
            lore.add(points >= cost ? ChatColor.GREEN + "✔ Cliquez pour acheter" : ChatColor.RED + "✘ Points insuffisants");
        } else {
            lore.add(ChatColor.GREEN + "✔ Niveau maximum atteint");
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createConsumableItem(Material material, String name, int cost, int points) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Coût: " + ChatColor.GOLD + cost + " points");
        lore.add(points >= cost ? ChatColor.GREEN + "✔ Cliquez pour acheter" : ChatColor.RED + "✘ Points insuffisants");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createCombinedConsumableItem(Material material, String name, ChatColor nameColor, int purchaseCost, int currentTier, int maxTier, int[] upgradeCosts, int points) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(nameColor + name);
        
        List<String> lore = new ArrayList<>();
        
        // Info niveau actuel
        if (currentTier > 0) {
            lore.add(ChatColor.GRAY + "Niveau actuel: " + ChatColor.WHITE + currentTier);
        } else {
            lore.add(ChatColor.GRAY + "Niveau: " + ChatColor.DARK_GRAY + "Aucun");
        }
        
        lore.add("");
        
        // Clic gauche = acheter
        lore.add(ChatColor.YELLOW + "◀ Clic gauche: " + ChatColor.WHITE + "Acheter");
        if (currentTier == 0) {
            lore.add(ChatColor.RED + "✘ Nécessite niveau 1+");
            lore.add(ChatColor.GRAY + "Améliorez d'abord (clic droit)");
        } else {
            lore.add(ChatColor.GRAY + "Coût: " + ChatColor.GOLD + purchaseCost + " points");
            lore.add(points >= purchaseCost ? ChatColor.GREEN + "✔ Points suffisants" : ChatColor.RED + "✘ Points insuffisants");
        }
        
        lore.add("");
        
        // Clic droit = améliorer
        lore.add(ChatColor.YELLOW + "▶ Clic droit: " + ChatColor.WHITE + "Améliorer");
        if (currentTier < maxTier) {
            int upgradeCost = upgradeCosts[currentTier];
            lore.add(ChatColor.GRAY + "Coût: " + ChatColor.GOLD + upgradeCost + " points");
            lore.add(points >= upgradeCost ? ChatColor.GREEN + "✔ Points suffisants" : ChatColor.RED + "✘ Points insuffisants");
        } else if (maxTier > 0) {
            lore.add(ChatColor.GREEN + "✔ Niveau maximum atteint");
        } else {
            lore.add(ChatColor.GRAY + "Pas d'amélioration disponible");
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getInventory().getName().equals(INVENTORY_NAME)) return;
        
        event.setCancelled(true);
        
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        UUID playerId = player.getUniqueId();
        boolean isLeftClick = event.isLeftClick();
        boolean isRightClick = event.isRightClick();
        
        switch (slot) {
            case 12: upgradeItem(player, "Épée", shopData.getSwordLevel(playerId), SWORD_PROGRESSION.length - 1, SWORD_COSTS, (lvl) -> shopData.setSwordLevel(playerId, lvl)); break;
            case 10: upgradeItem(player, "Casque", shopData.getHelmetLevel(playerId), HELMET_PROGRESSION.length - 1, HELMET_COSTS, (lvl) -> shopData.setHelmetLevel(playerId, lvl)); break;
            case 19: upgradeItem(player, "Plastron", shopData.getChestplateLevel(playerId), CHESTPLATE_PROGRESSION.length - 1, CHESTPLATE_COSTS, (lvl) -> shopData.setChestplateLevel(playerId, lvl)); break;
            case 28: upgradeItem(player, "Jambières", shopData.getLeggingsLevel(playerId), LEGGINGS_PROGRESSION.length - 1, LEGGINGS_COSTS, (lvl) -> shopData.setLeggingsLevel(playerId, lvl)); break;
            case 37: upgradeItem(player, "Bottes", shopData.getBootsLevel(playerId), BOOTS_PROGRESSION.length - 1, BOOTS_COSTS, (lvl) -> shopData.setBootsLevel(playerId, lvl)); break;
            case 30: upgradeItem(player, "Arc", shopData.getBowLevel(playerId), MAX_BOW_TIER, BOW_COSTS, (lvl) -> shopData.setBowLevel(playerId, lvl)); break;
            case 39: upgradeItem(player, "Flèches", shopData.getArrowLevel(playerId), MAX_ARROW_TIER, ARROW_COSTS, (lvl) -> shopData.setArrowLevel(playerId, lvl)); break;
            case 21: upgradeAxe(player); break;
            case 32: purchaseConsumable(player, Material.ENDER_PEARL, ChatColor.DARK_PURPLE + "Ender Pearl", 2); break;
            case 23: purchaseConsumable(player, Material.GOLDEN_APPLE, ChatColor.GOLD + "Pomme d'Or", 3); break;
            case 16:
                if (isLeftClick) {
                    int smokeTier = shopData.getSmokeTier(playerId);
                    if (smokeTier == 0) {
                        player.sendMessage(ChatColor.RED + "Vous devez améliorer au niveau 1 avant d'acheter !");
                    } else {
                        purchaseConsumable(player, Material.SNOW_BALL, ChatColor.GRAY + "Grenade Fumigène", SMOKE_PURCHASE_COST);
                    }
                } else if (isRightClick) {
                    upgradeItem(player, "Grenade Fumigène", shopData.getSmokeTier(playerId), MAX_SMOKE_TIER, SMOKE_COSTS, (lvl) -> shopData.setSmokeTier(playerId, lvl));
                }
                break;
            case 25:
                if (isLeftClick) {
                    int molotovTier = shopData.getMolotovTier(playerId);
                    if (molotovTier == 0) {
                        player.sendMessage(ChatColor.RED + "Vous devez améliorer au niveau 1 avant d'acheter !");
                    } else {
                        purchaseConsumable(player, Material.FIREBALL, ChatColor.GOLD + "Cocktail Molotov", MOLOTOV_PURCHASE_COST);
                    }
                } else if (isRightClick) {
                    upgradeItem(player, "Cocktail Molotov", shopData.getMolotovTier(playerId), MAX_MOLOTOV_TIER, MOLOTOV_COSTS, (lvl) -> shopData.setMolotovTier(playerId, lvl));
                }
                break;
            case 34:
                if (isLeftClick) {
                    int mineTier = shopData.getMineTier(playerId);
                    if (mineTier == 0) {
                        player.sendMessage(ChatColor.RED + "Vous devez améliorer au niveau 1 avant d'acheter !");
                    } else {
                        // Vérifier combien de mines sont déjà achetées (max 1)
                        int mineCount = countConsumablesByMaterial(playerId, Material.REDSTONE);
                        if (mineCount >= 1) {
                            player.sendMessage(ChatColor.RED + "Vous avez déjà acheté le maximum de mines pour ce round (1 max) !");
                        } else {
                            purchaseConsumable(player, Material.REDSTONE, ChatColor.RED + "Mine", MINE_PURCHASE_COST);
                        }
                    }
                } else if (isRightClick) {
                    upgradeItem(player, "Mine", shopData.getMineTier(playerId), MAX_MINE_TIER, MINE_COSTS, (lvl) -> shopData.setMineTier(playerId, lvl));
                }
                break;
            case 43:
                if (isLeftClick) {
                    int repulsiveTier = shopData.getRepulsiveTier(playerId);
                    if (repulsiveTier == 0) {
                        player.sendMessage(ChatColor.RED + "Vous devez améliorer au niveau 1 avant d'acheter !");
                    } else {
                        // Limite selon le tier: tier 1 = 1 max, tier 2 = 2 max
                        int maxAllowed = repulsiveTier;
                        int repulsiveCount = countConsumablesByMaterial(playerId, Material.FEATHER);
                        if (repulsiveCount >= maxAllowed) {
                            player.sendMessage(ChatColor.RED + "Vous avez déjà acheté le maximum de grenades propulses pour ce round (" + maxAllowed + " max) !");
                        } else {
                            purchaseConsumable(player, Material.FEATHER, ChatColor.AQUA + "Grenade Propulse", REPULSIVE_PURCHASE_COST);
                        }
                    }
                } else if (isRightClick) {
                    upgradeItem(player, "Grenade Propulse", shopData.getRepulsiveTier(playerId), MAX_REPULSIVE_TIER, REPULSIVE_COSTS, (lvl) -> shopData.setRepulsiveTier(playerId, lvl));
                }
                break;
            case 53:
                if (currentController != null) {
                    currentController.toggleReady(player);
                    player.closeInventory();
                    Bukkit.getScheduler().runTaskLater(Nerysia.getInstance(), () -> open(player, currentController), 1L);
                }
                break;
        }
    }
    
    @FunctionalInterface
    private interface LevelSetter {
        void setLevel(int level);
    }
    
    private void upgradeItem(Player player, String name, int currentLevel, int maxLevel, int[] costs, LevelSetter setter) {
        if (currentLevel >= maxLevel) {
            player.sendMessage(ChatColor.RED + "Vous avez déjà le niveau maximum de " + name + " !");
            return;
        }
        
        int cost = costs[currentLevel];
        int points = pointsManager.getPoints(player);
        
        if (points < cost) {
            player.sendMessage(ChatColor.RED + "Vous n'avez pas assez de points !");
            return;
        }
        
        pointsManager.removePoints(player, cost);
        setter.setLevel(currentLevel + 1);
        player.sendMessage(ChatColor.GREEN + name + " amélioré(e) !");
        
        // Mettre à jour le scoreboard
        if (currentController != null) {
            currentController.updatePlayerScoreboard(player);
        }
        
        open(player, currentController);
    }
    
    private void upgradeAxe(Player player) {
        UUID playerId = player.getUniqueId();
        int currentLevel = shopData.getAxeLevel(playerId);
        int maxLevel = AXE_PROGRESSION.length - 1;
        
        if (currentLevel >= maxLevel) {
            player.sendMessage(ChatColor.RED + "Vous avez déjà le niveau maximum de Hache !");
            return;
        }
        
        int cost = AXE_COSTS[currentLevel];
        int points = pointsManager.getPoints(player);
        
        if (points < cost) {
            player.sendMessage(ChatColor.RED + "Vous n'avez pas assez de points !");
            return;
        }
        
        pointsManager.removePoints(player, cost);
        int newLevel = currentLevel + 1;
        shopData.setAxeLevel(playerId, newLevel);
        
        // Si on atteint le tier 4 (fer) et qu'on n'a pas encore d'élément, en attribuer un aléatoirement
        if (newLevel == 4 && shopData.getAxeElement(playerId) == null) {
            String[] elements = {"fire", "ice", "lightning"};
            String randomElement = elements[RANDOM.nextInt(elements.length)];
            shopData.setAxeElement(playerId, randomElement);
            
            String elementName = randomElement.equals("fire") ? ChatColor.RED + "FEU" : 
                                randomElement.equals("ice") ? ChatColor.AQUA + "GLACE" : 
                                ChatColor.YELLOW + "FOUDRE";
            player.sendMessage(ChatColor.GREEN + "Hache améliorée !");
            player.sendMessage(ChatColor.GOLD + "Élément attribué : " + elementName);
        } else {
            player.sendMessage(ChatColor.GREEN + "Hache améliorée !");
        }
        
        // Mettre à jour le scoreboard
        if (currentController != null) {
            currentController.updatePlayerScoreboard(player);
        }
        
        open(player, currentController);
    }
    
    private void purchaseConsumable(Player player, Material material, String name, int cost) {
        int points = pointsManager.getPoints(player);
        
        if (points < cost) {
            player.sendMessage(ChatColor.RED + "Vous n'avez pas assez de points !");
            return;
        }
        
        pointsManager.removePoints(player, cost);
        shopData.addConsumableItem(player.getUniqueId(), material, name);
        player.sendMessage(ChatColor.GREEN + "Acheté: " + name);
        player.sendMessage(ChatColor.GRAY + "L'item sera disponible au prochain round !");
        
        // Mettre à jour le scoreboard
        if (currentController != null) {
            currentController.updatePlayerScoreboard(player);
        }
        
        open(player, currentController);
    }
    
    /**
     * Compte le nombre d'items consommables d'un type spécifique
     */
    private int countConsumablesByMaterial(UUID playerId, Material material) {
        List<FocusShopData.ConsumableItem> consumables = shopData.getConsumableItems(playerId);
        int count = 0;
        for (FocusShopData.ConsumableItem item : consumables) {
            if (item.material == material) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Donne l'équipement au joueur basé sur ses achats
     */
    public void givePlayerEquipment(Player player, boolean inGame) {
        player.getInventory().clear();
        UUID playerId = player.getUniqueId();
        
        // Si on n'est PAS en jeu (au spawn_minijeux), donner SEULEMENT l'item du shop
        if (!inGame) {
            ItemStack shopItem = new ItemStack(Material.CHEST);
            ItemMeta shopMeta = shopItem.getItemMeta();
            shopMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Shop Focus");
            shopMeta.setLore(Arrays.asList(ChatColor.GRAY + "Clic droit pour ouvrir"));
            shopItem.setItemMeta(shopMeta);
            player.getInventory().setItem(4, shopItem); // Slot 4 (5ème slot)
            return; // Ne pas donner d'équipement
        }
        
        // === À partir d'ici, uniquement si inGame = true ===
        
        // Épée (toujours donner avec enchantements selon niveau - exactement comme ancien code)
        int swordLevel = shopData.getSwordLevel(playerId);
        if (swordLevel < SWORD_PROGRESSION.length) {
            ItemStack sword = new ItemStack(SWORD_PROGRESSION[swordLevel]);
            
            // Enchantements selon le niveau (exactement comme ancien code)
            if (swordLevel == 3) {
                // GOLD_SWORD - Sharpness I
                sword.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
            } else if (swordLevel == 4) {
                // IRON_SWORD - Sharpness II
                sword.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 2);
            } else if (swordLevel == 5) {
                // DIAMOND_SWORD - Sharpness II
                sword.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 2);
            }
            
            // Rendre l'épée incassable
            ItemMeta swordMeta = sword.getItemMeta();
            swordMeta.spigot().setUnbreakable(true);
            sword.setItemMeta(swordMeta);
            
            player.getInventory().addItem(sword);
        }
        
        // Armure (pièces indépendantes)
        ItemStack[] armor = new ItemStack[4];
        
        int helmetLevel = shopData.getHelmetLevel(playerId);
        if (helmetLevel > 0 && helmetLevel <= HELMET_PROGRESSION.length) {
            ItemStack helmet = new ItemStack(HELMET_PROGRESSION[helmetLevel - 1]);
            ItemMeta helmetMeta = helmet.getItemMeta();
            
            // Incassable
            helmetMeta.spigot().setUnbreakable(true);
            helmet.setItemMeta(helmetMeta);
            
            // Enchantements selon le niveau (PAS de Thorns sur le casque)
            if (helmetLevel == 3) { // Or
                helmet.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
            } else if (helmetLevel == 4) { // Fer
                helmet.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
            } else if (helmetLevel == 5) { // Diamant
                helmet.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
            }
            
            armor[3] = helmet;
            
            // +2 cœurs permanents par niveau
            double maxHealth = 20.0 + (helmetLevel * 4.0); // 2 cœurs = 4 HP
            player.setMaxHealth(maxHealth);
            player.setHealth(maxHealth);
        }
        
        int chestLevel = shopData.getChestplateLevel(playerId);
        if (chestLevel > 0 && chestLevel <= CHESTPLATE_PROGRESSION.length) {
            ItemStack chestplate = new ItemStack(CHESTPLATE_PROGRESSION[chestLevel - 1]);
            ItemMeta chestMeta = chestplate.getItemMeta();
            
            // Incassable
            chestMeta.spigot().setUnbreakable(true);
            chestplate.setItemMeta(chestMeta);
            
            // Enchantements selon le niveau
            if (chestLevel == 3) { // Or
                chestplate.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
                chestplate.addUnsafeEnchantment(Enchantment.THORNS, 2);
            } else if (chestLevel == 4) { // Fer
                chestplate.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
                chestplate.addUnsafeEnchantment(Enchantment.THORNS, 2);
            } else if (chestLevel == 5) { // Diamant
                chestplate.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
                chestplate.addUnsafeEnchantment(Enchantment.THORNS, 2);
            }
            
            armor[2] = chestplate;
        }
        
        int legsLevel = shopData.getLeggingsLevel(playerId);
        if (legsLevel > 0 && legsLevel <= LEGGINGS_PROGRESSION.length) {
            ItemStack leggings = new ItemStack(LEGGINGS_PROGRESSION[legsLevel - 1]);
            ItemMeta legsMeta = leggings.getItemMeta();
            
            // Incassable
            legsMeta.spigot().setUnbreakable(true);
            leggings.setItemMeta(legsMeta);
            
            // Enchantements selon le niveau (PAS de Thorns sur les jambières)
            if (legsLevel == 3) { // Or
                leggings.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
            } else if (legsLevel == 4) { // Fer
                leggings.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
            } else if (legsLevel == 5) { // Diamant
                leggings.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
            }
            
            armor[1] = leggings;
            
            // Effet de vitesse permanent (Speed 1, Speed 2, Speed 3, etc.)
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, legsLevel - 1, false, false));
        }
        
        int bootsLevel = shopData.getBootsLevel(playerId);
        if (bootsLevel > 0 && bootsLevel <= BOOTS_PROGRESSION.length) {
            ItemStack boots = new ItemStack(BOOTS_PROGRESSION[bootsLevel - 1]);
            ItemMeta bootsMeta = boots.getItemMeta();
            
            // Incassable
            bootsMeta.spigot().setUnbreakable(true);
            boots.setItemMeta(bootsMeta);
            
            // Enchantements selon le niveau (PAS de Thorns sur les bottes)
            if (bootsLevel == 3) { // Or
                boots.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
            } else if (bootsLevel == 4) { // Fer
                boots.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
            } else if (bootsLevel == 5) { // Diamant
                boots.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
            }
            
            armor[0] = boots;
            
            // Effet de jump boost permanent
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, bootsLevel - 1, false, false));
        }
        
        player.getInventory().setArmorContents(armor);
        
        // Arc et flèches
        int bowLevel = shopData.getBowLevel(playerId);
        if (bowLevel > 0) {
            ItemStack bow = new ItemStack(Material.BOW);
            ItemMeta bowMeta = bow.getItemMeta();
            
            // Enchantements selon le niveau
            if (bowLevel == 2) { // Tier 2
                bow.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 1); // Power 1
            } else if (bowLevel == 3) { // Tier 3
                bow.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 2); // Power 2
            } else if (bowLevel >= 4) { // Tier 4+
                bow.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 2); // Power 2
                bow.addUnsafeEnchantment(Enchantment.ARROW_KNOCKBACK, 2); // Punch 2
            }
            
            // Incassable
            bowMeta.spigot().setUnbreakable(true);
            bow.setItemMeta(bowMeta);
            
            player.getInventory().addItem(bow);
            
            // Flèches selon le niveau
            int arrowLevel = shopData.getArrowLevel(playerId);
            if (arrowLevel > 0) {
                int arrowCount = 16; // Tier 1
                if (arrowLevel == 2) {
                    arrowCount = 32; // Tier 2
                } else if (arrowLevel >= 3) {
                    arrowCount = 64; // Tier 3+
                }
                ItemStack arrows = new ItemStack(Material.ARROW, arrowCount);
                player.getInventory().addItem(arrows);
            }
        }
        
        // Hache avec élément
        int axeLevel = shopData.getAxeLevel(playerId);
        if (axeLevel > 0 && axeLevel <= AXE_PROGRESSION.length) {
            ItemStack axe = new ItemStack(AXE_PROGRESSION[axeLevel - 1]);
            ItemMeta axeMeta = axe.getItemMeta();
            
            // Enchantements de base selon le niveau (comme l'ancien code)
            if (axeLevel == 2) { // Pierre
                axe.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1); // Sharpness 1
            } else if (axeLevel == 3) { // Or
                axe.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 2); // Sharpness 2
            } else if (axeLevel == 4) { // Fer - élément disponible
                axe.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 2); // Sharpness 2
            } else if (axeLevel == 5) { // Diamant
                axe.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3); // Sharpness 3
            }
            
            // Élément (disponible à partir du niveau Fer = 4)
            String element = shopData.getAxeElement(playerId);
            if (element != null && axeLevel >= 4) {
                switch (element) {
                    case "fire":
                        axeMeta.setDisplayName(ChatColor.RED + "Hache de Feu");
                        axeMeta.setLore(Arrays.asList(ChatColor.GRAY + "Donne Fire Aspect"));
                        axe.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);
                        break;
                    case "ice":
                        axeMeta.setDisplayName(ChatColor.AQUA + "Hache de Glace");
                        axeMeta.setLore(Arrays.asList(ChatColor.GRAY + "Ralentit l'ennemi"));
                        break;
                    case "lightning":
                        axeMeta.setDisplayName(ChatColor.YELLOW + "Hache de Foudre");
                        axeMeta.setLore(Arrays.asList(ChatColor.GRAY + "Donne Speed"));
                        break;
                }
            }
            
            // Incassable
            axeMeta.spigot().setUnbreakable(true);
            axe.setItemMeta(axeMeta);
            
            player.getInventory().addItem(axe);
        }
        
        // Items consommables (seulement en jeu)
        if (inGame) {
            List<FocusShopData.ConsumableItem> consumables = shopData.getConsumableItems(playerId);
            for (FocusShopData.ConsumableItem consumable : consumables) {
                ItemStack item = new ItemStack(consumable.material);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(consumable.name);
                item.setItemMeta(meta);
                player.getInventory().addItem(item);
            }
        }
    }
}
