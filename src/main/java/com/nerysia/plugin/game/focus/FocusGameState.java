package com.nerysia.plugin.game.focus;

public enum FocusGameState {
    PREPARATION,    // Jaune - En préparation
    AVAILABLE,      // Vert - Disponible pour rejoindre
    PRIVATE,        // Orange - Partie privée ou complète
    IN_PROGRESS,    // Rouge - Partie en cours
    FINISHED        // Gris foncé - Partie terminée
}
