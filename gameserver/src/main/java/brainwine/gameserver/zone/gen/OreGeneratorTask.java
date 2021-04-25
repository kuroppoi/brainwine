package brainwine.gameserver.zone.gen;

import brainwine.gameserver.item.Layer;
import brainwine.gameserver.zone.Biome;

public class OreGeneratorTask implements GeneratorTask {

	// Copper, Zinc, Iron, Quartz, Sulfur, Lead, Marble
    private final int[] plainOres = {
    	550, 551, 552, 553, 554, 555, 557
    };

	// Copper, Zinc, Iron, Quartz, Lead, Marble, Diamond
    private final int[] arcticOres = {
    	550, 551, 552, 553, 555, 557, 558
    };

	// Copper, Zinc, Iron, Quartz, Sulfur, Lead, Bloodstone
    private final int[] hellOres = {
    	550, 551, 552, 553, 554, 555, 556
    };

	// Copper, Zinc, Iron, Quartz, Lead, Beryllium
    private final int[] desertOres = {
    	550, 551, 552, 553, 555, 559
    };

	// Copper, Zinc, Iron, Quartz, Lead
    private final int[] brainOres = {
        550, 551, 552, 553, 555
    };

	// Copper, Zinc, Iron, Quartz, Sulfur
    private final int[] deepOres = {
        550, 551, 552, 553, 554
    };

	// Copper, Zinc, Iron, Quartz, Marble, Platinum
    private final int[] spaceOres = {
    	550, 551, 552, 555, 557, 60
    };
    
    @Override
    public void generate(GeneratorContext ctx) {
        for(int x = 0; x < ctx.getWidth(); x++) {
            for(int y = 0; y < ctx.getHeight(); y++) {
                if(ctx.isUnderground(x, y)) {
                    if(!ctx.getBlock(x, y).getFrontItem().isAir()) {
                        if(ctx.areCoordinatesInBounds(x, y - 1)) {
                            if(ctx.getBlock(x, y - 1).getFrontItem().isWhole() && ctx.nextDouble() < 0.15) {
                                ctx.updateBlock(x, y, Layer.FRONT, ore(ctx), 2);
                            }
                        }
                        
                        if(ctx.areCoordinatesInBounds(x, y + 1)) {
                            if(ctx.getBlock(x, y + 1).getFrontItem().isWhole() && ctx.nextDouble() < 0.15) {
                                ctx.updateBlock(x, y, Layer.FRONT, ore(ctx));
                            }
                        }
                    }
                }
            }
        }
    }
    
    private int ore(GeneratorContext ctx) {
    	if (ctx.getBiome() == Biome.PLAIN)
    		return plainOres[(int)(ctx.nextDouble() * plainOres.length)];
    	else if (ctx.getBiome() == Biome.ARCTIC)
    		return arcticOres[(int)(ctx.nextDouble() * arcticOres.length)];
    	else if (ctx.getBiome() == Biome.HELL)
    		return hellOres[(int)(ctx.nextDouble() * hellOres.length)];
    	else if (ctx.getBiome() == Biome.DESERT)
    		return desertOres[(int)(ctx.nextDouble() * desertOres.length)];
    	else if (ctx.getBiome() == Biome.BRAIN)
    		return brainOres[(int)(ctx.nextDouble() * brainOres.length)];
    	else if (ctx.getBiome() == Biome.DEEP)
    		return deepOres[(int)(ctx.nextDouble() * deepOres.length)];
    	else if (ctx.getBiome() == Biome.SPACE)
    		return spaceOres[(int)(ctx.nextDouble() * spaceOres.length)];
    	else 
    		return plainOres[(int)(ctx.nextDouble() * plainOres.length)];
    }
}
