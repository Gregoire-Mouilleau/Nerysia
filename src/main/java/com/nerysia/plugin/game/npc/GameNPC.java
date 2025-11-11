package com.nerysia.plugin.game.npc;

import com.nerysia.plugin.game.GameMode;
import org.bukkit.Location;

public class GameNPC {
    
    private final int entityId;
    private final GameMode gameMode;
    private final String displayName;
    private final Location location;
    
    public GameNPC(int entityId, GameMode gameMode, String displayName, Location location) {
        this.entityId = entityId;
        this.gameMode = gameMode;
        this.displayName = displayName;
        this.location = location;
    }
    
    public int getEntityId() {
        return entityId;
    }
    
    public GameMode getGameMode() {
        return gameMode;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public Location getLocation() {
        return location;
    }
}
