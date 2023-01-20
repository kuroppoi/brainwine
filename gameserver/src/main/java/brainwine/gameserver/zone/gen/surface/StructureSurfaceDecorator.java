package brainwine.gameserver.zone.gen.surface;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.prefab.Prefab;
import brainwine.gameserver.util.WeightedMap;
import brainwine.gameserver.zone.gen.GeneratorContext;

public class StructureSurfaceDecorator extends SurfaceDecorator {
    
    @JsonProperty("max_structures")
    protected int maxStructures = 4;
    
    @JsonProperty("structure_spawn_chance")
    protected double structureSpawnChance = 0.1;
    
    @JsonProperty("prefabs")
    protected WeightedMap<Prefab> prefabs = new WeightedMap<>();
    
    @JsonCreator
    protected StructureSurfaceDecorator() {}
    
    @Override
    public void decorate(GeneratorContext ctx, SurfaceRegion region) {
        // Return if there are no structures to try and generate
        if(prefabs.isEmpty()) {
            return;
        }
        
        int structureCount = 0;
        
        for(int x = region.getStart(); x < region.getEnd(); x++) {
            // If the spawn try succeeds
            if(ctx.nextDouble() < structureSpawnChance) {
                Prefab prefab = prefabs.next(ctx.getRandom());
                
                // Check if there is room for a structure here
                if(ctx.placePrefabSurface(prefab, x)) {
                    structureCount++;
                    x += prefab.getWidth();
                }
            }
            
            // Stop if we've reached the maximum amount of structures in this region
            if(structureCount >= maxStructures) {
                break;
            }
        }
    }
}
