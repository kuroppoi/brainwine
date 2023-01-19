package brainwine.gameserver.zone.gen.caves;

import java.util.ArrayList;
import java.util.List;

import brainwine.gameserver.util.Vector2i;
import brainwine.gameserver.zone.gen.models.StoneType;

public class Cave {

    private final List<Vector2i> blocks = new ArrayList<>();
    private final List<Vector2i> ceilingBlocks = new ArrayList<>();
    private final List<Vector2i> floorBlocks = new ArrayList<>();
    private final CaveType type;
    private final StoneType stoneType;
    private final double depth;
    
    public Cave(CaveType type, StoneType stoneType, double depth) {
        this.type = type;
        this.stoneType = stoneType;
        this.depth = depth;
    }
    
    public void addBlock(Vector2i block) {
        blocks.add(block);
    }
    
    public void addBlock(int x, int y) {
        addBlock(new Vector2i(x, y));
    }
    
    public int getSize() {
        return blocks.size();
    }
    
    public List<Vector2i> getBlocks() {
        return blocks;
    }
    
    public void addCeilingBlock(Vector2i block) {
        ceilingBlocks.add(block);
    }
    
    public void addCeilingBlock(int x, int y) {
        addCeilingBlock(new Vector2i(x, y));
    }
    
    public void removeCeilingBlock(Vector2i block) {
        ceilingBlocks.remove(block);
    }
    
    public List<Vector2i> getCeilingBlocks() {
        return ceilingBlocks;
    }
    
    public void addFloorBlock(Vector2i block) {
        floorBlocks.add(block);
    }
    
    public void addFloorBlock(int x, int y) {
        addFloorBlock(new Vector2i(x, y));
    }
    
    public List<Vector2i> getFloorBlocks() {
        return floorBlocks;
    }
    
    public CaveType getType() {
        return type;
    }
    
    public StoneType getStoneType() {
        return stoneType;
    }
    
    public double getDepth() {
        return depth;
    }
}
