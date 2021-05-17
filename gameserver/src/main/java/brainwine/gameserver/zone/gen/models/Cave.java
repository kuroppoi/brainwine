package brainwine.gameserver.zone.gen.models;

import java.util.ArrayList;
import java.util.List;

import brainwine.gameserver.zone.gen.CaveDecorator;

public class Cave {

    private final List<BlockPosition> blocks = new ArrayList<>();
    private final List<BlockPosition> ceilingBlocks = new ArrayList<>();
    private final List<BlockPosition> floorBlocks = new ArrayList<>();
    private final CaveDecorator decorator;
    private final StoneVariant variant;
    
    public Cave(CaveDecorator decorator, StoneVariant variant) {
        this.decorator = decorator;
        this.variant = variant;
    }
    
    public void addBlock(BlockPosition block) {
        blocks.add(block);
    }
    
    public void addBlock(int x, int y) {
        addBlock(new BlockPosition(x, y));
    }
    
    public int getSize() {
        return blocks.size();
    }
    
    public List<BlockPosition> getBlocks() {
        return blocks;
    }
    
    public void addCeilingBlock(BlockPosition block) {
        ceilingBlocks.add(block);
    }
    
    public void addCeilingBlock(int x, int y) {
        addCeilingBlock(new BlockPosition(x, y));
    }
    
    public void removeCeilingBlock(BlockPosition block) {
        ceilingBlocks.remove(block);
    }
    
    public List<BlockPosition> getCeilingBlocks() {
        return ceilingBlocks;
    }
    
    public void addFloorBlock(BlockPosition block) {
        floorBlocks.add(block);
    }
    
    public void addFloorBlock(int x, int y) {
        addFloorBlock(new BlockPosition(x, y));
    }
    
    public List<BlockPosition> getFloorBlocks() {
        return floorBlocks;
    }
    
    public CaveDecorator getDecorator() {
        return decorator;
    }
    
    public StoneVariant getVariant() {
        return variant;
    }
}
