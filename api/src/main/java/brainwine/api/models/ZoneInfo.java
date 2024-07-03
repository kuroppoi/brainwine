package brainwine.api.models;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ZoneInfo {
    
    private final String name;
    private final String biome;
    private final String activity;
    private final boolean pvp;
    private final boolean premium;
    private final boolean isProtected;
    private final int playerCount;
    private final double explorationProgress;
    private final OffsetDateTime creationDate;
    private final boolean isPrivate;
    private final String owner;
    private final List<String> members;
    
    public ZoneInfo(String name, String biome, String activity, boolean pvp, boolean premium, boolean isProtected, 
            int playerCount, double explorationProgress, OffsetDateTime creationDate, boolean isPrivate, String owner, List<String> members) {
        this.name = name;
        this.biome = biome;
        this.activity = activity;
        this.pvp = pvp;
        this.premium = premium;
        this.isProtected = isProtected;
        this.playerCount = playerCount;
        this.explorationProgress = explorationProgress;
        this.creationDate = creationDate;
        this.isPrivate = isPrivate;
        this.owner = owner;
        this.members = members;
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
    
    public boolean isProtected() {
        return isProtected;
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
    
    @JsonIgnore
    public boolean isPrivate() {
        return isPrivate;
    }
    
    @JsonIgnore
    public String getOwner() {
        return owner;
    }
    
    @JsonIgnore
    public List<String> getMembers() {
        return Collections.unmodifiableList(members);
    }
}
