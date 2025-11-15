package com.nerysia.plugin.game.focus;

public class FocusGameSettings {
    
    private int maxPlayers;
    private int difficulty;
    private int roundTime;
    private boolean allowSpectators;
    
    public FocusGameSettings() {
        // Valeurs par défaut
        this.maxPlayers = 8;
        this.difficulty = 1; // 1 = Facile, 2 = Moyen, 3 = Difficile
        this.roundTime = 60; // Temps en secondes par round
        this.allowSpectators = true;
    }
    
    public int getMaxPlayers() {
        return maxPlayers;
    }
    
    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }
    
    public int getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }
    
    public int getRoundTime() {
        return roundTime;
    }
    
    public void setRoundTime(int roundTime) {
        this.roundTime = roundTime;
    }
    
    public boolean isAllowSpectators() {
        return allowSpectators;
    }
    
    public void setAllowSpectators(boolean allowSpectators) {
        this.allowSpectators = allowSpectators;
    }
    
    public String getDifficultyName() {
        switch (difficulty) {
            case 1:
                return "§aFacile";
            case 2:
                return "§eNormal";
            case 3:
                return "§cDifficile";
            default:
                return "§7Inconnu";
        }
    }
}
