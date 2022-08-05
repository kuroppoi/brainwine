package brainwine.gameserver.zone.gen;

import java.util.Collections;
import java.util.List;

import brainwine.gameserver.item.Layer;
import brainwine.gameserver.prefab.Prefab;
import brainwine.gameserver.util.Vector2i;
import brainwine.gameserver.util.WeightedMap;
import brainwine.gameserver.zone.Block;
import brainwine.gameserver.zone.MetaBlock;

public class StructureGenerator implements GeneratorTask {
    
    private final Prefab[] uniqueStructures;
    private final WeightedMap<Prefab> dungeons;
    private final WeightedMap<Prefab> spawnTowers;
    private final Vector2i dungeonRegion;
    private final double dungeonRate;
    private final boolean filled;
    
    public StructureGenerator(GeneratorConfig config) {
        uniqueStructures = config.getUniqueStructures();
        dungeons = config.getDungeons();
        spawnTowers = config.getSpawnTowers();
        dungeonRegion = config.getDungeonRegion();
        dungeonRate = config.getDungeonRate();
        filled = config.getTerrainType() == TerrainType.FILLED;
    }
    
    @Override
    public void generate(GeneratorContext ctx) {
        int width = ctx.getWidth();
        int height = ctx.getHeight();
        
        // Generate spawn buildings
        placeRandomSpawnTower(ctx, (int)(width * 0.2));
        placeRandomSpawnTower(ctx, (int)(width * 0.5));
        placeRandomSpawnTower(ctx, (int)(width * 0.8));
        
        // Generate guaranteed structures (Such as the painting bunker)
        for(Prefab structure : uniqueStructures) {
            int x = ctx.nextInt(width);
            int minY = ctx.getZone().getSurface()[x];
            int y = ctx.nextInt(height - minY) + minY;
            ctx.placePrefab(structure, x, y);
        }
        
        // Generate dungeons
        if(!dungeons.isEmpty()) {
            for(int x = 0; x < width; x += dungeonRegion.getX()) {
                for(int y = 0; y < height; y += dungeonRegion.getY()) {
                    if(ctx.nextDouble() <= dungeonRate) {
                        Prefab dungeon = dungeons.next(ctx.getRandom());
                        int prefabWidth = dungeon.getWidth();
                        int prefabHeight = dungeon.getHeight();
                        
                        if(ctx.isUnderground(x, y) && ctx.isUnderground(x + prefabWidth, y)) {
                            int placeX = x + (prefabWidth >= dungeonRegion.getX() ? x : ctx.nextInt(dungeonRegion.getX() - prefabWidth));
                            int placeY = y + (prefabHeight >= dungeonRegion.getY() ? y : ctx.nextInt(dungeonRegion.getY() - prefabHeight));
                            ctx.placePrefab(dungeon, placeX, placeY);
                        }
                    }
                }
            }
        }
        
        // Populate world with broken teleporters and (soon) world machines & component chests.
        // TODO magic numbers kinda bad
        List<MetaBlock> replaceableContainers = ctx.getZone().getMetaBlocks(metaBlock
                -> metaBlock.getItem().getId() == 824 || (metaBlock.getItem().getId() == 801 && !metaBlock.getMetadata().containsKey("@")));
        Collections.shuffle(replaceableContainers, ctx.getRandom());
        int brokenTeleporterCount = Math.min(replaceableContainers.size(), Math.max(1, width * height / (ctx.nextInt(80000) + 120000)));
        
        for(int i = 0; i < brokenTeleporterCount; i++) {
            MetaBlock container = replaceableContainers.remove(0);
            int x = container.getX();
            int y = container.getY();
            ctx.updateBlock(x, y, Layer.FRONT, 890);
            ctx.getZone().setMetaBlock(x, y, 0);
        }
    }
    
    private void placeRandomSpawnTower(GeneratorContext ctx, int x) {
        int surface = !this.filled ? ctx.getZone().getSurface()[x] 
                : ctx.getHeight() / 8 + ctx.nextInt(Math.max(1, ctx.nextInt(ctx.getHeight() / 8)));
        
        if(!spawnTowers.isEmpty()) {
            Prefab spawnTower = spawnTowers.next(ctx.getRandom());
            int height = spawnTower.getHeight();
            int y = surface - height;
            ctx.placePrefab(spawnTower, x, y);
            generateFoundation(ctx, x, y + height, spawnTower.getWidth());
        } else {
            ctx.updateBlock(x, surface - 1, Layer.FRONT, 891, 0);
        }
    }
    
    private void generateFoundation(GeneratorContext ctx, int x, int y, int width) {
        for(int i = x; i < x + width; i++) {
            int j = y;
            Block block = null;
            
            while((block = ctx.getZone().getBlock(i, j)) != null && block.getBaseItem().isAir() && !block.getFrontItem().isWhole()) {
                ctx.updateBlock(i, j, Layer.BACK, 258);
                j++;
            }
        }
    }
}
