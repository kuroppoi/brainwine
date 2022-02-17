package brainwine.gameserver.zone.gen.caves;

import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.item.Layer;
import brainwine.gameserver.util.WeightedMap;
import brainwine.gameserver.zone.gen.GeneratorContext;
import brainwine.gameserver.zone.gen.models.BlockPosition;

public class MushroomCaveDecorator extends CaveDecorator {
    
    @JsonProperty("mushrooms")
    private final WeightedMap<MushroomType> mushrooms = new WeightedMap<>();
    
    @JsonProperty("mushroom_spawn_chance")
    private double spawnChance = 0.2;
    
    @JsonProperty("elder_spawn_chance")
    private double elderSpawnChance = 0.05;
    
    @Override
    public void decorate(GeneratorContext ctx, Cave cave) {
        if(!mushrooms.isEmpty()) {
            MushroomType mushroom = mushrooms.next(ctx.getRandom());
            
            for(BlockPosition block : cave.getFloorBlocks()) {
                if(ctx.nextDouble() <= spawnChance) {
                    int x = block.getX();
                    int y = block.getY();
                    int item = mushroom.getItem();
                    
                    if(mushroom.hasStalk()) {
                        growMushroom(ctx, x, y, item, mushroom.getStalk(), mushroom.getMaxHeight());
                    } else if(mushroom.hasElder() && ctx.nextDouble() <= elderSpawnChance){
                        ctx.updateBlock(x, y, Layer.FRONT, mushroom.getElder());
                    } else {
                        ctx.updateBlock(x, y, Layer.FRONT, item);
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
