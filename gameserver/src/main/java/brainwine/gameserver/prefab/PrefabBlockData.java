package brainwine.gameserver.prefab;

import java.beans.ConstructorProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import brainwine.gameserver.zone.Block;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PrefabBlockData {
    
    private int width;
    private int height;
    private Block[] blocks;
    
    protected PrefabBlockData(Prefab prefab) {
        this(prefab.getWidth(), prefab.getHeight(), prefab.getBlocks());
    }
    
    @ConstructorProperties({"width", "height", "blocks"})
    public PrefabBlockData(int width, int height, Block[] blocks) {
        this.width = width;
        this.height = height;
        this.blocks = blocks;
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
}
