package brainwine.api.models;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * TODO split model in two: one for internal use & one for {@code /v1/worlds} serialization.
 */
public class ZoneInfo {
    
    private final String name;
    private final String biome;
    private final String activity;
    private final boolean pvp;
    private final boolean premium;
    private final boolean locked;
    private final int playerCount;
    private final int width;
    private final int height;
    private final int[] surface;
    private final double explorationProgress;
    private final OffsetDateTime creationDate;
    
    public ZoneInfo(String name, String biome, String activity, boolean pvp, boolean premium, boolean locked, 
            int playerCount, int width, int height, int[] surface, double explorationProgress, OffsetDateTime creationDate) {
        this.name = name;
        this.biome = biome;
        this.activity = activity;
        this.pvp = pvp;
        this.premium = premium;
        this.locked = locked;
        this.playerCount = playerCount;
        this.width = width;
        this.height = height;
        this.surface = surface;
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
    
    @JsonIgnore
    public int getWidth() {
        return width;
    }
    
    @JsonIgnore
    public int getHeight() {
        return height;
    }
    
    @JsonIgnore
    public int[] getSurface() {
        return surface;
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
