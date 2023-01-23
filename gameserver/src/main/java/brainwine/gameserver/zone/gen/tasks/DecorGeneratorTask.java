package brainwine.gameserver.zone.gen.tasks;

import java.util.ArrayList;
import java.util.List;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.util.Vector2i;
import brainwine.gameserver.zone.gen.GeneratorConfig;
import brainwine.gameserver.zone.gen.GeneratorContext;
import brainwine.gameserver.zone.gen.caves.Cave;
import brainwine.gameserver.zone.gen.caves.CaveDecorator;
import brainwine.gameserver.zone.gen.caves.CaveType;
import brainwine.gameserver.zone.gen.caves.StructureCaveDecorator;
import brainwine.gameserver.zone.gen.models.BaseResource;
import brainwine.gameserver.zone.gen.models.ModTileBlock;
import brainwine.gameserver.zone.gen.models.OreDeposit;
import brainwine.gameserver.zone.gen.models.TerrainType;
import brainwine.gameserver.zone.gen.surface.StructureSurfaceDecorator;
import brainwine.gameserver.zone.gen.surface.SurfaceDecorator;
import brainwine.gameserver.zone.gen.surface.SurfaceRegion;

public class DecorGeneratorTask implements GeneratorTask {
    
    private static final String[] drawings = {"base/drawing-modern", "base/drawing-historical", "base/drawing-danger"};
    private final TerrainType terrainType;
    private final double backgroundAccentChance;
    private final double backgroundDrawingChance;
    private final BaseResource[] baseResources;
    private final OreDeposit[] oreDeposits;
    private final List<SurfaceDecorator> globalSurfaceDecorators;
    private final List<CaveDecorator> globalCaveDecorators;
    
    public DecorGeneratorTask(GeneratorConfig config) {
        terrainType = config.getTerrainType();
        backgroundAccentChance = config.getBackgroundAccentChance();
        backgroundDrawingChance = config.getBackgroundDrawingChance();
        baseResources = config.getBaseResources();
        oreDeposits = config.getOreDeposits();
        globalSurfaceDecorators = config.getGlobalSurfaceDecorators();
        globalCaveDecorators = config.getGlobalCaveDecorators();
    }
    
    @Override
    public void generate(GeneratorContext ctx) {
        int width = ctx.getWidth();
        int height = ctx.getHeight();
        
        // Generate steam vents
        int steamVentCount = width * height / 8000;
        
        for(int i = 0; i < steamVentCount; i++) {
            int x = ctx.nextInt(ctx.getWidth());
            int surface = ctx.getSurface(x);
            int minDepth = surface + (int)(surface * 0.15);
            int startX  = ctx.nextInt(ctx.getWidth());
            int startY = ctx.nextInt(ctx.getHeight() - minDepth + 1) + minDepth;
            generateSteamVent(ctx, startX, startY);
        }
        
        // Generate background accents and drawings
        for(int x = 0; x < ctx.getWidth(); x++) {
            for(int y = 0; y < ctx.getHeight(); y++) {
                if(ctx.getBlock(x, y).getBaseItem().hasId("base/earth")) {
                    if(ctx.nextDouble() <= backgroundDrawingChance) {
                        ctx.updateBlock(x, y, Layer.BASE, drawings[ctx.nextInt(drawings.length)]);
                    }
                    
                    if(ctx.nextDouble() <= backgroundAccentChance) {
                        ctx.updateBlock(x, y, Layer.BASE, "base/earth-accent");
                    }
                }
            }
        }
        
        // Decorate caves
        for(Cave cave : ctx.getCaves()) {
            // Global decorators
            for(CaveDecorator decorator : globalCaveDecorators) {
                if(!(decorator instanceof StructureCaveDecorator) && ctx.nextDouble() <= decorator.getChance()) {
                    decorator.decorate(ctx, cave);
                }
            }
            
            // Type-exclusive decorators
            CaveType type = cave.getType();
            
            if(type != null) {
                for(CaveDecorator decorator : type.getDecorators()) {
                    if(!(decorator instanceof StructureCaveDecorator) && ctx.nextDouble() <= decorator.getChance()) {
                        decorator.decorate(ctx, cave);
                    }
                }
            }
        }
        
        // Decorate surface regions
        for(SurfaceRegion region : ctx.getSurfaceRegions()) {
            // Global decorators
            for(SurfaceDecorator decorator : globalSurfaceDecorators) {
                if(!(decorator instanceof StructureSurfaceDecorator) && ctx.nextDouble() <= decorator.getChance()) {
                    decorator.decorate(ctx, region);
                }
            }
            
            // Type-exclusive decorators
            for(SurfaceDecorator decorator : region.getType().getDecorators()) {
                if(!(decorator instanceof StructureSurfaceDecorator) && ctx.nextDouble() <= decorator.getChance()) {
                    decorator.decorate(ctx, region);
                }
            }
        }
        
        // Generate base resources (rocks, logs 'n roots)
        for(BaseResource resource : baseResources) {
            generateBaseResources(ctx, resource);
        }
        
        // Generate ore veins
        for(OreDeposit deposit : oreDeposits) {
            generateOreDeposits(ctx, deposit);
        }
    }
    
    private void generateSteamVent(GeneratorContext ctx, int startX, int startY) {
        List<Vector2i> path = randomWalk(ctx, startX, startY, 250);
        double minDepth = terrainType == TerrainType.FILLED ? 0 : 0.15;
        int minPathSize = 125;
        int size;
        
        for(size = 0; size < path.size(); size++) {
            Vector2i position = path.get(size);
            int x = position.getX();
            int y = position.getY();
            
            // Break loop if not in bounds
            if(!ctx.inBounds(x, y)) {
                break;
            }
            
            int surface = ctx.getSurface(x);
            double depth = (double)(y - surface) / (ctx.getHeight() - surface);
            
            // Break loop if not deep enough below surface
            if(depth < minDepth) {
                break;
            }
            
            // Cancel the whole thing if this steam vent path intersects with another
            if(!findAdjacentSteamVents(ctx, x, y).isEmpty()) {
                return;
            }
        }
        
        // Don't place the steam vents if the final path is too small
        if(size < minPathSize) {
            return;
        }
                
        // Place steam vent blocks
        boolean interrupted = false;
        
        for(int i = 0; i < size; i++) {
            Vector2i position = path.get(i);
            int x = position.getX();
            int y = position.getY();
            Item baseItem = ctx.getBlock(x, y).getBaseItem();
            
            // Place vent caps at places where placement is interrupted
            if(baseItem.isAir() || baseItem.hasId("base/empty") || baseItem.hasId("base/sandstone") || baseItem.hasId("base/limestone")) {
                if(!interrupted) {
                    if(i > 0) {
                        Vector2i previousPosition = path.get(i - 1);
                        int previousX = previousPosition.getX();
                        int previousY = previousPosition.getY();
                        ctx.updateBlock(previousX, previousY, Layer.BASE, "base/vent-cap");
                    }
                    
                    interrupted = true;
                }
            } else if(interrupted) {
                ctx.updateBlock(x, y, Layer.BASE, "base/vent-cap");
                interrupted = false;
            } else {
                ctx.updateBlock(x, y, Layer.BASE, i == 0 || i + 1 == size ? "base/vent-cap" : "base/vent");
            }
        }
    }
    
    private List<Vector2i> findAdjacentSteamVents(GeneratorContext ctx, int x, int y) {
        // Epic Java moment
        List<Vector2i> adjacentVents = new ArrayList<>();
        
        // Left
        if(isSteamVent(ctx, x - 1, y)) {
            adjacentVents.add(new Vector2i(x - 1, y));
        }
        
        // Right
        if(isSteamVent(ctx, x + 1, y)) {
            adjacentVents.add(new Vector2i(x + 1, y));
        }
        
        // Up
        if(isSteamVent(ctx, x, y - 1)) {
            adjacentVents.add(new Vector2i(x, y - 1));
        }
        
        // Dowm
        if(isSteamVent(ctx, x, y + 1)) {
            adjacentVents.add(new Vector2i(x, y + 1));
        }
        
        return adjacentVents;
    }
    
    private boolean isSteamVent(GeneratorContext ctx, int x, int y) {
        if(!ctx.inBounds(x, y)) {
            return false;
        }
        
        Item baseItem = ctx.getBlock(x, y).getBaseItem();
        return baseItem.hasId("base/vent") || baseItem.hasId("base/vent-cap");
    }
    
    private List<Vector2i> randomWalk(GeneratorContext ctx, int x, int y, int maxWalks) {
        List<Vector2i> path = new ArrayList<>();
        randomWalk(ctx, x, y, maxWalks, 0, new int[15], path);
        return path;
    }
    
    private void randomWalk(GeneratorContext ctx, int x, int y, int maxWalks, int current, 
            int[] recentDirections, List<Vector2i> path) {
        // Add self to path
        path.add(new Vector2i(x, y));
        
        // Return if we've reached the maximum amount of walks
        if(current >= maxWalks) {
            return;
        }
        
        // We pretend this doesn't exist
        List<Integer> possibleDirections = new ArrayList<>();
        
        for(int i = 1; i <= 4; i++) {
            possibleDirections.add(i);
        }
        
        // Remove directions opposite of recent directions from possible directions
        for(int direction : recentDirections) {
            possibleDirections.remove(
                    direction == 1 ? (Object)3 : direction == 2 ? (Object)4 : direction == 3 ? (Object)1 : (Object)2);
        }
        
        // Walk to the next position; 75% chance of walking in the same direction as last time
        int previousDirection = current == 0 ? 0 : recentDirections[(current - 1) % recentDirections.length];
        int direction = previousDirection > 0 && ctx.nextDouble() <= 0.75 ? previousDirection
                : possibleDirections.get(ctx.nextInt(possibleDirections.size()));
        int nextX = x + (direction == 2 ? 1 : direction == 4 ? -1 : 0);
        int nextY = y + (direction == 1 ? -1 : direction == 3 ? 1 : 0);
        recentDirections[current % recentDirections.length] = direction;
        randomWalk(ctx, nextX, nextY, maxWalks, current + 1, recentDirections, path);
    }
    
    private void generateBaseResources(GeneratorContext ctx, BaseResource resource) {
        ModTileBlock[] blocks = resource.getType().getBlocks();
        
        if(blocks.length == 0) {
            return;
        }
        
        int width = ctx.getWidth();
        int height = ctx.getHeight();
        float amount = width * height / (float)resource.getBlocksPerSpawn();
        int resourceCount = (int)amount;
        
        if(ctx.nextDouble() < (amount - resourceCount)) {
            resourceCount++;
        }
        
        for(int i = 0; i < resourceCount; i++) {
            int x = ctx.nextInt(width);
            int surface = ctx.getSurface(x);
            int minY = (int)(resource.getMinDepth() * (height - surface)) + surface;
            int maxY = (int)(resource.getMaxDepth() * (height - surface)) + surface;
            int y = ctx.nextInt(maxY - minY + 1) + minY;
            placeModTileBlock(ctx, x, y, blocks[ctx.nextInt(blocks.length)]);
        }
    }
    
    private void placeModTileBlock(GeneratorContext ctx, int x, int y, ModTileBlock block) {
        int width = block.getWidth();
        int height = block.getHeight(); 
        
        for(int i = x; i < x + width; i++) {
            for(int j = y; j < y + height; j++) {
                if(!ctx.isEarthy(i, j)) {
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
    
    private void generateOreDeposits(GeneratorContext ctx, OreDeposit deposit) {
        int width = ctx.getWidth();
        int height = ctx.getHeight();
        Item item = deposit.getItem();
        float amount = width * height / (float)deposit.getBlocksPerSpawn();
        int veinCount = (int)amount;
        
        if(ctx.nextDouble() < (amount - veinCount)) {
            veinCount++;
        }
        
        for(int i = 0; i < veinCount; i++) {
            int x = ctx.nextInt(width);
            int surface = ctx.getSurface(x);
            int minY = (int)(deposit.getMinDepth() * (height - surface)) + surface;
            int maxY = (int)(deposit.getMaxDepth() * (height - surface)) + surface;
            int y = ctx.nextInt(maxY - minY + 1) + minY;
            Vector2i current = new Vector2i(x, y);
            
            if(ctx.isEarthy(x, y)) {
                ctx.updateBlock(x, y, Layer.FRONT, item, 0);
            }
            
            int veinSize = ctx.nextInt(deposit.getMaxSize() - deposit.getMinSize() + 1) + deposit.getMinSize();
            
            for(int j = 0; j < veinSize; j++) {
                int nX = current.getX() + ctx.nextInt(3) - 1;
                int nY = current.getY() + ctx.nextInt(3) - 1;
                current = new Vector2i(nX, nY);
                
                if(ctx.isEarthy(nX, nY)) {
                    ctx.updateBlock(nX, nY, Layer.FRONT, item, 1);
                }
            }
        }
    }
}
