package brainwine.gameserver.zone.gen;

import brainwine.gameserver.item.Layer;

public class TerrainGeneratorTask implements GeneratorTask {
    
    @Override
    public void generate(GeneratorContext ctx) {
        PerlinNoise noise = new PerlinNoise(ctx.getSeed());
        int zoneHeight = ctx.getHeight() / 4;
        double amplitude = ctx.nextDouble() * 40 + 40; // Randomize the amplitude as well to give each zone a bit of personality ;)
        
        for(int x = 0; x < ctx.getWidth(); x++) {
            int height = (int)(noise.perlinNoise((x + 1) * 0.01, 0.5, 7) * amplitude) + zoneHeight;

            for(int y = 0; y < ctx.getHeight(); y++) {
                if(y >= height) {
                    ctx.updateBlock(x, y, Layer.BASE, 2);
                    
                    float heightScale = (float)y / ctx.getHeight();
                    
                    if(heightScale > 0.8) {
                        ctx.updateBlock(x, y, Layer.FRONT, 518);
                    } else if(heightScale > 0.6) {
                        ctx.updateBlock(x, y, Layer.FRONT, 517);
                    } else if(heightScale > 0.4) {
                        ctx.updateBlock(x, y, Layer.FRONT, 516);
                    } else {
                        ctx.updateBlock(x, y, Layer.FRONT, 512);
                    }
                }
            }
        }
    }
}
