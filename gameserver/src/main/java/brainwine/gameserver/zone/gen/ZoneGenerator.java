package brainwine.gameserver.zone.gen;

public class ZoneGenerator {
    
    private final GeneratorTask terrainGenerator;
    private final GeneratorTask caveGenerator;
    private final GeneratorTask decorGenerator;
    
    public ZoneGenerator() {
        this(new GeneratorConfig());
    }
    
    public ZoneGenerator(GeneratorConfig config) {
        terrainGenerator = new TerrainGenerator(config);
        caveGenerator = new CaveGenerator(config);
        decorGenerator = new DecorGenerator(config);
    }
    
    public void generate(GeneratorContext ctx) {
        terrainGenerator.generate(ctx);
        caveGenerator.generate(ctx);
        decorGenerator.generate(ctx);
    }
}
