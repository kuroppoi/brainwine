package brainwine.gameserver.zone.gen.caves;

import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.zone.gen.GeneratorContext;
import brainwine.gameserver.zone.gen.models.BlockPosition;

public class FillCaveDecorator extends CaveDecorator {
    
    @JsonProperty("item")
    private Item item = Item.AIR;
    
    @JsonProperty("liquid")
    private boolean liquid = false;
    
    @Override
    public void decorate(GeneratorContext ctx, Cave cave) {
        int minY = -1;
        int maxY = -1;
        
        for(BlockPosition block : cave.getBlocks()) {
            int y = block.getY();
            
            if(y < minY || minY == -1) {
                minY = y;
            }
            
            if(y > maxY || maxY == -1) {
                maxY = y;
            }
        }
        
        int maxDifference = (maxY - minY + 1) / 5;
        int center = (minY + maxY) / 2;
        int volume = ctx.nextInt(maxDifference + 1) - maxDifference + center;
        int surfaceLevel = ctx.nextInt(4) + 1;
        
        for(BlockPosition block : cave.getBlocks()) {
            if(block.getY() >= volume) {
                int mod = liquid ? block.getY() == volume ? surfaceLevel : 5 : 0;
                ctx.updateBlock(block.getX(), block.getY(), item.getLayer(), item, mod);
            }
        }
    }
}
