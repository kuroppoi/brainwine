package brainwine.gameserver.zone.gen.caves;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ModType;
import brainwine.gameserver.util.Vector2i;
import brainwine.gameserver.util.WeightedMap;
import brainwine.gameserver.zone.gen.GeneratorContext;

public class ItemCaveDecorator extends CaveDecorator {
    
    @JsonProperty("single_type")
    protected boolean singleType;
    
    @JsonProperty("floor")
    protected boolean floor = true;
    
    @JsonProperty("ceiling")
    protected boolean ceiling;
    
    @JsonProperty("walls")
    protected boolean walls;
    
    @JsonProperty("corners_only")
    protected boolean cornersOnly;
    
    @JsonProperty("max_items")
    protected int maxItems;
    
    @JsonProperty("item_spawn_chance")
    protected double itemSpawnChance = 0.1;
    
    @JsonProperty("items")
    protected WeightedMap<Item> items = new WeightedMap<>();
    
    @JsonProperty("mods")
    protected Map<Item, WeightedMap<Integer>> mods = new HashMap<>();
    
    @JsonCreator
    protected ItemCaveDecorator() {}
    
    @Override
    public void decorate(GeneratorContext ctx, Cave cave) {
        // Return if there are no items to generate
        if(items.isEmpty()) {
            return;
        }
        
        int itemCount = 0;
        Item item = singleType ? items.next(ctx.getRandom()) : null;
        
        for(Vector2i block : cave.getBlocks()) {
            int x = block.getX();
            int y = block.getY();
            
            // TODO isSolid not safe because checked blocks might not be part of the cave itself!
            boolean floor = this.floor && ctx.isSolid(x, y + 1);
            boolean ceiling = this.ceiling && ctx.isSolid(x, y - 1);
            boolean wall = walls && (ctx.isSolid(x - 1, y) || ctx.isSolid(x + 1, y));
            boolean corner = cornersOnly && (ctx.isSolid(x - 1, y) || ctx.isSolid(x + 1, y)) && (floor || ceiling);
            
            // Skip if this block fits none of the criteria or the spawn try fails
            if((cornersOnly && !corner) || (!floor && !ceiling && !wall) || ctx.nextDouble() > itemSpawnChance) {
                continue;
            }
            
            // Get next item unless this decorator is single-typed
            if(!singleType) {
                item = items.next(ctx.getRandom());
            }
            
            int mod = 0;
            
            // If mods are defined for this item, get a random possible one.
            // Otherwise, if the mod type is rotation, automatically assign the proper rotation mod
            // based on whether the item is generated on the floor, ceiling, wall or corner.
            if(mods.containsKey(item)) {
                mod = mods.get(item).next(ctx.getRandom());
            } else if(item.getMod() == ModType.ROTATION) {
                if(corner) {
                    mod = ctx.isSolid(x - 1, y) ? (ceiling ? 3 : 2) : (ceiling ? 0 : 1);
                } else if(ceiling) {
                    mod = 2;
                } else if(!floor && wall) {
                    mod = ctx.isSolid(x - 1, y) ? 1 : 3;
                }
            }
            
            ctx.updateBlock(block.getX(), block.getY(), item.getLayer(), item, mod);
            itemCount++;
            
            // Stop if we've reached the maximum amount of items
            if(maxItems > 0 && itemCount >= maxItems) {
                break;
            }
        }
    }
}
