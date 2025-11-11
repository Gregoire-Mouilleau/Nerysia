package com.nerysia.plugin.game;

import org.bukkit.Material;

public enum GameMode {
    // UHC
    LOL_UHC(GameType.UHC, "§c§lLOL UHC", "LOL Ultra Hardcore", Material.GOLDEN_APPLE, 20),
    
    // Mini-Jeux
    ARENA(GameType.MINI_GAMES, "§4§lArena", "Combat en arène", Material.IRON_SWORD, 12),
    FOCUS(GameType.MINI_GAMES, "§e§lFocus", "Jeu de concentration", Material.ENDER_PEARL, 8),
    THE_TOWER(GameType.MINI_GAMES, "§6§lThe Tower", "Montez la tour", Material.BRICK, 16),
    FALLEN_KINGDOMS(GameType.MINI_GAMES, "§d§lFallen Kingdoms", "Royaumes déchus", Material.DIAMOND_BLOCK, 16),
    SKY_DEFENDER(GameType.MINI_GAMES, "§b§lSky Defender", "Défense aérienne", Material.FEATHER, 12),
    BALL_OF_STEELS(GameType.MINI_GAMES, "§7§lBall Of Steels", "Bataille de balles", Material.IRON_BLOCK, 10),
    RUSH(GameType.MINI_GAMES, "§a§lRush", "Rush rapide", Material.DIAMOND_SWORD, 8);

    private final GameType type;
    private final String displayName;
    private final String description;
    private final Material icon;
    private final int maxPlayers;

    GameMode(GameType type, String displayName, String description, Material icon, int maxPlayers) {
        this.type = type;
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.maxPlayers = maxPlayers;
    }

    public GameType getType() {
        return type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public Material getIcon() {
        return icon;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }
}
