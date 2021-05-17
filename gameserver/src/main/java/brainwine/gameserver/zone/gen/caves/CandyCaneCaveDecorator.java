package brainwine.gameserver.zone.gen.caves;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.item.Layer;
import brainwine.gameserver.util.WeightedList;
import brainwine.gameserver.zone.gen.CaveDecorator;
import brainwine.gameserver.zone.gen.GeneratorContext;
import brainwine.gameserver.zone.gen.models.BlockPosition;
import brainwine.gameserver.zone.gen.models.Cave;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CandyCaneCaveDecorator extends CaveDecorator {
    
    @JsonIgnore
    private final WeightedList<Integer> candyCanes = new WeightedList<>();
    
    @JsonProperty("candy_cane_chance")
    private double rate = 0.15;
    
    public CandyCaneCaveDecorator() {
        candyCanes.addEntry(460, 6);
        candyCanes.addEntry(461, 1);
    }
    
    @Override
    public void decorate(GeneratorContext ctx, Cave cave) {
        for(BlockPosition block : cave.getFloorBlocks()) {
            if(ctx.nextDouble() <= rate) {
                ctx.updateBlock(block.getX(), block.getY(), Layer.FRONT, candyCanes.next(ctx.getRandom()));
            }
        }
    }
}
