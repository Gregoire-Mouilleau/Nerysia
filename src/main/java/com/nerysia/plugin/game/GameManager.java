package com.nerysia.plugin.game;

import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.stream.Collectors;

public class GameManager {
    
    private final Map<String, Game> games;
    private int gameCounter;
    
    public GameManager(Plugin plugin) {
        this.games = new HashMap<>();
        this.gameCounter = 0;
    }
    
    /**
     * Créer une nouvelle partie
     */
    public Game createGame(String name, GameMode mode, UUID creator) {
        gameCounter++;
        String gameId = "game_" + gameCounter;
        Game game = new Game(gameId, name, mode, creator);
        games.put(gameId, game);
        return game;
    }
    
    /**
     * Récupérer une partie par son ID
     */
    public Game getGame(String gameId) {
        return games.get(gameId);
    }
    
    /**
     * Supprimer une partie
     */
    public boolean deleteGame(String gameId) {
        return games.remove(gameId) != null;
    }
    
    /**
     * Récupérer toutes les parties
     */
    public List<Game> getAllGames() {
        return new ArrayList<>(games.values());
    }
    
    /**
     * Récupérer les parties par type
     */
    public List<Game> getGamesByType(GameType type) {
        return games.values().stream()
                .filter(game -> game.getType() == type)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupérer les parties par mode
     */
    public List<Game> getGamesByMode(GameMode mode) {
        return games.values().stream()
                .filter(game -> game.getMode() == mode)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupérer les parties par état
     */
    public List<Game> getGamesByState(GameState state) {
        return games.values().stream()
                .filter(game -> game.getState() == state)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupérer les parties disponibles (non pleines, pas en cours, pas terminées)
     */
    public List<Game> getAvailableGames() {
        return games.values().stream()
                .filter(game -> !game.isFull() && 
                               game.getState() != GameState.EN_COURS && 
                               game.getState() != GameState.TERMINEE &&
                               !game.isPrivate())
                .collect(Collectors.toList());
    }
    
    /**
     * Récupérer les parties d'un joueur
     */
    public List<Game> getPlayerGames(UUID playerUuid) {
        return games.values().stream()
                .filter(game -> game.hasPlayer(playerUuid))
                .collect(Collectors.toList());
    }
    
    /**
     * Récupérer la partie actuelle d'un joueur
     */
    public Game getPlayerCurrentGame(UUID playerUuid) {
        return games.values().stream()
                .filter(game -> game.hasPlayer(playerUuid) && 
                               (game.getState() == GameState.EN_COURS || 
                                game.getState() == GameState.DISPONIBLE ||
                                game.getState() == GameState.SUR_DEMANDE))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Vérifier si un joueur est dans une partie
     */
    public boolean isPlayerInGame(UUID playerUuid) {
        return games.values().stream()
                .anyMatch(game -> game.hasPlayer(playerUuid) && 
                                 game.getState() != GameState.TERMINEE);
    }
    
    /**
     * Nettoyer les parties terminées (optionnel)
     */
    public void cleanupFinishedGames() {
        List<String> toRemove = games.entrySet().stream()
                .filter(entry -> entry.getValue().getState() == GameState.TERMINEE)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        toRemove.forEach(games::remove);
    }
    
    /**
     * Obtenir le nombre total de parties
     */
    public int getGameCount() {
        return games.size();
    }
    
    /**
     * Obtenir le nombre de parties en cours
     */
    public int getActiveGameCount() {
        return (int) games.values().stream()
                .filter(game -> game.getState() == GameState.EN_COURS)
                .count();
    }
}
