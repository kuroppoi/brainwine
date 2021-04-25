package brainwine.gameserver.zone.gen;

import brainwine.gameserver.item.Layer;
import brainwine.gameserver.zone.Biome;

public class OreGeneratorTask implements GeneratorTask {

	// Copper, Zinc, Iron, Quartz, Sulfur, Marble
    private final int[] plainOres = {
    	560, 561, 562, 563, 564, 565, 567
    };

	// Copper, Zinc, Iron, Quartz, Marble, Diamond
    private final int[] arcticOres = {
        560, 561, 562, 563, 565, 567, 568
    };

	// Copper, Zinc, Iron, Quartz, Sulfur, Bloodstone
    private final int[] hellOres = {
    	560, 561, 562, 563, 564, 565, 566
    };

	// Copper, Zinc, Iron, Quartz, Beryllium
    private final int[] desertOres = {
    	560, 561, 562, 563, 565, 569
    };

	// Copper, Zinc, Iron, Quartz
    private final int[] brainOres = {
        560, 561, 562, 563, 565
    };

	// Copper, Zinc, Iron, Sulfur
    private final int[] deepOres = {
        560, 561, 562, 563, 564
    };

	// Copper, Zinc, Iron, Quartz, Marble, Platinum
    private final int[] spaceOres = {
    	560, 561, 562, 565, 567, 59
    };
    
    @Override
    public void generate(GeneratorContext ctx) {
        for(int x = 0; x < ctx.getWidth(); x++) {
            for(int y = 0; y < ctx.getHeight(); y++) {
                if(ctx.isUnderground(x, y)) {
                    if(!ctx.getBlock(x, y).getFrontItem().isAir()) {
                        if(ctx.areCoordinatesInBounds(x, y - 1)) {
                            if(ctx.getBlock(x, y - 1).getFrontItem().isWhole() && ctx.nextDouble() < 0.35) {
                                ctx.updateBlock(x, y, Layer.FRONT, ore(ctx), 2);
                            }
                        }
                        
                        if(ctx.areCoordinatesInBounds(x, y + 1)) {
                            if(ctx.getBlock(x, y + 1).getFrontItem().isWhole() && ctx.nextDouble() < 0.35) {
                                ctx.updateBlock(x, y, Layer.FRONT, ore(ctx));
                            }
                        }
                    }
                }
            }
        }
    }
    
    private int ore(GeneratorContext ctx) {
    	int ran = (int)(ctx.nextDouble());
    	if (ctx.getBiome() == Biome.PLAIN)
    		return plainOres[(ran * plainOres.length)];
    	else if (ctx.getBiome() == Biome.ARCTIC)
    		return arcticOres[(ran * arcticOres.length)];
    	else if (ctx.getBiome() == Biome.HELL)
    		return hellOres[(ran * hellOres.length)];
    	else if (ctx.getBiome() == Biome.DESERT)
    		return desertOres[(ran * desertOres.length)];
    	else if (ctx.getBiome() == Biome.BRAIN)
    		return brainOres[(ran * brainOres.length)];
    	else if (ctx.getBiome() == Biome.DEEP)
    		return deepOres[(ran * deepOres.length)];
    	else if (ctx.getBiome() == Biome.SPACE)
    		return spaceOres[(ran * spaceOres.length)];
    	else 
    		return plainOres[(ran * plainOres.length)];
    }
}
