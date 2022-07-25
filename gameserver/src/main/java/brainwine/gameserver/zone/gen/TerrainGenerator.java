package brainwine.gameserver.zone.gen;

import brainwine.gameserver.item.Layer;

public class TerrainGenerator implements GeneratorTask {
    
    private final TerrainType type;
    
    public TerrainGenerator(GeneratorConfig config) {
        type = config.getTerrainType();
    }
    
    @Override
    public void generate(GeneratorContext ctx) {
        int width = ctx.getWidth();
        int height = ctx.getHeight();
        int surfaceLevel = height < 600 ? height / 3 : 200;
        int lowestSurfaceLevel = 0;
        boolean asteroids = type == TerrainType.ASTEROIDS;
        
        // Determine surface first, then start placing blocks.
        if(type == TerrainType.FILLED) {
            for(int x = 0; x < width; x++) {
                ctx.getZone().setSurface(x, 0);
            }
        } else {
            PerlinNoise noise = new PerlinNoise(ctx.getSeed());
            double minAmplitude = asteroids ? 10 : 40;
            double maxAmplitude = asteroids ? 20 : 80;
            double amplitude = ctx.nextDouble() * minAmplitude + (maxAmplitude - minAmplitude);
            
            for(int x = 0; x < width; x++) {
                int surface = (int)(noise.perlinNoise(x * 0.01, 0.5, 7) * amplitude) + surfaceLevel;
                ctx.getZone().setSurface(x, surface);
                
                if(surface > lowestSurfaceLevel) {
                    lowestSurfaceLevel = surface;
                }
            }
        }
        
        int heightBelowSurface = height - lowestSurfaceLevel;
        ctx.getZone().setDepths(
                (int)(heightBelowSurface * 0.25 + lowestSurfaceLevel),
                (int)(heightBelowSurface * 0.5 + lowestSurfaceLevel),
                (int)(heightBelowSurface * 0.75 + lowestSurfaceLevel));
        
        for(int x = 0; x < width; x++) {
            int surface = ctx.getZone().getSurface()[x];
            
            // Only generate a thin layer for asteroids, cave gen will take care of the rest.
            for(int y = surface; y < (asteroids ? surface + 6 : height); y++) {
                ctx.updateBlock(x, y, Layer.FRONT, ctx.getEarthLayer(y));
                ctx.updateBlock(x, y, Layer.BASE, 2);
            }
        }
    }
}
