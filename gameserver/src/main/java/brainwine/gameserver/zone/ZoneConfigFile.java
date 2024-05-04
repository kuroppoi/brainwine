package brainwine.gameserver.zone;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

import brainwine.gameserver.item.Item;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ZoneConfigFile {
    
    @JsonSetter(nulls = Nulls.FAIL)
    private String name = "Mystery Zone";
    
    @JsonSetter(nulls = Nulls.SKIP)
    private Biome biome = Biome.PLAIN;
    
    @JsonSetter(nulls = Nulls.FAIL)
    private int width;
    
    @JsonSetter(nulls = Nulls.FAIL)
    private int height;
    
    @JsonSetter(nulls = Nulls.SKIP)
    private float acidity = 1.0F;
    
    @JsonSetter(nulls = Nulls.SKIP, contentNulls = Nulls.SKIP)
    private Map<EcologicalMachine, List<Item>> discoveredParts = new HashMap<>();
    
    @JsonSetter(nulls = Nulls.SKIP)
    private OffsetDateTime creationDate = OffsetDateTime.now();
    
    public ZoneConfigFile(Zone zone) {
        this(zone.getName(), zone.getBiome(), zone.getWidth(), zone.getHeight(), zone.getAcidity(), zone.getDiscoveredParts(), zone.getCreationDate());
    }
    
    public ZoneConfigFile(String name, Biome biome, int width, int height, float acidity,
            Map<EcologicalMachine, List<Item>> discoveredParts, OffsetDateTime creationDate) {
        this.name = name;
        this.biome = biome;
        this.width = width;
        this.height = height;
        this.acidity = acidity;
        this.discoveredParts = discoveredParts;
        this.creationDate = creationDate;
    }
    
    @JsonCreator
    private ZoneConfigFile(@JsonProperty(value = "name", required = true) String name,
            @JsonProperty(value = "width", required = true) int width,
            @JsonProperty(value = "height", required = true) int height) {
        this.name = name;
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
    
    public float getAcidity() {
        return acidity;
    }
    
    public Map<EcologicalMachine, List<Item>> getDiscoveredParts() {
        return discoveredParts;
    }
    
    public OffsetDateTime getCreationDate() {
        return creationDate;
    }
}
