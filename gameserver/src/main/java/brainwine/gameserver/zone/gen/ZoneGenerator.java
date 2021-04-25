package brainwine.gameserver.zone.gen;

import brainwine.gameserver.item.Layer;
import brainwine.gameserver.zone.Biome;
import brainwine.gameserver.zone.Zone;

/**
 * TODO rewrite the entire zone gen system
 */
public class ZoneGenerator {
    
    public static Zone generateZone(Biome biome, int width, int height) {
        GeneratorContext ctx = new GeneratorContext(biome, width, height);
        
        // Prepare tasks...
        TerrainGeneratorTask terrainGen = new TerrainGeneratorTask();
        CaveGeneratorTask caveGen = new CaveGeneratorTask();
        OreGeneratorTask oreGen = new OreGeneratorTask();
        DecorGeneratorTask decorGen = new DecorGeneratorTask();
        BedrockGeneratorTask bedrockGen = new BedrockGeneratorTask();
        
        // ...And execute them!
        terrainGen.generate(ctx);
        caveGen.generate(ctx);
        oreGen.generate(ctx);
        decorGen.generate(ctx);
        bedrockGen.generate(ctx);
        
        ctx.updateBlock(width / 2, ctx.getSurface()[width / 2] - 1, Layer.FRONT, 891);
        
        return ctx.constructZone();
    }
}
