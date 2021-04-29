package brainwine.gameserver.zone.gen.caves;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemRegistry;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.zone.gen.GeneratorContext;
import brainwine.gameserver.zone.gen.models.BlockPosition;
import brainwine.gameserver.zone.gen.models.Cave;
import brainwine.gameserver.zone.gen.models.CaveDecorator;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SaltCaveDecorator extends CaveDecorator {
    
    @JsonProperty("salt")
    private Item salt = ItemRegistry.getItem(586);
    
    @JsonProperty("salt_chance")
    private double rate = 0.175;
    
    @Override
    public void decorate(GeneratorContext ctx, Cave cave) {
        for(BlockPosition block : cave.getFloorBlocks()) {
            if(ctx.nextDouble() <= rate) {
                ctx.updateBlock(block.getX(), block.getY(), Layer.FRONT, salt);
            }
        }
    }
}
