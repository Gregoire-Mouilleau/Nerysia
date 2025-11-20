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
    private int pointsPerKill;
    private int firstPlacePoints;
    
    public FocusGameSettings() {
        // Valeurs par défaut
        resetToDefaults();
    }
    
    public void resetToDefaults() {
        this.maxPlayers = 8;
        this.difficulty = 1; // 1 = Facile, 2 = Moyen, 3 = Difficile
        this.roundTime = 60; // Temps en secondes par round
        this.allowSpectators = true;
        this.victoryCondition = VictoryCondition.KILLS;
        this.killsToWin = 10;
        this.roundsToWin = 5;
        this.pointsPerKill = 2; // Points par kill
        this.firstPlacePoints = 8; // Points pour la 1ère place
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
    
    // ========== POINTS SYSTEM ==========
    
    public int getPointsPerKill() {
        return pointsPerKill;
    }
    
    public void setPointsPerKill(int pointsPerKill) {
        this.pointsPerKill = Math.max(1, Math.min(10, pointsPerKill));
    }
    
    public int getFirstPlacePoints() {
        return firstPlacePoints;
    }
    
    public void setFirstPlacePoints(int firstPlacePoints) {
        this.firstPlacePoints = Math.max(1, Math.min(20, firstPlacePoints));
    }
    
    /**
     * Calcule les points pour un placement donné
     * @param placement 1 = 1er, 2 = 2ème, etc.
     * @return points à attribuer
     */
    public int getPointsForPlacement(int placement) {
        if (placement == 1) return firstPlacePoints;
        if (placement == 2) return Math.max(1, firstPlacePoints - 2);
        
        // À partir de la 3ème place, on descend de 1 à chaque fois
        int points = firstPlacePoints - 2 - (placement - 2);
        return Math.max(1, points); // Minimum 1 point
    }
}

