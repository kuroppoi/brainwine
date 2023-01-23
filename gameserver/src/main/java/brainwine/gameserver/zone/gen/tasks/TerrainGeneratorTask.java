package brainwine.gameserver.zone.gen.tasks;

import brainwine.gameserver.item.Layer;
import brainwine.gameserver.util.SimplexNoise;
import brainwine.gameserver.util.WeightedMap;
import brainwine.gameserver.zone.gen.GeneratorConfig;
import brainwine.gameserver.zone.gen.GeneratorContext;
import brainwine.gameserver.zone.gen.models.TerrainType;
import brainwine.gameserver.zone.gen.surface.SurfaceRegion;
import brainwine.gameserver.zone.gen.surface.SurfaceRegionType;

public class TerrainGeneratorTask implements GeneratorTask {
    
    private final TerrainType type;
    private final double minAmplitude;
    private final double maxAmplitude;
    private final int surfaceRegionSize;
    private final WeightedMap<SurfaceRegionType> surfaceRegionTypes;
    
    public TerrainGeneratorTask(GeneratorConfig config) {
        type = config.getTerrainType();
        minAmplitude = config.getMinAmplitude();
        maxAmplitude = config.getMaxAmplitude();
        surfaceRegionSize = config.getSurfaceRegionSize();
        surfaceRegionTypes = config.getSurfaceRegionTypes();
    }
    
    @Override
    public void generate(GeneratorContext ctx) {
        int width = ctx.getWidth();
        int height = ctx.getHeight();
        int surfaceLevel = height < 600 ? height / 3 : 200;
        int lowestSurfaceLevel = 0;
        
        // Determine surface first, then start placing blocks.
        if(type == TerrainType.FILLED) {
            for(int x = 0; x < width; x++) {
                ctx.setSurface(x, 0);
            }
        } else {
            double amplitude = ctx.nextDouble() * (maxAmplitude - minAmplitude) + minAmplitude;
            
            for(int x = 0; x < width; x++) {
                int surface = (int)(SimplexNoise.noise2(ctx.getSeed(), x / 256.0, 0, 7) * amplitude) + surfaceLevel;
                ctx.setSurface(x, surface);
                
                if(surface > lowestSurfaceLevel) {
                    lowestSurfaceLevel = surface;
                }
                
                // Init surface regions
                if(!surfaceRegionTypes.isEmpty() && x % surfaceRegionSize == 0) {
                    int regionEnd = Math.min(width, x + surfaceRegionSize);
                    ctx.addSurfaceRegion(new SurfaceRegion(surfaceRegionTypes.next(ctx.getRandom()), x, regionEnd));
                }
            }
        }
        
        int heightBelowSurface = height - lowestSurfaceLevel;
        ctx.getZone().setDepths(
                (int)(heightBelowSurface * 0.25 + lowestSurfaceLevel),
                (int)(heightBelowSurface * 0.5 + lowestSurfaceLevel),
                (int)(heightBelowSurface * 0.75 + lowestSurfaceLevel));
        
        // Place the blocks!
        for(int x = 0; x < width; x++) {
            int surface = ctx.getSurface(x);
            
            // Only generate a thin layer for asteroids, cave gen will take care of the rest.
            for(int y = surface; y < (type == TerrainType.ASTEROIDS ? surface + 6 : height); y++) {
                ctx.updateBlock(x, y, Layer.FRONT, ctx.getEarthLayer(y));
                ctx.updateBlock(x, y, Layer.BASE, "base/earth");
            }
        }
    }
}
