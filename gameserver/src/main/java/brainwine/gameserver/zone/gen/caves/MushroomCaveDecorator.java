package brainwine.gameserver.zone.gen.caves;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.item.Layer;
import brainwine.gameserver.util.Vector2i;
import brainwine.gameserver.util.WeightedMap;
import brainwine.gameserver.zone.gen.GeneratorContext;

public class MushroomCaveDecorator extends CaveDecorator {
    
    @JsonProperty("mushrooms")
    protected final WeightedMap<MushroomType> mushrooms = new WeightedMap<>();
    
    @JsonProperty("mushroom_spawn_chance")
    protected double spawnChance = 0.33;
    
    @JsonProperty("elder_spawn_chance")
    protected double elderSpawnChance = 0.05;
    
    @JsonCreator
    protected MushroomCaveDecorator() {}
    
    @Override
    public void decorate(GeneratorContext ctx, Cave cave) {
        // Return if there are no mushrooms to generate
        if(mushrooms.isEmpty()) {
            return;
        }
        
        // Get random mushroom type
        MushroomType mushroom = mushrooms.next(ctx.getRandom());
        
        for(Vector2i block : cave.getFloorBlocks()) {
            // Skip if the spawn try fails
            if(ctx.nextDouble() > spawnChance) {
                continue;
            }
            
            int x = block.getX();
            int y = block.getY();
            
            // If the mushroom has a stalk, try to grow it instead of just placing it
            // Otherwise, check if it has an elder variant and try to spawn that instead
            // Lastly, just place the mushroom if everything else failed
            if(mushroom.hasStalk()) {
                growMushroom(ctx, x, y, mushroom);
            } else if(mushroom.hasElder() && ctx.nextDouble() <= elderSpawnChance){
                ctx.updateBlock(x, y, Layer.FRONT, mushroom.getElder());
            } else {
                ctx.updateBlock(x, y, Layer.FRONT, mushroom.getItem());
            }
        }
    }
    
    protected void growMushroom(GeneratorContext ctx, int x, int y, MushroomType mushroom) {
        int currentHeight = 0;
        
        // While we haven't reached the maximum height, there is space and it succeeds to grow
        while(currentHeight < mushroom.getMaxHeight() && !ctx.isOccupied(x, y - currentHeight, Layer.FRONT)
                && ctx.nextDouble() < 0.25) {
            // Place the stalk!
            ctx.updateBlock(x, y - currentHeight++, Layer.FRONT, mushroom.getStalk());
        }
        
        // Finally place the mushroom cap!
        ctx.updateBlock(x, y - currentHeight, Layer.FRONT, mushroom.getItem());
    }
}
