package com.nerysia.plugin.game.focus;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FocusGame {
    
    private final String gameId;
    private final UUID hostId;
    private final String hostName;
    private FocusGameState state;
    private final FocusGameSettings settings;
    private final List<UUID> players;
    private boolean isPublic;
    private long creationTime;
    
    public FocusGame(String gameId, Player host) {
        this.gameId = gameId;
        this.hostId = host.getUniqueId();
        this.hostName = host.getName();
        this.state = FocusGameState.PREPARATION;
        this.settings = new FocusGameSettings();
        this.players = new ArrayList<>();
        this.players.add(host.getUniqueId());
        this.isPublic = false; // Par défaut en préparation (ni public ni privé affiché)
        this.creationTime = System.currentTimeMillis();
    }
    
    public String getGameId() {
        return gameId;
    }
    
    public UUID getHostId() {
        return hostId;
    }
    
    public String getHostName() {
        return hostName;
    }
    
    public FocusGameState getState() {
        return state;
    }
    
    public void setState(FocusGameState state) {
        this.state = state;
    }
    
    public FocusGameSettings getSettings() {
        return settings;
    }
    
    public List<UUID> getPlayers() {
        return new ArrayList<>(players);
    }
    
    public int getPlayerCount() {
        return players.size();
    }
    
    public boolean addPlayer(UUID playerId) {
        if (players.size() >= settings.getMaxPlayers()) {
            return false;
        }
        if (players.contains(playerId)) {
            return false;
        }
        return players.add(playerId);
    }
    
    public boolean removePlayer(UUID playerId) {
        return players.remove(playerId);
    }
    
    public boolean isHost(UUID playerId) {
        return hostId.equals(playerId);
    }
    
    public boolean isPlayerInGame(UUID playerId) {
        return players.contains(playerId);
    }
    
    public boolean isPublic() {
        return isPublic;
    }
    
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
        
        // Quand l'host choisit public/privé, on sort de PREPARATION
        if (state == FocusGameState.PREPARATION) {
            if (isPublic) {
                state = FocusGameState.AVAILABLE; // Public = disponible pour tous
            } else {
                state = FocusGameState.PRIVATE; // Privé = sur invitation uniquement
            }
        } else if (state == FocusGameState.AVAILABLE || state == FocusGameState.PRIVATE) {
            // Si on change après, mettre à jour l'état
            if (isPublic) {
                if (players.size() < settings.getMaxPlayers()) {
                    state = FocusGameState.AVAILABLE;
                }
            } else {
                state = FocusGameState.PRIVATE;
            }
        }
    }
    
    public boolean isFull() {
        return players.size() >= settings.getMaxPlayers();
    }
    
    public long getCreationTime() {
        return creationTime;
    }
    
    public String getDisplayName() {
        return "§ePartie de §6" + hostName;
    }
}
