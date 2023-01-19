package brainwine.gameserver.zone.gen.caves;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.prefab.Prefab;
import brainwine.gameserver.util.Vector2i;
import brainwine.gameserver.util.WeightedMap;
import brainwine.gameserver.zone.gen.GeneratorContext;

public class StructureCaveDecorator extends CaveDecorator {
    
    @JsonProperty("prefabs")
    protected WeightedMap<Prefab> prefabs = new WeightedMap<>();
    
    @JsonCreator
    protected StructureCaveDecorator() {}
    
    @Override
    public void decorate(GeneratorContext ctx, Cave cave) {
        if(!prefabs.isEmpty()) {
            List<Vector2i> blocks = cave.getBlocks();
            Vector2i position = blocks.get(ctx.nextInt(blocks.size() - 1));
            Prefab prefab = prefabs.next(ctx.getRandom());
            ctx.placePrefab(prefab, position.getX() - prefab.getWidth() / 2, position.getY() - prefab.getHeight() / 2);
        }
    }
}
