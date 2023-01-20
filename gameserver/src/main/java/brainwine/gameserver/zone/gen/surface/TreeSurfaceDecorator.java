package brainwine.gameserver.zone.gen.surface;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.item.Layer;
import brainwine.gameserver.zone.gen.GeneratorContext;

public class TreeSurfaceDecorator extends SurfaceDecorator {
    
    @JsonProperty("max_trees")
    protected int maxTrees;
    
    @JsonProperty("tree_spawn_chance")
    protected double treeSpawnChance = 0.2;
    
    @JsonProperty("spacing")
    protected int spacing = 6;
    
    @JsonCreator
    protected TreeSurfaceDecorator() {}
    
    @Override
    public void decorate(GeneratorContext ctx, SurfaceRegion region) {
        int treeCount = 0;
        int x = region.getStart();
        
        while(x + 3 < region.getEnd()) {
            boolean treePlaced = false;
            
            // If the spawn try succeeds
            if(ctx.nextDouble() < treeSpawnChance) {
                int surface = ctx.getZone().getSurface()[x + 3];
                
                // Check if there is room for a tree here
                if(canPlaceTree(ctx, x, surface)) {
                    placeTree(ctx, x, surface);
                    treeCount++;
                    treePlaced = true;
                }
            }
            
            // Stop if we've reached the maximum number of trees
            if(maxTrees > 0 && treeCount >= maxTrees) {
                break;
            }
            
            x += treePlaced ? spacing : 1;
        }
    }
    
    protected boolean canPlaceTree(GeneratorContext ctx, int x, int y) {   
        // Check if there's space for the trunk
        for(int j = y - 4; j < y; j++) {
            if(!ctx.inBounds(x + 3, j) || !ctx.getZone().getBlock(x + 3, j).getFrontItem().isAir()) {
                return false;
            }
        }
        
        // Check if there's space for the roots
        for(int i = x; i < x + 6; i++) {
            for(int j = y; j < y + 4; j++) {
                if(!ctx.inBounds(i, j) || (!ctx.isEarthy(i, j) && !ctx.getZone().getBlock(i, j).getFrontItem().isAir())) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    protected void placeTree(GeneratorContext ctx, int x, int y) {
        placeTreeTrunk(ctx, x + 3, y - 3);
        placeTreeRoots(ctx, x, y);
    }
    
    protected void placeTreeTrunk(GeneratorContext ctx, int x, int y) {
        for(int j = 0; j < 3; j++) {
            ctx.updateBlock(x, y + j, Layer.FRONT, 720, j);
        }
        
        ctx.updateBlock(x - 1, y - 1, Layer.FRONT, 725); // TODO why is this not positioned correctly?
    }
    
    protected void placeTreeRoots(GeneratorContext ctx, int x, int y) {
        int rootWidth = 6;
        int rootHeight = 3;
        
        for(int i = 0; i < rootWidth; i++) {
            for(int j = 0; j < rootHeight; j++) {
                // Voodoo to exclude some unimportant parts of the roots
                if(i == 0 || (i == 5 && j == 0) || (j == 0 && i == 1 && !ctx.isEarthy(x + i, y + j))) {
                    continue;
                }
                
                int mod = j * rootWidth + i;
                ctx.updateBlock(x + i, y + j, Layer.FRONT, 721, mod);
            }
        }
    }
}
