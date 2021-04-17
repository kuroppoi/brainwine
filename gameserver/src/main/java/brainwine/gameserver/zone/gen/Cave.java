package brainwine.gameserver.zone.gen;

import java.util.ArrayList;
import java.util.List;

public class Cave {
    
    private final List<BlockPosition> blocks = new ArrayList<>();
    private final CaveType type;
    
    public Cave(CaveType type) {
        this.type = type;
    }
    
    public void addBlock(int x, int y) {
        addBlock(new BlockPosition(x, y));
    }
    
    public void addBlock(BlockPosition block) {
        blocks.add(block);
    }
    
    public List<BlockPosition> getBlocks() {
        return blocks;
    }
    
    public CaveType getType() {
        return type;
    }
    
    public int getSize() {
        return blocks.size();
    }
}
