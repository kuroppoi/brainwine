package brainwine.gameserver.zone.gen.surface;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import brainwine.gameserver.zone.gen.GeneratorContext;

@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "type")
@JsonSubTypes({
    @Type(name = "item", value = ItemSurfaceDecorator.class),
    @Type(name = "tree", value = TreeSurfaceDecorator.class),
    @Type(name = "growth", value = GrowthSurfaceDecorator.class),
    @Type(name = "structure", value = StructureSurfaceDecorator.class),
    @Type(name = "rubble", value = RubbleSurfaceDecorator.class)
})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class SurfaceDecorator {
    
    @JsonProperty("chance")
    protected double chance = 1.0;
    
    public abstract void decorate(GeneratorContext ctx, SurfaceRegion region);
    
    public double getChance() {
        return chance;
    }
}
