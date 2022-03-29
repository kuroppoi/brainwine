package brainwine.gameserver.zone.gen;

import java.util.Map;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.zone.gen.caves.Cave;
import brainwine.gameserver.zone.gen.caves.CaveDecorator;
import brainwine.gameserver.zone.gen.caves.CaveType;
import brainwine.gameserver.zone.gen.models.BaseResourceType;
import brainwine.gameserver.zone.gen.models.BlockPosition;
import brainwine.gameserver.zone.gen.models.Deposit;
import brainwine.gameserver.zone.gen.models.ModTileBlock;
import brainwine.gameserver.zone.gen.models.OreDeposit;

public class DecorGenerator implements GeneratorTask {
    
    private final Item[] surfaceFillers;
    private final Item[] speleothems;
    private final Map<BaseResourceType, Deposit> baseResources;
    private final Map<Item, OreDeposit> oreDeposits;
    
    // TODO make configurable
    private final double surfaceFillerRate = 0.3;
    private final double speleothemRate = 0.2;
    private final double accentRate = 0.033;
    private final double drawingRate = 0.001;
    private final int[] drawings = {
        12, 13, 14
    };
    
    public DecorGenerator(GeneratorConfig config) {
        surfaceFillers = config.getSurfaceFillers();
        speleothems = config.getSpeleothems();
        baseResources = config.getBaseResources();
        oreDeposits = config.getOreDeposits();
    }
    
    @Override
    public void generate(GeneratorContext ctx) {
        for(int x = 0; x < ctx.getWidth(); x++) {
            if(surfaceFillers.length > 0) {
                int y = ctx.getZone().getSurface()[x];
                
                // ha ez
                if(ctx.isEarth(x, y) && ctx.isEarth(x + 1, y) && ctx.nextDouble() <= surfaceFillerRate) {
                    ctx.updateBlock(x, y - 1, Layer.FRONT, surfaceFillers[ctx.nextInt(surfaceFillers.length)]);
                }
            }
            
            for(int y = 0; y < ctx.getHeight(); y++) {
                if(ctx.getZone().getBlock(x, y).getBaseItem().getId() == 2) {
                    double d = ctx.nextDouble();
                    
                    if(d <= drawingRate) {
                        ctx.updateBlock(x, y, Layer.BASE, drawings[ctx.nextInt(drawings.length)]);
                    } else if(d <= accentRate) {
                        ctx.updateBlock(x, y, Layer.BASE, 15);
                    }
                }
            }
        }
        
        for(Cave cave : ctx.getCaves()) {
            if(speleothems.length > 0) {
                for(BlockPosition block : cave.getCeilingBlocks()) {
                    if(ctx.nextDouble() <= speleothemRate) {
                        Item speleothem = speleothems[ctx.nextInt(speleothems.length)];
                        ctx.updateBlock(block.getX(), block.getY(), Layer.FRONT, speleothem, 2);
                    }
                }
                
                for(BlockPosition block : cave.getFloorBlocks()) {
                    if(ctx.nextDouble() <= speleothemRate) {
                        Item speleothem = speleothems[ctx.nextInt(speleothems.length)];
                        ctx.updateBlock(block.getX(), block.getY(), Layer.FRONT, speleothem, 0);
                    }
                }
            }
            
            CaveType type = cave.getType();
            
            if(type != null) {
                // TODO structure caves should probably be moved to StructureGenerators, or they
                // will take up dungeon space.
                for(CaveDecorator decorator : type.getDecorators()) {
                    if(ctx.nextDouble() <= decorator.getChance()) {
                        decorator.decorate(ctx, cave);
                    }
                }
            }
        }
        
        baseResources.forEach((k, v) -> {
            generateBaseResources(ctx, k, v);
        });
        
        oreDeposits.forEach((k, v) -> {
            generateOreVeins(ctx, k, v);
        });
    }
    
    private void generateBaseResources(GeneratorContext ctx, BaseResourceType type, Deposit deposit) {
        ModTileBlock[] blocks = type.getBlocks();
        
        if(blocks.length > 0) {
            int width = ctx.getWidth();
            int height = ctx.getHeight();
            int numResources = width * height / deposit.getPer();
            
            for(int i = 0; i < numResources; i++) {
                int x = ctx.nextInt(width);
                int surface = ctx.getZone().getSurface()[x];
                int minY = (int)(deposit.getMinDepth() * (height - surface)) + surface;
                int maxY = (int)(deposit.getMaxDepth() * (height - surface)) + surface;
                int y = ctx.nextInt(maxY - minY + 1) + minY;
                placeModTileBlock(ctx, x, y, blocks[ctx.nextInt(blocks.length)]);
            }
        }
    }
    
    private void placeModTileBlock(GeneratorContext ctx, int x, int y, ModTileBlock block) {
        int width = block.getWidth();
        int height = block.getHeight(); 
        
        for(int i = x; i < x + width; i++) {
            for(int j = y; j < y + height; j++) {
                if(!ctx.isEarth(i, j)) {
                    return;
                }
            }
        }
        
        for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++) {
                int mod = j * width + i;
                ctx.updateBlock(x + i, y + j, Layer.FRONT, block.getItem(), mod);
            }
        }
    }
    
    private void generateOreVeins(GeneratorContext ctx, Item item, OreDeposit deposit) {
        int width = ctx.getWidth();
        int height = ctx.getHeight();
        int minY = (int)(deposit.getMinDepth() * height);
        int maxY = (int)(deposit.getMaxDepth() * height);
        int numVeins = width * height / deposit.getPer();
        
        for(int i = 0; i < numVeins; i++) {
            int x = ctx.nextInt(width);
            int y = ctx.nextInt(maxY - minY + 1) + minY;
            BlockPosition current = new BlockPosition(x, y);
            
            if(ctx.isEarth(x, y)) {
                ctx.updateBlock(x, y, Layer.FRONT, item, 0);
            }
            
            int veinSize = ctx.nextInt(deposit.getMaxSize() - deposit.getMinSize() + 1) + deposit.getMinSize();
            
            for(int j = 0; j < veinSize; j++) {
                int nX = current.getX() + ctx.nextInt(3) - 1;
                int nY = current.getY() + ctx.nextInt(3) - 1;
                current = new BlockPosition(nX, nY);
                
                if(ctx.isEarth(nX, nY)) {
                    ctx.updateBlock(nX, nY, Layer.FRONT, item, 1);
                }
            }
        }
    }
}
