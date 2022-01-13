package brainwine.gameserver.prefab;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.util.WeightedMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PrefabConfig {
    
    @JsonProperty("dungeon")
    private boolean dungeon;
    
    @JsonProperty("ruin")
    private boolean ruin;
    
    @JsonProperty("loot")
    private boolean loot; 
    
    @JsonProperty("decay")
    private boolean decay;
    
    @JsonProperty("mirrorable")
    private boolean mirrorable;
    
    @JsonProperty("replace")
    private Map<Item, WeightedMap<Item>> replacements = new HashMap<>();
    
    @JsonProperty("corresponding_replace")
    private Map<Item, CorrespondingReplacement> correspondingReplacements = new HashMap<>();
    
    @JsonProperty("metadata")
    private Map<Integer, Map<String, Object>> metadata = new HashMap<>();
    
    @JsonCreator
    private PrefabConfig() {}
    
    protected PrefabConfig(Prefab prefab) {
        dungeon = prefab.isDungeon();
        ruin = prefab.isRuin();
        loot = prefab.hasLoot();
        decay = prefab.hasDecay();
        mirrorable = prefab.isMirrorable();
        replacements = prefab.getReplacements();
        correspondingReplacements = prefab.getCorrespondingReplacements();
        metadata = prefab.getMetadata();
    }
    
    public boolean isDungeon() {
        return dungeon;
    }
    
    public boolean isRuin() {
        return ruin;
    }
    
    public boolean hasLoot() {
        return loot;
    }
    
    public boolean hasDecay() {
        return decay;
    }
    
    public boolean isMirrorable() {
        return mirrorable;
    }
    
    public Map<Integer, Map<String, Object>> getMetadata() {
        return metadata;
    }
    
    public Map<Item, WeightedMap<Item>> getReplacements() {
        return replacements;
    }
    
    public Map<Item, CorrespondingReplacement> getCorrespondingReplacements() {
        return correspondingReplacements;
    }
}
