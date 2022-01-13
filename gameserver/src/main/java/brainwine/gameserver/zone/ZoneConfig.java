package brainwine.gameserver.zone;

import java.beans.ConstructorProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ZoneConfig {
    
    private final String name;
    private final Biome biome;
    private final int width;
    private final int height;
    
    @ConstructorProperties({"name", "biome", "width", "height"})
    public ZoneConfig(String name, Biome biome, int width, int height) {
        this.name = name;
        this.biome = biome;
        this.width = width;
        this.height = height;
    }
    
    public String getName() {
        return name;
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
}
