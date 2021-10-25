package brainwine.gameserver.prefab;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.util.WeightedMap;
import brainwine.gameserver.zone.Block;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Prefab {
    
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
    
    @JsonIgnore
    private int width;
    
    @JsonIgnore
    private int height;
    
    @JsonIgnore
    private Block[] blocks;
    
    @JsonCreator
    private Prefab() {}
    
    @JsonIgnore
    public Prefab(int width, int height, Block[] blocks) {
        this(width, height, blocks, new HashMap<>());
    }
    
    @JsonIgnore
    public Prefab(int width, int height, Block[] blocks, Map<Integer, Map<String, Object>> metadata) {
        this.width = width;
        this.height = height;
        this.blocks = blocks;
        this.metadata = metadata;
    }
    
    @JsonCreator
    private static Prefab fromName(String name) {
        return GameServer.getInstance().getPrefabManager().getPrefab(name);
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
    
    public Map<String, Object> getMetadata(int index) {
        return metadata.get(index);
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
        
    public void setWidth(int width) {
        this.width = width;
    }
    
    public int getWidth() {
        return width;
    }
    
    public void setHeight(int height) {
        this.height = height;
    }
    
    public int getHeight() {
        return height;
    }
    
    public void setBlocks(Block[] blocks) {
        this.blocks = blocks;
    }
    
    public Block[] getBlocks() {
        return blocks;
    }
}
