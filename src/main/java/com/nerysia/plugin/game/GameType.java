package com.nerysia.plugin.game;

public enum GameType {
    UHC("§c§lUHC", "Ultra Hardcore"),
    MINI_GAMES("§a§lMini-Jeux", "Mini-Jeux");

    private final String displayName;
    private final String description;

    GameType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
