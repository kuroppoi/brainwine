package brainwine.gameserver.zone.gen.sky;

import brainwine.gameserver.prefab.Prefab;
import brainwine.gameserver.util.WeightedMap;
import brainwine.gameserver.zone.gen.GeneratorContext;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class StructureSkyDecorator extends SkyDecorator {
    @JsonProperty("prefabs")
    protected WeightedMap<Prefab> prefabs = new WeightedMap<>();

    @JsonCreator
    protected StructureSkyDecorator() {}

    @Override
    public void decorate(GeneratorContext ctx, int x, int y) {
        if(!prefabs.isEmpty()) {
            Prefab prefab = prefabs.next(ctx.getRandom());
            x -= (int)(ctx.nextDouble() * prefab.getWidth());
            y -= (int)(ctx.nextDouble() * prefab.getHeight());
            ctx.placePrefab(prefab, x, y);
        }
    }
}
