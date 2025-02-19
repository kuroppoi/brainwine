package brainwine.gameserver.zone;

import java.time.OffsetDateTime;
import java.util.ArrayList;
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

    @JsonSetter(nulls = Nulls.DEFAULT)
    private ZoneActivity activity;

    @JsonSetter(value = "private")
    private boolean isPrivate;

    @JsonSetter(value = "protected")
    private boolean isProtected;

    @JsonSetter(nulls = Nulls.SKIP)
    private boolean pvp;
    
    @JsonSetter(nulls = Nulls.SKIP)
    private String entryCode;
    
    @JsonSetter(nulls = Nulls.SKIP)
    private String owner;

    @JsonSetter(nulls = Nulls.SKIP, contentNulls = Nulls.SKIP)
    private List<String> members = new ArrayList<>();

    @JsonSetter(nulls = Nulls.SKIP, contentNulls = Nulls.SKIP)
    private Map<EcologicalMachine, List<Item>> discoveredParts = new HashMap<>();
    
    @JsonSetter(nulls = Nulls.SKIP, contentNulls = Nulls.SKIP)
    private Map<String, OffsetDateTime> actionHistory = new HashMap<>();

    @JsonSetter(nulls = Nulls.SKIP)
    private OffsetDateTime creationDate = OffsetDateTime.now();
    
    @JsonSetter(nulls = Nulls.SKIP)
    private OffsetDateTime lastActiveDate = OffsetDateTime.now();

    @JsonSetter(nulls = Nulls.SKIP)
    private MassSpawnerConfiguration massSpawnerConfiguration = new MassSpawnerConfiguration();

    @JsonSetter(nulls = Nulls.SKIP)
    private MassTeleporterConfiguration massTeleporterConfiguration = new MassTeleporterConfiguration();

    @JsonSetter(nulls = Nulls.SKIP)
    private ZoneRules rules = null;

    @JsonCreator
    private ZoneConfigFile(@JsonProperty(value = "name", required = true) String name,
            @JsonProperty(value = "width", required = true) int width,
            @JsonProperty(value = "height", required = true) int height) {
        this.name = name;
        this.width = width;
        this.height = height;
    }
    
    public ZoneConfigFile(Zone zone) {
        this.name = zone.getName();
        this.biome = zone.getBiome();
        this.width = zone.getWidth();
        this.height = zone.getHeight();
        this.acidity = zone.getAcidity();
        this.activity = zone.getActivity();
        this.isPrivate = zone.isPrivate();
        this.isProtected = zone.isProtected();
        this.pvp = zone.isPvp();
        this.entryCode = zone.getEntryCode();
        this.owner = zone.getOwner();
        this.members = zone.getMembers();
        this.discoveredParts = zone.getDiscoveredParts();
        this.actionHistory = zone.getActionHistory();
        this.creationDate = zone.getCreationDate();
        this.massSpawnerConfiguration = zone.getMassSpawnerConfiguration();
        this.massTeleporterConfiguration = zone.getMassTeleporterConfiguration();
        this.rules = zone.getRules();
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

    public ZoneActivity getActivity() {
        return activity;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public boolean isProtected() {
        return isProtected;
    }
    
    public boolean isPvp() {
        return pvp;
    }
    
    public String getEntryCode() {
        return entryCode;
    }
    
    public String getOwner() {
        return owner;
    }

    public List<String> getMembers() {
        return members;
    }

    public Map<EcologicalMachine, List<Item>> getDiscoveredParts() {
        return discoveredParts;
    }
    
    public Map<String, OffsetDateTime> getActionHistory() {
        return actionHistory;
    }

    public OffsetDateTime getCreationDate() {
        return creationDate;
    }
    
    public OffsetDateTime getLastActiveDate() {
        return lastActiveDate;
    }

    public MassSpawnerConfiguration getMassSpawnerConfiguration() {
        return massSpawnerConfiguration;
    }

    public MassTeleporterConfiguration getMassTeleporterConfiguration() {
        return massTeleporterConfiguration;
    }

    public ZoneRules getRules() {
        return rules;
    }
}
