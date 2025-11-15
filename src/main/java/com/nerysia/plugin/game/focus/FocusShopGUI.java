package com.nerysia.plugin.game.focus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
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
import java.util.UUID;

/**
 * Gère le shop Focus - reproduction exacte de FocusShopCommand.java
 */
public class FocusShopGUI implements Listener {
    
    private static final String INVENTORY_NAME = ChatColor.DARK_PURPLE + "Shop Focus";
    
    // Progressions d'items
    private static final Material[] SWORD_PROGRESSION = {
        Material.STICK, Material.WOOD_SWORD, Material.STONE_SWORD, 
        Material.GOLD_SWORD, Material.IRON_SWORD, Material.DIAMOND_SWORD
    };
    
    private static final Material[] HELMET_PROGRESSION = {
        Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET, Material.GOLD_HELMET,
        Material.IRON_HELMET, Material.DIAMOND_HELMET
    };
    
    private static final Material[] CHESTPLATE_PROGRESSION = {
        Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.GOLD_CHESTPLATE,
        Material.IRON_CHESTPLATE, Material.DIAMOND_CHESTPLATE
    };
    
    private static final Material[] LEGGINGS_PROGRESSION = {
        Material.LEATHER_LEGGINGS, Material.CHAINMAIL_LEGGINGS, Material.GOLD_LEGGINGS,
        Material.IRON_LEGGINGS, Material.DIAMOND_LEGGINGS
    };
    
    private static final Material[] BOOTS_PROGRESSION = {
        Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS, Material.GOLD_BOOTS,
        Material.IRON_BOOTS, Material.DIAMOND_BOOTS
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
    private static final int[] CHESTPLATE_COSTS = {1, 2, 3, 4, 5};
    private static final int[] LEGGINGS_COSTS = {1, 2, 3, 4, 5};
    private static final int[] BOOTS_COSTS = {1, 2, 3, 4};
    private static final int[] BOW_COSTS = {1, 2, 3, 4, 5, 6};
    private static final int[] ARROW_COSTS = {1, 2, 3};
    private static final int[] AXE_COSTS = {1, 2, 3, 4, 5};
    private static final int[] SMOKE_COSTS = {1, 2, 3};
    private static final int[] MOLOTOV_COSTS = {1, 2, 3};
    
    private static final int MAX_SMOKE_TIER = 3;
    private static final int MAX_MOLOTOV_TIER = 3;
    
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
        
        // Épée (slot 10)
        inv.setItem(10, createUpgradeItem(
            "Épée",
            SWORD_PROGRESSION,
            shopData.getSwordLevel(playerId),
            SWORD_PROGRESSION.length - 1,
            SWORD_COSTS,
            points,
            ChatColor.YELLOW
        ));
        
        // Casque (slot 11)
        inv.setItem(11, createUpgradeItem(
            "Casque",
            HELMET_PROGRESSION,
            shopData.getHelmetLevel(playerId),
            HELMET_PROGRESSION.length - 1,
            HELMET_COSTS,
            points,
            ChatColor.AQUA
        ));
        
        // Plastron (slot 12)
        inv.setItem(12, createUpgradeItem(
            "Plastron",
            CHESTPLATE_PROGRESSION,
            shopData.getChestplateLevel(playerId),
            CHESTPLATE_PROGRESSION.length - 1,
            CHESTPLATE_COSTS,
            points,
            ChatColor.AQUA
        ));
        
        // Jambières (slot 13)
        inv.setItem(13, createUpgradeItem(
            "Jambières",
            LEGGINGS_PROGRESSION,
            shopData.getLeggingsLevel(playerId),
            LEGGINGS_PROGRESSION.length - 1,
            LEGGINGS_COSTS,
            points,
            ChatColor.AQUA
        ));
        
        // Bottes (slot 14)
        inv.setItem(14, createUpgradeItem(
            "Bottes",
            BOOTS_PROGRESSION,
            shopData.getBootsLevel(playerId),
            BOOTS_PROGRESSION.length - 1,
            BOOTS_COSTS,
            points,
            ChatColor.AQUA
        ));
        
        // Arc (slot 15)
        inv.setItem(15, createBowUpgradeItem(shopData.getBowLevel(playerId), points));
        
        // Flèches (slot 16)
        inv.setItem(16, createArrowUpgradeItem(shopData.getArrowLevel(playerId), points));
        
        // Hache (slot 28)
        inv.setItem(28, createAxeUpgradeItem(shopData.getAxeLevel(playerId), points));
        
        // Élément de hache (slot 29 si hache achetée)
        if (shopData.getAxeLevel(playerId) > 0) {
            inv.setItem(29, createAxeElementItem(shopData.getAxeElement(playerId)));
        }
        
        // Ender Pearl (slot 30)
        inv.setItem(30, createConsumableItem(Material.ENDER_PEARL, ChatColor.DARK_PURPLE + "Ender Pearl", 2, points));
        
        // Pomme d'or (slot 31)
        inv.setItem(31, createConsumableItem(Material.GOLDEN_APPLE, ChatColor.GOLD + "Pomme d'Or", 3, points));
        
        // Smoke tier upgrade (slot 32)
        inv.setItem(32, createSmokeUpgradeItem(shopData.getSmokeTier(playerId), points));
        
        // Molotov tier upgrade (slot 33)
        inv.setItem(33, createMolotovUpgradeItem(shopData.getMolotovTier(playerId), points));
        
        // Utilitaires
        inv.setItem(37, createConsumableItem(Material.WEB, ChatColor.GRAY + "Grenade Fumigène", 2, points));
        inv.setItem(38, createConsumableItem(Material.FIREBALL, ChatColor.GOLD + "Cocktail Molotov", 3, points));
        inv.setItem(39, createConsumableItem(Material.STONE_PLATE, ChatColor.RED + "Mine", 4, points));
        inv.setItem(40, createConsumableItem(Material.FEATHER, ChatColor.AQUA + "Grenade Propulse", 5, points));
        
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
    
    private ItemStack createAxeUpgradeItem(int currentLevel, int points) {
        Material material = currentLevel < AXE_PROGRESSION.length ? AXE_PROGRESSION[currentLevel] : AXE_PROGRESSION[AXE_PROGRESSION.length - 1];
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_RED + "Améliorer: Hache");
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Niveau actuel: " + ChatColor.WHITE + currentLevel);
        
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
    
    private ItemStack createAxeElementItem(String currentElement) {
        ItemStack item = new ItemStack(Material.BLAZE_POWDER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Élément de Hache");
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Actuel: " + ChatColor.WHITE + (currentElement == null ? "Aucun" : currentElement));
        lore.add("");
        lore.add(ChatColor.RED + "Feu " + (currentElement != null && currentElement.equals("fire") ? "✔" : ""));
        lore.add(ChatColor.AQUA + "Glace " + (currentElement != null && currentElement.equals("ice") ? "✔" : ""));
        lore.add(ChatColor.YELLOW + "Foudre " + (currentElement != null && currentElement.equals("lightning") ? "✔" : ""));
        lore.add("");
        lore.add(ChatColor.GRAY + "Clic gauche: Feu");
        lore.add(ChatColor.GRAY + "Clic droit: Glace");
        lore.add(ChatColor.GRAY + "Shift-Clic: Foudre");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createSmokeUpgradeItem(int currentTier, int points) {
        ItemStack item = new ItemStack(Material.WEB);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GRAY + "Améliorer: Grenade Fumigène");
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Niveau actuel: " + ChatColor.WHITE + currentTier);
        
        if (currentTier < MAX_SMOKE_TIER) {
            int cost = SMOKE_COSTS[currentTier];
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
    
    private ItemStack createMolotovUpgradeItem(int currentTier, int points) {
        ItemStack item = new ItemStack(Material.FIREBALL);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Améliorer: Cocktail Molotov");
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Niveau actuel: " + ChatColor.WHITE + currentTier);
        
        if (currentTier < MAX_MOLOTOV_TIER) {
            int cost = MOLOTOV_COSTS[currentTier];
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
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getInventory().getName().equals(INVENTORY_NAME)) return;
        
        event.setCancelled(true);
        
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        UUID playerId = player.getUniqueId();
        int points = pointsManager.getPoints(player);
        ClickType clickType = event.getClick();
        
        switch (slot) {
            case 10: upgradeItem(player, "Épée", shopData.getSwordLevel(playerId), SWORD_PROGRESSION.length - 1, SWORD_COSTS, (lvl) -> shopData.setSwordLevel(playerId, lvl)); break;
            case 11: upgradeItem(player, "Casque", shopData.getHelmetLevel(playerId), HELMET_PROGRESSION.length - 1, HELMET_COSTS, (lvl) -> shopData.setHelmetLevel(playerId, lvl)); break;
            case 12: upgradeItem(player, "Plastron", shopData.getChestplateLevel(playerId), CHESTPLATE_PROGRESSION.length - 1, CHESTPLATE_COSTS, (lvl) -> shopData.setChestplateLevel(playerId, lvl)); break;
            case 13: upgradeItem(player, "Jambières", shopData.getLeggingsLevel(playerId), LEGGINGS_PROGRESSION.length - 1, LEGGINGS_COSTS, (lvl) -> shopData.setLeggingsLevel(playerId, lvl)); break;
            case 14: upgradeItem(player, "Bottes", shopData.getBootsLevel(playerId), BOOTS_PROGRESSION.length - 1, BOOTS_COSTS, (lvl) -> shopData.setBootsLevel(playerId, lvl)); break;
            case 15: upgradeItem(player, "Arc", shopData.getBowLevel(playerId), MAX_BOW_TIER, BOW_COSTS, (lvl) -> shopData.setBowLevel(playerId, lvl)); break;
            case 16: upgradeItem(player, "Flèches", shopData.getArrowLevel(playerId), MAX_ARROW_TIER, ARROW_COSTS, (lvl) -> shopData.setArrowLevel(playerId, lvl)); break;
            case 28: upgradeItem(player, "Hache", shopData.getAxeLevel(playerId), AXE_PROGRESSION.length - 1, AXE_COSTS, (lvl) -> shopData.setAxeLevel(playerId, lvl)); break;
            case 29: handleAxeElement(player, clickType); break;
            case 30: purchaseConsumable(player, Material.ENDER_PEARL, ChatColor.DARK_PURPLE + "Ender Pearl", 2); break;
            case 31: purchaseConsumable(player, Material.GOLDEN_APPLE, ChatColor.GOLD + "Pomme d'Or", 3); break;
            case 32: upgradeItem(player, "Grenade Fumigène", shopData.getSmokeTier(playerId), MAX_SMOKE_TIER, SMOKE_COSTS, (lvl) -> shopData.setSmokeTier(playerId, lvl)); break;
            case 33: upgradeItem(player, "Cocktail Molotov", shopData.getMolotovTier(playerId), MAX_MOLOTOV_TIER, MOLOTOV_COSTS, (lvl) -> shopData.setMolotovTier(playerId, lvl)); break;
            case 37: purchaseConsumable(player, Material.WEB, ChatColor.GRAY + "Grenade Fumigène", 2); break;
            case 38: purchaseConsumable(player, Material.FIREBALL, ChatColor.GOLD + "Cocktail Molotov", 3); break;
            case 39: purchaseConsumable(player, Material.STONE_PLATE, ChatColor.RED + "Mine", 4); break;
            case 40: purchaseConsumable(player, Material.FEATHER, ChatColor.AQUA + "Grenade Propulse", 5); break;
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
        open(player, currentController);
    }
    
    private void handleAxeElement(Player player, ClickType clickType) {
        UUID playerId = player.getUniqueId();
        
        if (shopData.getAxeLevel(playerId) == 0) {
            player.sendMessage(ChatColor.RED + "Vous devez d'abord acheter une hache !");
            return;
        }
        
        String element;
        if (clickType == ClickType.LEFT) {
            element = "fire";
            player.sendMessage(ChatColor.RED + "Élément FEU sélectionné !");
        } else if (clickType == ClickType.RIGHT) {
            element = "ice";
            player.sendMessage(ChatColor.AQUA + "Élément GLACE sélectionné !");
        } else if (clickType == ClickType.SHIFT_LEFT || clickType == ClickType.SHIFT_RIGHT) {
            element = "lightning";
            player.sendMessage(ChatColor.YELLOW + "Élément FOUDRE sélectionné !");
        } else {
            return;
        }
        
        shopData.setAxeElement(playerId, element);
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
        open(player, currentController);
    }
    
    /**
     * Donne l'équipement au joueur basé sur ses achats
     */
    public void givePlayerEquipment(Player player, boolean inGame) {
        player.getInventory().clear();
        UUID playerId = player.getUniqueId();
        
        // Épée (toujours donner)
        int swordLevel = shopData.getSwordLevel(playerId);
        if (swordLevel < SWORD_PROGRESSION.length) {
            player.getInventory().addItem(new ItemStack(SWORD_PROGRESSION[swordLevel]));
        }
        
        // Armure (pièces indépendantes)
        ItemStack[] armor = new ItemStack[4];
        
        int helmetLevel = shopData.getHelmetLevel(playerId);
        if (helmetLevel > 0 && helmetLevel <= HELMET_PROGRESSION.length) {
            armor[3] = new ItemStack(HELMET_PROGRESSION[helmetLevel - 1]);
        }
        
        int chestLevel = shopData.getChestplateLevel(playerId);
        if (chestLevel > 0 && chestLevel <= CHESTPLATE_PROGRESSION.length) {
            armor[2] = new ItemStack(CHESTPLATE_PROGRESSION[chestLevel - 1]);
        }
        
        int legsLevel = shopData.getLeggingsLevel(playerId);
        if (legsLevel > 0 && legsLevel <= LEGGINGS_PROGRESSION.length) {
            armor[1] = new ItemStack(LEGGINGS_PROGRESSION[legsLevel - 1]);
        }
        
        int bootsLevel = shopData.getBootsLevel(playerId);
        if (bootsLevel > 0 && bootsLevel <= BOOTS_PROGRESSION.length) {
            armor[0] = new ItemStack(BOOTS_PROGRESSION[bootsLevel - 1]);
        }
        
        player.getInventory().setArmorContents(armor);
        
        // Arc et flèches
        int bowLevel = shopData.getBowLevel(playerId);
        if (bowLevel > 0) {
            ItemStack bow = new ItemStack(Material.BOW);
            if (bowLevel > 1) {
                bow.addEnchantment(Enchantment.ARROW_DAMAGE, bowLevel - 1);
            }
            player.getInventory().addItem(bow);
            
            // Flèches avec effets
            int arrowLevel = shopData.getArrowLevel(playerId);
            ItemStack arrows = new ItemStack(Material.ARROW, 64);
            player.getInventory().addItem(arrows);
        }
        
        // Hache avec élément
        int axeLevel = shopData.getAxeLevel(playerId);
        if (axeLevel > 0 && axeLevel <= AXE_PROGRESSION.length) {
            ItemStack axe = new ItemStack(AXE_PROGRESSION[axeLevel - 1]);
            String element = shopData.getAxeElement(playerId);
            
            if (element != null) {
                ItemMeta meta = axe.getItemMeta();
                switch (element) {
                    case "fire":
                        meta.setDisplayName(ChatColor.RED + "Hache de Feu");
                        meta.setLore(Arrays.asList(ChatColor.GRAY + "Donne Fire Aspect"));
                        axe.addEnchantment(Enchantment.FIRE_ASPECT, 1);
                        break;
                    case "ice":
                        meta.setDisplayName(ChatColor.AQUA + "Hache de Glace");
                        meta.setLore(Arrays.asList(ChatColor.GRAY + "Ralentit l'ennemi"));
                        break;
                    case "lightning":
                        meta.setDisplayName(ChatColor.YELLOW + "Hache de Foudre");
                        meta.setLore(Arrays.asList(ChatColor.GRAY + "Donne Speed"));
                        break;
                }
                axe.setItemMeta(meta);
            }
            
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
        
        // Item de shop (slot 5)
        ItemStack shopItem = new ItemStack(Material.CHEST);
        ItemMeta shopMeta = shopItem.getItemMeta();
        shopMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Shop Focus");
        shopMeta.setLore(Arrays.asList(ChatColor.GRAY + "Clic droit pour ouvrir"));
        shopItem.setItemMeta(shopMeta);
        player.getInventory().setItem(5, shopItem);
    }
}
