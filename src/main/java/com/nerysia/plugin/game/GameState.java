package com.nerysia.plugin.game;

public enum GameState {
    CREATION("§e§lCréation", "La partie est en cours de création"),
    DISPONIBLE("§a§lDisponible", "La partie est ouverte aux joueurs"),
    SUR_DEMANDE("§b§lSur Demande", "La partie démarre sur demande"),
    PRIVEE("§6§lPrivée", "La partie est privée"),
    EN_COURS("§c§lEn Cours", "La partie a démarré"),
    TERMINEE("§7§lTerminée", "La partie est terminée");

    private final String displayName;
    private final String description;

    GameState(String displayName, String description) {
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
