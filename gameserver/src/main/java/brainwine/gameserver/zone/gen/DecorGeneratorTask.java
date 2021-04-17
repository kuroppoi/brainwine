package brainwine.gameserver.zone.gen;

import brainwine.gameserver.item.Layer;

public class DecorGeneratorTask implements GeneratorTask {
    
    private final int[] stalagmites = {
        570, 571, 572, 573, 574
    };
    
    private final int[] surfaceRubble = {
        940, 941, 942, 943, 944, 945, 946, 947,
        950, 951,
        954, 955, 956, 957, 958, 959,
        960
    };
    
    @Override
    public void generate(GeneratorContext ctx) {
        for(int x = 0; x < ctx.getWidth(); x++) {
            for(int y = 0; y < ctx.getHeight(); y++) {
                if(ctx.isUnderground(x, y)) {
                    // Populate caves
                    if(ctx.getBlock(x, y).getFrontItem().isAir()) {
                        if(ctx.areCoordinatesInBounds(x, y - 1)) {
                            if(ctx.getBlock(x, y - 1).getFrontItem().isWhole() && ctx.nextDouble() < 0.15) {
                                ctx.updateBlock(x, y, Layer.FRONT, decor(ctx, stalagmites), 2);
                            }
                        }
                        
                        if(ctx.areCoordinatesInBounds(x, y + 1)) {
                            if(ctx.getBlock(x, y + 1).getFrontItem().isWhole() && ctx.nextDouble() < 0.15) {
                                ctx.updateBlock(x, y, Layer.FRONT, decor(ctx, stalagmites));
                            }
                        }
                    }
                } else {
                    // Populate surface
                    if(ctx.getSurface()[x] - 1 == y && ctx.getBlock(x, y + 1).getFrontItem().isWhole()) {
                        double rand = ctx.nextDouble();
                        if(rand < 0.07) {
                            ctx.updateBlock(x, y, Layer.FRONT, decor(ctx, surfaceRubble));
                        } else if(rand < 0.3) {
                            ctx.updateBlock(x, y, Layer.FRONT, 710);
                        }
                    }
                }
            }
        }
        
        // rocks
        for(int i = 0; i < 3600; i++) {
            int x = ctx.nextInt(ctx.getWidth());
            int y = ctx.nextInt(ctx.getHeight());
            double size = ctx.nextDouble();
            
            if(size < 0.3) {
                placeDecor(ctx, x, y, 528, 1, 1);
            } else if(size < 0.6) {
                placeDecor(ctx, x, y, 529, 2, 1);
            } else {
                placeDecor(ctx, x, y, 530, 2, 2);
            }
        }
        
        // roots
        for(int i = 0; i < 3600; i++) {
            int x = ctx.nextInt(ctx.getWidth());
            int y = ctx.nextInt(ctx.getHeight());
            double size = ctx.nextDouble();
            
            if(size < 0.2) {
                placeDecor(ctx, x, y, 534, 2, 1);
            } else if(size < 0.4) {
                placeDecor(ctx, x, y, 535, 2, 1);
            } else if(size < 0.6) {
                placeDecor(ctx, x, y, 536, 1, 2);
            } else if(size < 0.8) {
                placeDecor(ctx, x, y, 537, 2, 2);
            } else {
                placeDecor(ctx, x, y, 538, 3, 3);
            }
        }
        
        // logs
        for(int i = 0; i < 3600; i++) {
            int x = ctx.nextInt(ctx.getWidth());
            int y = ctx.nextInt(ctx.getHeight());
            double size = ctx.nextDouble();
            
            if(size < 0.2) {
                placeDecor(ctx, x, y, 522, 2, 1);
            } else if(size < 0.4) {
                placeDecor(ctx, x, y, 523, 2, 1);
            } else if(size < 0.6) {
                placeDecor(ctx, x, y, 524, 2, 1);
            } else if(size < 0.8) {
                placeDecor(ctx, x, y, 525, 2, 1);
            } else {
                placeDecor(ctx, x, y, 526, 4, 2);
            }
        }
    }
    
    private void placeDecor(GeneratorContext ctx, int x, int y, int item, int width, int height) {
        for(int i = x; i < x + width; i++) {
            for(int j = y; j < y + height; j++) {
                if(!ctx.areCoordinatesInBounds(i, j) || !ctx.getBlock(i, j).getFrontItem().isWhole()) {
                    return;
                }
            }
        }
        
        for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++) {
                int mod = j * width + i;
                ctx.updateBlock(x + i, y + j, Layer.FRONT, item, mod);
            }
        }
    }
    
    private int decor(GeneratorContext ctx, int[] items) {
        return items[(int)(ctx.nextDouble() * items.length)];
    }
}
