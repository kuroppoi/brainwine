package brainwine.api.models;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ZoneInfo {
    
    private final String name;
    private final String biome;
    private final String activity;
    private final boolean pvp;
    private final boolean premium;
    private final boolean locked;
    private final int playerCount;
    private final double explorationProgress;
    private final OffsetDateTime creationDate;
    
    public ZoneInfo(String name, String biome, String activity, boolean pvp, boolean premium, boolean locked, 
            int playerCount, double explorationProgress, OffsetDateTime creationDate) {
        this.name = name;
        this.biome = biome;
        this.activity = activity;
        this.pvp = pvp;
        this.premium = premium;
        this.locked = locked;
        this.playerCount = playerCount;
        this.explorationProgress = explorationProgress;
        this.creationDate = creationDate;
    }
    
    public String getName() {
        return name;
    }
    
    public String getBiome() {
        return biome;
    }
    
    public String getActivity() {
        return activity;
    }
    
    public boolean isPvp() {
        return pvp;
    }
    
    public boolean isPremium() {
        return premium;
    }
    
    @JsonProperty("protected")
    public boolean isLocked() {
        return locked;
    }
    
    @JsonProperty("players")
    public int getPlayerCount() {
        return playerCount;
    }
    
    @JsonProperty("explored")
    public double getExplorationProgress() {
        return explorationProgress;
    }
    
    @JsonProperty("gen_date")
    public OffsetDateTime getCreationDate() {
        return creationDate;
    }
}
