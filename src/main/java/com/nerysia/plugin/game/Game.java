package com.nerysia.plugin.game;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Game {
    
    private final String id;
    private String name;
    private final GameMode mode;
    private GameState state;
    private final UUID creator;
    private final List<UUID> players;
    private final List<UUID> spectators;
    private boolean isPrivate;
    private String password;
    private final long createdAt;
    
    public Game(String id, String name, GameMode mode, UUID creator) {
        this.id = id;
        this.name = name;
        this.mode = mode;
        this.creator = creator;
        this.state = GameState.CREATION;
        this.players = new ArrayList<>();
        this.spectators = new ArrayList<>();
        this.isPrivate = false;
        this.password = null;
        this.createdAt = System.currentTimeMillis();
        
        // Ajouter automatiquement le créateur à la partie
        this.players.add(creator);
    }
    
    // Getters
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public GameMode getMode() {
        return mode;
    }
    
    public GameType getType() {
        return mode.getType();
    }
    
    public GameState getState() {
        return state;
    }
    
    public UUID getCreator() {
        return creator;
    }
    
    public List<UUID> getPlayers() {
        return new ArrayList<>(players);
    }
    
    public List<UUID> getSpectators() {
        return new ArrayList<>(spectators);
    }
    
    public int getPlayerCount() {
        return players.size();
    }
    
    public int getMaxPlayers() {
        return mode.getMaxPlayers();
    }
    
    public boolean isFull() {
        return players.size() >= mode.getMaxPlayers();
    }
    
    public boolean isPrivate() {
        return isPrivate;
    }
    
    public String getPassword() {
        return password;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    // Setters
    public void setName(String name) {
        this.name = name;
    }
    
    public void setState(GameState state) {
        this.state = state;
    }
    
    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
        if (isPrivate) {
            this.state = GameState.PRIVEE;
        }
    }
    
    public void setPassword(String password) {
        this.password = password;
        if (password != null && !password.isEmpty()) {
            this.isPrivate = true;
            this.state = GameState.PRIVEE;
        }
    }
    
    // Méthodes pour gérer les joueurs
    public boolean addPlayer(UUID playerUuid) {
        if (isFull() || state == GameState.EN_COURS || state == GameState.TERMINEE) {
            return false;
        }
        if (!players.contains(playerUuid)) {
            players.add(playerUuid);
            return true;
        }
        return false;
    }
    
    public boolean removePlayer(UUID playerUuid) {
        return players.remove(playerUuid);
    }
    
    public boolean hasPlayer(UUID playerUuid) {
        return players.contains(playerUuid);
    }
    
    public boolean addSpectator(UUID playerUuid) {
        if (!spectators.contains(playerUuid)) {
            spectators.add(playerUuid);
            return true;
        }
        return false;
    }
    
    public boolean removeSpectator(UUID playerUuid) {
        return spectators.remove(playerUuid);
    }
    
    public boolean hasSpectator(UUID playerUuid) {
        return spectators.contains(playerUuid);
    }
    
    // Méthodes utiles
    public boolean canJoin(Player player) {
        if (isFull()) return false;
        if (state == GameState.EN_COURS || state == GameState.TERMINEE) return false;
        return true;
    }
    
    public void start() {
        if (state != GameState.EN_COURS) {
            this.state = GameState.EN_COURS;
        }
    }
    
    public void finish() {
        this.state = GameState.TERMINEE;
    }
    
    @Override
    public String toString() {
        return "Game{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", mode=" + mode.getDisplayName() +
                ", state=" + state.getDisplayName() +
                ", players=" + players.size() + "/" + getMaxPlayers() +
                '}';
    }
}
