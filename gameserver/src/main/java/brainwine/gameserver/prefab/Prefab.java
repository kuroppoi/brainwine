package brainwine.gameserver.prefab;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.util.WeightedMap;
import brainwine.gameserver.zone.Block;

public class Prefab {
    
    private boolean dungeon;
    private boolean ruin;
    private boolean loot;
    private boolean decay;
    private boolean mirrorable;
    private int width;
    private int height;
    private Block[] blocks;
    private Map<Item, WeightedMap<Item>> replacements = new HashMap<>();
    private Map<Item, CorrespondingReplacement> correspondingReplacements = new HashMap<>();
    private Map<Integer, Map<String, Object>> metadata = new HashMap<>();
    
    protected Prefab(PrefabConfigFile config, PrefabBlocksFile blockData) {
        this(blockData.getWidth(), blockData.getHeight(), blockData.getBlocks(), config.getMetadata());
        dungeon = config.isDungeon();
        ruin = config.isRuin();
        loot = config.hasLoot();
        decay = config.hasDecay();
        mirrorable = config.isMirrorable();
        replacements = config.getReplacements();
        correspondingReplacements = config.getCorrespondingReplacements();
    }
    
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
        
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public Block[] getBlocks() {
        return blocks;
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
}
