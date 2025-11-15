package com.nerysia.plugin.game.focus;

public class FocusGameSettings {
    
    public enum VictoryCondition {
        KILLS,          // Victoire par kills uniquement
        ROUNDS,         // Victoire par rounds uniquement
        KILLS_AND_ROUNDS // Victoire par kills ET rounds (les deux conditions doivent être remplies)
    }
    
    private int maxPlayers;
    private int difficulty;
    private int roundTime;
    private boolean allowSpectators;
    private VictoryCondition victoryCondition;
    private int killsToWin;
    private int roundsToWin;
    
    public FocusGameSettings() {
        // Valeurs par défaut
        this.maxPlayers = 8;
        this.difficulty = 1; // 1 = Facile, 2 = Moyen, 3 = Difficile
        this.roundTime = 60; // Temps en secondes par round
        this.allowSpectators = true;
        this.victoryCondition = VictoryCondition.KILLS;
        this.killsToWin = 10;
        this.roundsToWin = 5;
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
    
    // ========== VICTORY CONDITION ==========
    
    public VictoryCondition getVictoryCondition() {
        return victoryCondition;
    }
    
    public void setVictoryCondition(VictoryCondition victoryCondition) {
        this.victoryCondition = victoryCondition;
    }
    
    public String getVictoryConditionName() {
        switch (victoryCondition) {
            case KILLS:
                return "§eKills";
            case ROUNDS:
                return "§aRounds";
            case KILLS_AND_ROUNDS:
                return "§6Kills + Rounds";
            default:
                return "§7Inconnu";
        }
    }
    
    public int getKillsToWin() {
        return killsToWin;
    }
    
    public void setKillsToWin(int killsToWin) {
        this.killsToWin = Math.max(1, Math.min(50, killsToWin));
    }
    
    public int getRoundsToWin() {
        return roundsToWin;
    }
    
    public void setRoundsToWin(int roundsToWin) {
        this.roundsToWin = Math.max(1, Math.min(20, roundsToWin));
    }
}
