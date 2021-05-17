package brainwine.gameserver.zone.gen.caves;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.util.WeightedList;
import brainwine.gameserver.zone.gen.CaveDecorator;
import brainwine.gameserver.zone.gen.GeneratorContext;
import brainwine.gameserver.zone.gen.models.BlockPosition;
import brainwine.gameserver.zone.gen.models.Cave;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RareCrystalCaveDecorator extends CaveDecorator {
    
    @JsonProperty("crystals")
    private final WeightedList<Item> crystals = new WeightedList<>();
    
    @JsonProperty("crystal_chance")
    private double rate = 0.05;
    
    @Override
    public void decorate(GeneratorContext ctx, Cave cave) {
        if(!crystals.isEmpty()) {
            Item crystal = crystals.next(ctx.getRandom());
            
            for(BlockPosition block : cave.getFloorBlocks()) {
                if(ctx.nextDouble() <= rate) {
                    ctx.updateBlock(block.getX(), block.getY(), Layer.FRONT, crystal);
                }
            }
        }
    }
}
