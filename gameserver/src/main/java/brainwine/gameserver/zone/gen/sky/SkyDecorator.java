package brainwine.gameserver.zone.gen.sky;

import brainwine.gameserver.zone.gen.GeneratorContext;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "structure", value = StructureSkyDecorator.class),
})
public abstract class SkyDecorator {
    @JsonProperty("chance")
    private double chance = 1.0;

    @JsonProperty("min_depth")
    private double minDepth = 0.0;

    @JsonProperty("max_depth")
    private double maxDepth = 1.0;

    @JsonProperty("min_surface_clearance")
    private int minSurfaceClearance = 0;

    @JsonProperty("max_surface_clearance")
    private int maxSurfaceClearance = Integer.MAX_VALUE;

    @JsonCreator
    protected SkyDecorator() {}

    public abstract void decorate(GeneratorContext ctx, int x, int y);

    public double getChance() {
        return chance;
    }

    public double getMinDepth() {
        return minDepth;
    }

    public double getMaxDepth() {
        return maxDepth;
    }

    public int getMinSurfaceClearance() {
        return minSurfaceClearance;
    }

    public int getMaxSurfaceClearance() {
        return maxSurfaceClearance;
    }
}
