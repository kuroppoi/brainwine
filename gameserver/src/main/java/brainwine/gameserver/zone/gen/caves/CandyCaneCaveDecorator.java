package brainwine.gameserver.zone.gen.caves;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemRegistry;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.util.WeightedList;
import brainwine.gameserver.zone.gen.CaveDecorator;
import brainwine.gameserver.zone.gen.GeneratorContext;
import brainwine.gameserver.zone.gen.models.BlockPosition;
import brainwine.gameserver.zone.gen.models.Cave;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CandyCaneCaveDecorator extends CaveDecorator {
    
    @JsonProperty("candy_canes")
    private final WeightedList<Item> candyCanes = new WeightedList<Item>()
        .addEntry(ItemRegistry.getItem("holiday/candy-cane-small"), 6)
        .addEntry(ItemRegistry.getItem("holiday/candy-cane-large"), 1);
    
    @JsonProperty("candy_cane_chance")
    private double rate = 0.15;
    
    @Override
    public void decorate(GeneratorContext ctx, Cave cave) {
        for(BlockPosition block : cave.getFloorBlocks()) {
            if(ctx.nextDouble() <= rate) {
                ctx.updateBlock(block.getX(), block.getY(), Layer.FRONT, candyCanes.next(ctx.getRandom()));
            }
        }
    }
}
