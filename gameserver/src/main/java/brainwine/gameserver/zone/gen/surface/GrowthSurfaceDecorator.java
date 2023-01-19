package brainwine.gameserver.zone.gen.surface;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.util.Vector2i;
import brainwine.gameserver.util.WeightedMap;
import brainwine.gameserver.zone.gen.GeneratorContext;
import brainwine.gameserver.zone.gen.models.GrowthSegment;

public class GrowthSurfaceDecorator extends SurfaceDecorator {
    
    @JsonProperty("max_spawns")
    private int maxSpawns;
    
    @JsonProperty("spawn_chance")
    protected double spawnChance = 0.25;
    
    @JsonProperty("grow_chance")
    protected double growChance = 0.5;
    
    @JsonProperty("spacing")
    protected int spacing = 3;
    
    @JsonProperty("min_height")
    protected int minHeight = 2;
    
    @JsonProperty("max_height")
    protected int maxHeight = 7;
    
    @JsonProperty("bottom")
    protected Item bottomItem;
    
    @JsonProperty("middle")
    protected Item middleItem;
    
    @JsonProperty("top")
    protected Item topItem;
    
    @JsonProperty("segments")
    protected WeightedMap<GrowthSegment> segments = new WeightedMap<>();
    
    @JsonCreator
    protected GrowthSurfaceDecorator() {}
    
    @Override
    public void decorate(GeneratorContext ctx, SurfaceRegion region) {
        int spawnCount = 0;
        int x = region.getStart();
        
        while(x < region.getEnd()) {
            int surface = ctx.getZone().getSurface()[x];
            boolean placed = false;
            
            // If we're on an earthy block and the spawn try succeeds
            if(ctx.isEarthy(x, surface) && ctx.nextDouble() <= spawnChance) {
                int y = surface - 2;
                int height = 0;
                
                // Place bottom item
                ctx.updateBlock(x, surface - 1, bottomItem.getLayer(), bottomItem);
                
                // While we haven't reached the maximum height
                // and we either haven't reached the minimum height or it succeeds to grow
                while(height < maxHeight && (height < minHeight || ctx.nextDouble() <= growChance)) {
                    int blockHeight = 0;
                    
                    // Place middle item if there are no segments, otherwise place a random segment
                    if(segments.isEmpty()) {
                        ctx.updateBlock(x, y, middleItem.getLayer(), middleItem);
                        blockHeight = middleItem.getBlockHeight();
                    } else {
                        GrowthSegment segment = segments.next(ctx.getRandom());
                        Vector2i offset = segment.getOffset();
                        Item item = segment.getItem();
                        
                        // Place middle item if the block at the current position plus offset is already occupied,
                        // otherwise place the segment as normal
                        if(ctx.isOccupied(x + offset.getX(), y + offset.getY(), item.getLayer())) {
                            ctx.updateBlock(x, y, middleItem.getLayer(), middleItem);
                            blockHeight = middleItem.getBlockHeight();
                        } else {
                            ctx.updateBlock(x + offset.getX(), y + offset.getY(), item.getLayer(), item);
                            blockHeight = item.getBlockHeight();
                        }
                    }
                    
                    y -= blockHeight;
                    height += blockHeight;
                }
                
                // Finally, place the top item!
                ctx.updateBlock(x, y, topItem.getLayer(), topItem);
                spawnCount++;
                placed = true;
            }
            
            // Stop if we've reached the maximum amount of spawns
            if(maxSpawns > 0 && spawnCount >= maxSpawns) {
                break;
            }
            
            x += placed ? spacing : 1;
        }
    }
    
    @JsonSetter("segments")
    protected void setSegments(Collection<GrowthSegment> segments) {
        this.segments = new WeightedMap<>(segments, GrowthSegment::getFrequency);
    }
}
