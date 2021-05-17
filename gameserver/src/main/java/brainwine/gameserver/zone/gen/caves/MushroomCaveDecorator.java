package brainwine.gameserver.zone.gen.caves;

import java.beans.ConstructorProperties;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.item.Layer;
import brainwine.gameserver.util.WeightedList;
import brainwine.gameserver.zone.gen.CaveDecorator;
import brainwine.gameserver.zone.gen.GeneratorContext;
import brainwine.gameserver.zone.gen.models.BlockPosition;
import brainwine.gameserver.zone.gen.models.Cave;
import brainwine.gameserver.zone.gen.models.MushroomType;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MushroomCaveDecorator extends CaveDecorator {
    
    @JsonIgnore
    private final WeightedList<MushroomType> list = new WeightedList<>();
    
    @JsonProperty("mushroom_chance")
    private double rate = 0.2;
    
    @JsonProperty("elder_chance")
    private double elderRate = 0.05;
    
    @ConstructorProperties({"mushrooms"})
    public MushroomCaveDecorator(Map<MushroomType, Integer> mushrooms) {
        if(mushrooms != null) {
            mushrooms.forEach((k, v) -> {
                list.addEntry(k, v);
            });
        }
    }
    
    @Override
    public void decorate(GeneratorContext ctx, Cave cave) {
        if(!list.isEmpty()) {
            MushroomType mushroom = list.next(ctx.getRandom());
            
            for(BlockPosition block : cave.getFloorBlocks()) {
                if(ctx.nextDouble() <= rate) {
                    int x = block.getX();
                    int y = block.getY();
                    int item = mushroom.getItem();
                    
                    if(mushroom.hasStalk()) {
                        growMushroom(ctx, x, y, item, mushroom.getStalk(), mushroom.getMaxHeight());
                    } else {
                        if(mushroom.hasElder() && ctx.nextDouble() <= elderRate) {
                            ctx.updateBlock(x, y, Layer.FRONT, mushroom.getElder());
                        } else {
                            ctx.updateBlock(x, y, Layer.FRONT, item);
                        }
                    }
                }
            }
        }
    }
    
    private void growMushroom(GeneratorContext ctx, int x, int y, int item, int stalk, int maxHeight) {
        int space = 0;
        
        for(int i = y; i > y - maxHeight; i--) {
            if(ctx.inBounds(x, i)) {
                if(!ctx.getZone().getBlock(x, i).getFrontItem().isAir()) {
                    break;
                }
                
                space++;
            }
        }
        
        int height = 1;
        double growChance = 0.5;
        
        for(int i = 0; i < space; i++) {
            if(ctx.nextDouble() <= growChance) {
                height++;
                growChance *= 0.5;
            } else {
                break;
            }
        }
        
        for(int i = y; i > y - height; i--) {
            ctx.updateBlock(x, i, Layer.FRONT, i - 1 == y - height ? item : stalk);
        }
    }
}
