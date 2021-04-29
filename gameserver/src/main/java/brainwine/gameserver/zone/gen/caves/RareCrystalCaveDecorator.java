package brainwine.gameserver.zone.gen.caves;

import java.beans.ConstructorProperties;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.util.WeightedList;
import brainwine.gameserver.zone.gen.GeneratorContext;
import brainwine.gameserver.zone.gen.models.BlockPosition;
import brainwine.gameserver.zone.gen.models.Cave;
import brainwine.gameserver.zone.gen.models.CaveDecorator;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RareCrystalCaveDecorator extends CaveDecorator {
    
    @JsonIgnore
    private final WeightedList<Item> list = new WeightedList<>();
    
    @JsonProperty("crystal_chance")
    private double rate = 0.05;
    
    @ConstructorProperties({"crystals"})
    public RareCrystalCaveDecorator(Map<Item, Integer> crystals) {
        if(crystals != null) {
            crystals.forEach((k, v) -> {
                list.addEntry(k, v);
            });
        }
    }
    
    @Override
    public void decorate(GeneratorContext ctx, Cave cave) {
        if(!list.isEmpty()) {
            Item crystal = list.next(ctx.getRandom());
            
            for(BlockPosition block : cave.getFloorBlocks()) {
                if(ctx.nextDouble() <= rate) {
                    ctx.updateBlock(block.getX(), block.getY(), Layer.FRONT, crystal);
                }
            }
        }
    }
}
