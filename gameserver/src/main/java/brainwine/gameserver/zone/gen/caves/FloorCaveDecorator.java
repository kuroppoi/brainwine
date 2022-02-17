package brainwine.gameserver.zone.gen.caves;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.util.WeightedMap;
import brainwine.gameserver.zone.gen.GeneratorContext;
import brainwine.gameserver.zone.gen.models.BlockPosition;

public class FloorCaveDecorator extends CaveDecorator {
    
    @JsonProperty("single_type")
    private boolean singleType = false;
    
    @JsonProperty("item_spawn_chance")
    private double spawnChance = 0.1;
    
    @JsonProperty("items")
    private WeightedMap<Item> items = new WeightedMap<>();
    
    @JsonProperty("mods")
    private Map<Item, WeightedMap<Integer>> mods = new HashMap<>();
    
    @Override
    public void decorate(GeneratorContext ctx, Cave cave) {
        if(!items.isEmpty()) {
            Item item = singleType ? items.next(ctx.getRandom()) : null;
            
            for(BlockPosition block : cave.getFloorBlocks()) {
                if(ctx.nextDouble() <= spawnChance) {
                    if(!singleType) {
                        item = items.next(ctx.getRandom());
                    }
                    
                    int mod = 0;
                    
                    if(mods.containsKey(item)) {
                        mod = mods.get(item).next(ctx.getRandom());
                    }
                    
                    ctx.updateBlock(block.getX(), block.getY(), item.getLayer(), item, mod);
                }
            }
        }
    }
}
