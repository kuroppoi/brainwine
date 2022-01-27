package brainwine.gameserver.zone.gen;

import brainwine.gameserver.item.Layer;

public class TerrainGenerator implements GeneratorTask {
    
    private final boolean surface;
    private final double accentRate = 0.033;
    private final double drawingRate = 0.001;
    private final int[] drawings = {
        12, 13, 14
    };
    
    public TerrainGenerator(GeneratorConfig config) {
        surface = config.getSurface();
    }
    
    @Override
    public void generate(GeneratorContext ctx) {
        int width = ctx.getWidth();
        int height = ctx.getHeight();
        
        if(surface) {
            PerlinNoise noise = new PerlinNoise(ctx.getSeed());
            int surfaceLevel = height < 600 ? height / 3 : 200;
            double amplitude = ctx.nextDouble() * 40 + (80 - 40);
            
            for(int x = 0; x < width; x++) {
                int surface = (int)(noise.perlinNoise(x * 0.01, 0.5, 7) * amplitude) + surfaceLevel;
                ctx.getZone().setSurface(x, surface);
                
                for(int y = 0; y < height; y++) {
                    if(y >= surface) {
                        putTerrain(ctx, x, y);
                    }
                }
            }
        } else {
            for(int x = 0; x < width; x++) {
                ctx.getZone().setSurface(x, 0);
                
                for(int y = 0; y < height; y++) {
                    putTerrain(ctx, x, y);
                }
            }
        }
    }
    
    private void putTerrain(GeneratorContext ctx, int x, int y) {
        ctx.updateBlock(x, y, Layer.FRONT, 512);
        double d = ctx.nextDouble();
        
        if(d <= drawingRate) {
            ctx.updateBlock(x, y, Layer.BASE, drawings[ctx.nextInt(drawings.length)]);
        } else if(d <= accentRate) {
            ctx.updateBlock(x, y, Layer.BASE, 15);
        } else {
            ctx.updateBlock(x, y, Layer.BASE, 2);
        }
    }
}
