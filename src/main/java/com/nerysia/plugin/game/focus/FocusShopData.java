package com.nerysia.plugin.game.focus;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Stocke les niveaux d'upgrade et les items consommables de chaque joueur dans le shop Focus
 */
public class FocusShopData {
    
    private final Map<UUID, Integer> swordLevels;
    private final Map<UUID, Integer> helmetLevels;
    private final Map<UUID, Integer> chestplateLevels;
    private final Map<UUID, Integer> leggingsLevels;
    private final Map<UUID, Integer> bootsLevels;
    private final Map<UUID, Integer> bowLevels;
    private final Map<UUID, Integer> arrowLevels;
    private final Map<UUID, Integer> axeLevels;
    private final Map<UUID, String> axeElements; // "fire", "ice", "lightning"
    private final Map<UUID, Integer> smokeTiers;
    private final Map<UUID, Integer> molotovTiers;
    
    // Items consommables stockés par joueur
    private final Map<UUID, List<ConsumableItem>> consumableItems;
    
    public static class ConsumableItem {
        public final Material material;
        public final String name;
        
        public ConsumableItem(Material material, String name) {
            this.material = material;
            this.name = name;
        }
    }
    
    public FocusShopData() {
        this.swordLevels = new HashMap<>();
        this.helmetLevels = new HashMap<>();
        this.chestplateLevels = new HashMap<>();
        this.leggingsLevels = new HashMap<>();
        this.bootsLevels = new HashMap<>();
        this.bowLevels = new HashMap<>();
        this.arrowLevels = new HashMap<>();
        this.axeLevels = new HashMap<>();
        this.axeElements = new HashMap<>();
        this.smokeTiers = new HashMap<>();
        this.molotovTiers = new HashMap<>();
        this.consumableItems = new HashMap<>();
    }
    
    // ========== SWORD ==========
    
    public int getSwordLevel(UUID playerId) {
        return swordLevels.getOrDefault(playerId, 0);
    }
    
    public void setSwordLevel(UUID playerId, int level) {
        swordLevels.put(playerId, level);
    }
    
    // ========== BOW ==========
    
    public int getBowLevel(UUID playerId) {
        return bowLevels.getOrDefault(playerId, 0);
    }
    
    public void setBowLevel(UUID playerId, int level) {
        bowLevels.put(playerId, level);
    }
    
    // ========== HELMET ==========
    
    public int getHelmetLevel(UUID playerId) {
        return helmetLevels.getOrDefault(playerId, 0);
    }
    
    public void setHelmetLevel(UUID playerId, int level) {
        helmetLevels.put(playerId, level);
    }
    
    // ========== CHESTPLATE ==========
    
    public int getChestplateLevel(UUID playerId) {
        return chestplateLevels.getOrDefault(playerId, 0);
    }
    
    public void setChestplateLevel(UUID playerId, int level) {
        chestplateLevels.put(playerId, level);
    }
    
    // ========== LEGGINGS ==========
    
    public int getLeggingsLevel(UUID playerId) {
        return leggingsLevels.getOrDefault(playerId, 0);
    }
    
    public void setLeggingsLevel(UUID playerId, int level) {
        leggingsLevels.put(playerId, level);
    }
    
    // ========== BOOTS ==========
    
    public int getBootsLevel(UUID playerId) {
        return bootsLevels.getOrDefault(playerId, 0);
    }
    
    public void setBootsLevel(UUID playerId, int level) {
        bootsLevels.put(playerId, level);
    }
    
    // ========== ARROW ==========
    
    public int getArrowLevel(UUID playerId) {
        return arrowLevels.getOrDefault(playerId, 0);
    }
    
    public void setArrowLevel(UUID playerId, int level) {
        arrowLevels.put(playerId, level);
    }
    
    // ========== AXE ==========
    
    public int getAxeLevel(UUID playerId) {
        return axeLevels.getOrDefault(playerId, 0);
    }
    
    public void setAxeLevel(UUID playerId, int level) {
        axeLevels.put(playerId, level);
    }
    
    public String getAxeElement(UUID playerId) {
        return axeElements.get(playerId);
    }
    
    public void setAxeElement(UUID playerId, String element) {
        axeElements.put(playerId, element);
    }
    
    // ========== SMOKE ==========
    
    public int getSmokeTier(UUID playerId) {
        return smokeTiers.getOrDefault(playerId, 0);
    }
    
    public void setSmokeTier(UUID playerId, int tier) {
        smokeTiers.put(playerId, tier);
    }
    
    // ========== MOLOTOV ==========
    
    public int getMolotovTier(UUID playerId) {
        return molotovTiers.getOrDefault(playerId, 0);
    }
    
    public void setMolotovTier(UUID playerId, int tier) {
        molotovTiers.put(playerId, tier);
    }
    
    // ========== CONSUMABLE ITEMS ==========
    
    public void addConsumableItem(UUID playerId, Material material, String name) {
        consumableItems.computeIfAbsent(playerId, k -> new ArrayList<>()).add(new ConsumableItem(material, name));
    }
    
    public List<ConsumableItem> getConsumableItems(UUID playerId) {
        return consumableItems.getOrDefault(playerId, new ArrayList<>());
    }
    
    public void clearConsumableItems(UUID playerId) {
        consumableItems.remove(playerId);
    }
    
    // ========== RESET ==========
    
    public void resetPlayer(UUID playerId) {
        swordLevels.put(playerId, 0);
        helmetLevels.put(playerId, 0);
        chestplateLevels.put(playerId, 0);
        leggingsLevels.put(playerId, 0);
        bootsLevels.put(playerId, 0);
        bowLevels.put(playerId, 0);
        arrowLevels.put(playerId, 0);
        axeLevels.put(playerId, 0);
        axeElements.remove(playerId);
        smokeTiers.put(playerId, 0);
        molotovTiers.put(playerId, 0);
        consumableItems.remove(playerId);
    }
    
    public void resetAll() {
        swordLevels.clear();
        helmetLevels.clear();
        chestplateLevels.clear();
        leggingsLevels.clear();
        bootsLevels.clear();
        bowLevels.clear();
        arrowLevels.clear();
        axeLevels.clear();
        axeElements.clear();
        smokeTiers.clear();
        molotovTiers.clear();
        consumableItems.clear();
    }
}
