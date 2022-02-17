package brainwine.gameserver.zone.gen.caves;

import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.prefab.Prefab;
import brainwine.gameserver.util.WeightedMap;
import brainwine.gameserver.zone.gen.GeneratorContext;
import brainwine.gameserver.zone.gen.models.BlockPosition;

public class StructureCaveDecorator extends CaveDecorator {
    
    @JsonProperty("prefabs")
    private WeightedMap<Prefab> prefabs = new WeightedMap<>();
    
    @Override
    public void decorate(GeneratorContext ctx, Cave cave) {
        if(!prefabs.isEmpty()) {
            // TODO maybe randomly offset by prefab size
            BlockPosition position = cave.getBlocks().get(ctx.nextInt(cave.getSize() - 1));
            ctx.placePrefab(prefabs.next(), position.getX(), position.getY());
        }
    }
}
