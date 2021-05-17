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
public class CrystalCaveDecorator extends CaveDecorator {
    
    @JsonProperty("crystals")
    private final WeightedList<Item> crystals = new WeightedList<Item>()
        .addEntry(ItemRegistry.getItem("ground/crystal-blue-1"), 16)
        .addEntry(ItemRegistry.getItem("ground/crystal-blue-3"), 16)
        .addEntry(ItemRegistry.getItem("ground/crystal-blue-2"), 4)
        .addEntry(ItemRegistry.getItem("ground/crystal-blue-4"), 1);
    
    @JsonProperty("crystal_chance")
    private double rate = 0.1;
    
    @Override
    public void decorate(GeneratorContext ctx, Cave cave) {
        for(BlockPosition block : cave.getFloorBlocks()) {
            if(ctx.nextDouble() <= rate) {
                ctx.updateBlock(block.getX(), block.getY(), Layer.FRONT, crystals.next(ctx.getRandom()));
            }
        }
    }
}
