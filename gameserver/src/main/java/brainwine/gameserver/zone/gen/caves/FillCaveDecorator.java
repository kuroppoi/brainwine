package brainwine.gameserver.zone.gen.caves;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.util.Vector2i;
import brainwine.gameserver.zone.gen.GeneratorContext;

public class FillCaveDecorator extends CaveDecorator {
    
    @JsonProperty("item")
    protected Item item = Item.AIR;
    
    @JsonProperty("liquid")
    protected boolean liquid = false;
    
    @JsonCreator
    protected FillCaveDecorator() {}
    
    @Override
    public void decorate(GeneratorContext ctx, Cave cave) {
        int minY = -1;
        int maxY = -1;
        
        // Establish the highest and lowest points in the cave
        for(Vector2i block : cave.getBlocks()) {
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
        
        for(Vector2i block : cave.getBlocks()) {
            if(block.getY() >= volume) {
                int mod = liquid ? block.getY() == volume ? surfaceLevel : 5 : 0;
                ctx.updateBlock(block.getX(), block.getY(), item.getLayer(), item, mod);
            }
        }
    }
}
