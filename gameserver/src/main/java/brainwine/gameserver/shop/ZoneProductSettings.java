package brainwine.gameserver.shop;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.zone.Biome;

public class ZoneProductSettings {
    
    private final Biome biome;
    private final int width;
    private final int height;
    private final String generator;
    
    @JsonCreator
    public ZoneProductSettings(
            @JsonProperty(value = "biome", required = true) Biome biome,
            @JsonProperty(value = "width", required = true) int width,
            @JsonProperty(value = "height", required = true) int height,
            @JsonProperty(value = "generator") String generator) {
        this.biome = biome;
        this.width = width;
        this.height = height;
        this.generator = generator;
    }
    
    public Biome getBiome() {
        return biome;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public String getGenerator() {
        return generator;
    }
}
