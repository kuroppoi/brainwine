package brainwine.gameserver.zone.gen.surface;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.util.WeightedMap;
import brainwine.gameserver.zone.gen.GeneratorContext;

public class ItemSurfaceDecorator extends SurfaceDecorator {
    
    @JsonProperty("single_type")
    protected boolean singleType;
    
    @JsonProperty("max_items")
    protected int maxItems;
    
    @JsonProperty("item_spawn_chance")
    protected double itemSpawnChance = 0.15;
    
    @JsonProperty("items")
    protected WeightedMap<Item> items = new WeightedMap<>();
    
    @JsonProperty("mods")
    protected Map<Item, WeightedMap<Integer>> mods = new HashMap<>();
    
    @JsonCreator
    protected ItemSurfaceDecorator() {}
    
    @Override
    public void decorate(GeneratorContext ctx, SurfaceRegion region) {
        // Return if there are no items to generate
        if(items.isEmpty()) {
            return;
        }
        
        int itemCount = 0;
        Item item = singleType ? items.next(ctx.getRandom()) : null;
        
        for(int x = region.getStart(); x < region.getEnd(); x++) {
            // Skip if spawn try failed
            if(ctx.nextDouble() > itemSpawnChance) {
                continue;
            }
            
            // Get next item unless this decorator is single-typed
            if(!singleType) {
                item = items.next(ctx.getRandom());
            }
            
            int surface = ctx.getZone().getSurface()[x];
            
            // Check if we're on earth and not right above a cave or something
            if(ctx.isEarthy(x, surface) && ctx.isEarthy(x + (item.getBlockWidth() - 1), surface)) {
                int y = surface - 1;
                Layer layer = item.getLayer();
                int mod = 0;
                
                // Get random mod if applicable
                if(mods.containsKey(item)) {
                    mod = mods.get(item).next(ctx.getRandom());
                }
                
                ctx.updateBlock(x, y, layer, item, mod);
                itemCount++;
            }
            
            // Stop if we've reached the maximum amount of items
            if(maxItems > 0 && itemCount >= maxItems) {
                break;
            }
        }
    }
}
