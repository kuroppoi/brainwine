package brainwine.gameserver.loot;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.zone.Biome;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Loot {
    
    @JsonProperty("items")
    private Map<Item, Integer> items = new HashMap<>();
    
    @JsonProperty("crowns")
    private int crowns;
    
    @JsonProperty("frequency")
    private double frequency = 1.0;
    
    @JsonProperty("biome")
    private Biome biome;
    
    @JsonCreator
    private Loot() {}
    
    public Map<Item, Integer> getItems() {
        return items;
    }
    
    public int getCrowns() {
        return crowns;
    }
    
    public double getFrequency() {
        return frequency;
    }
    
    public Biome getBiome() {
        return biome;
    }
}
