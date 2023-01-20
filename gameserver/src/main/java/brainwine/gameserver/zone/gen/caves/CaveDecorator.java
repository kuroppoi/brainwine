package brainwine.gameserver.zone.gen.caves;

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
    @Type(name = "item", value = ItemCaveDecorator.class),
    @Type(name = "mushroom", value = MushroomCaveDecorator.class),
    @Type(name = "fill", value = FillCaveDecorator.class),
    @Type(name = "structure", value = StructureCaveDecorator.class),
})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class CaveDecorator {
    
    @JsonProperty("chance")
    private double chance = 1.0;
    
    public abstract void decorate(GeneratorContext ctx, Cave cave);
    
    public double getChance() {
        return chance;
    }
}
