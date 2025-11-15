package com.nerysia.plugin.game.focus;

import com.nerysia.plugin.game.focus.listeners.FocusItemsListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class FocusGameManager {
    
    private final Map<String, FocusGame> games;
    private final Map<UUID, String> playerToGame;
    private final Map<String, FocusGameController> gameControllers;
    private int gameCounter;
    
    private final FocusPointsManager pointsManager;
    private final FocusShopData shopData;
    private final FocusShopGUI shopGUI;
    
    // Référence au listener des items pour clear les mines
    private static FocusItemsListener itemsListener;
    
    public FocusGameManager(FocusPointsManager pointsManager, FocusShopData shopData, FocusShopGUI shopGUI) {
        this.games = new HashMap<>();
        this.playerToGame = new HashMap<>();
        this.gameControllers = new HashMap<>();
        this.gameCounter = 1;
        this.pointsManager = pointsManager;
        this.shopData = shopData;
        this.shopGUI = shopGUI;
    }
    
    /**
     * Créer une nouvelle partie Focus
     */
    public FocusGame createGame(Player host) {
        String gameId = "FOCUS-" + gameCounter++;
        FocusGame game = new FocusGame(gameId, host);
        
        games.put(gameId, game);
        playerToGame.put(host.getUniqueId(), gameId);
        
        // Créer le controller associé (passer this pour permettre le cleanup)
        FocusGameController controller = new FocusGameController(game, pointsManager, shopData, shopGUI, this);
        gameControllers.put(gameId, controller);
        
        Bukkit.getLogger().info("[FOCUS] Partie créée: " + gameId + " par " + host.getName());
        return game;
    }
    
    /**
     * Récupérer le controller d'une partie
     */
    public FocusGameController getGameController(FocusGame game) {
        return gameControllers.get(game.getGameId());
    }
    
    /**
     * Supprimer une partie
     */
    public boolean deleteGame(String gameId) {
        FocusGame game = games.remove(gameId);
        if (game != null) {
            // Retirer tous les joueurs de la map playerToGame
            for (UUID playerId : game.getPlayers()) {
                playerToGame.remove(playerId);
            }
            // Supprimer le controller
            gameControllers.remove(gameId);
            Bukkit.getLogger().info("[FOCUS] Partie supprimée: " + gameId);
            return true;
        }
        return false;
    }
    
    /**
     * Récupérer une partie par son ID
     */
    public FocusGame getGame(String gameId) {
        return games.get(gameId);
    }
    
    /**
     * Récupérer la partie d'un joueur
     */
    public FocusGame getPlayerGame(UUID playerId) {
        String gameId = playerToGame.get(playerId);
        return gameId != null ? games.get(gameId) : null;
    }
    
    /**
     * Vérifier si un joueur est dans une partie
     */
    public boolean isPlayerInGame(UUID playerId) {
        return playerToGame.containsKey(playerId);
    }
    
    /**
     * Ajouter un joueur à une partie
     */
    public boolean addPlayerToGame(String gameId, Player player) {
        FocusGame game = games.get(gameId);
        if (game == null) {
            return false;
        }
        
        // Vérifier si le joueur est déjà dans une partie
        if (isPlayerInGame(player.getUniqueId())) {
            return false;
        }
        
        // Vérifier si la partie est disponible
        if (game.getState() != FocusGameState.AVAILABLE && game.getState() != FocusGameState.PREPARATION) {
            return false;
        }
        
        // Vérifier si la partie est privée
        if (!game.isPublic()) {
            return false;
        }
        
        // Ajouter le joueur
        if (game.addPlayer(player.getUniqueId())) {
            playerToGame.put(player.getUniqueId(), gameId);
            
            // Mettre à jour l'état si la partie est pleine
            if (game.isFull()) {
                game.setState(FocusGameState.PRIVATE);
            }
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Retirer un joueur d'une partie
     */
    public boolean removePlayerFromGame(UUID playerId) {
        String gameId = playerToGame.remove(playerId);
        if (gameId != null) {
            FocusGame game = games.get(gameId);
            if (game != null) {
                game.removePlayer(playerId);
                
                // Si c'était l'hôte, supprimer la partie
                if (game.isHost(playerId)) {
                    deleteGame(gameId);
                    return true;
                }
                
                // Mettre à jour l'état si la partie n'est plus pleine
                if (!game.isFull() && game.isPublic() && game.getState() == FocusGameState.PRIVATE) {
                    game.setState(FocusGameState.AVAILABLE);
                }
                
                return true;
            }
        }
        return false;
    }
    
    /**
     * Récupérer toutes les parties
     */
    public List<FocusGame> getAllGames() {
        return new ArrayList<>(games.values());
    }
    
    /**
     * Récupérer toutes les parties visibles (y compris les terminées pour l'historique)
     */
    public List<FocusGame> getVisibleGames() {
        List<FocusGame> visibleGames = new ArrayList<>();
        for (FocusGame game : games.values()) {
            // Afficher toutes les parties, y compris FINISHED (pour l'historique)
            visibleGames.add(game);
        }
        return visibleGames;
    }
    
    /**
     * Nettoyer les parties terminées depuis plus de X minutes
     */
    public void cleanupOldGames(long maxAgeMinutes) {
        long currentTime = System.currentTimeMillis();
        long maxAge = maxAgeMinutes * 60 * 1000;
        
        List<String> toRemove = new ArrayList<>();
        for (FocusGame game : games.values()) {
            if (game.getState() == FocusGameState.FINISHED) {
                if (currentTime - game.getCreationTime() > maxAge) {
                    toRemove.add(game.getGameId());
                }
            }
        }
        
        for (String gameId : toRemove) {
            deleteGame(gameId);
        }
    }
    
    /**
     * Définir le listener des items
     */
    public static void setItemsListener(FocusItemsListener listener) {
        itemsListener = listener;
    }
    
    /**
     * Réinitialiser les mines pour un nouveau round
     */
    public static void clearMines() {
        if (itemsListener != null) {
            itemsListener.clearPlacedMinesThisRound();
        }
    }
}
