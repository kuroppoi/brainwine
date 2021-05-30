package brainwine.gameserver.zone.gen;

import brainwine.gameserver.item.Layer;

public class ZoneGenerator {
    
    private final GeneratorTask terrainGenerator;
    private final GeneratorTask caveGenerator;
    private final GeneratorTask decorGenerator;
    private final GeneratorTask structureGenerator;
    
    public ZoneGenerator() {
        this(new GeneratorConfig());
    }
    
    public ZoneGenerator(GeneratorConfig config) {
        terrainGenerator = new TerrainGenerator(config);
        caveGenerator = new CaveGenerator(config);
        decorGenerator = new DecorGenerator(config);
        structureGenerator = new StructureGenerator(config);
    }
    
    public void generate(GeneratorContext ctx) {
        int width = ctx.getWidth();
        int height = ctx.getHeight();
        ctx.createChunks();
        terrainGenerator.generate(ctx);
        caveGenerator.generate(ctx);
        decorGenerator.generate(ctx);
        structureGenerator.generate(ctx);
        
        for(int x = 0; x < width; x++) {
            ctx.updateBlock(x, height - 1, Layer.FRONT, 666, 0);
        }
    }
}
