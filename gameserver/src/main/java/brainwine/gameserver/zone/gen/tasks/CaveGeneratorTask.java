package brainwine.gameserver.zone.gen.tasks;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import brainwine.gameserver.item.Layer;
import brainwine.gameserver.util.MathUtils;
import brainwine.gameserver.util.SimplexNoise;
import brainwine.gameserver.util.Vector2i;
import brainwine.gameserver.util.WeightedMap;
import brainwine.gameserver.zone.gen.GeneratorConfig;
import brainwine.gameserver.zone.gen.GeneratorContext;
import brainwine.gameserver.zone.gen.caves.Cave;
import brainwine.gameserver.zone.gen.caves.CaveType;
import brainwine.gameserver.zone.gen.models.StoneType;
import brainwine.gameserver.zone.gen.models.TerrainType;

public class CaveGeneratorTask implements GeneratorTask {
    
    private final TerrainType terrainType;
    private final WeightedMap<StoneType> stoneVariants;
    private final List<CaveType> caveTypes;
    
    public CaveGeneratorTask(GeneratorConfig config) {
        terrainType = config.getTerrainType();
        stoneVariants = config.getStoneTypes();
        caveTypes = config.getCaveTypes();
    }
    
    @Override
    public void generate(GeneratorContext ctx) {
        boolean asteroids = terrainType == TerrainType.ASTEROIDS;
        int width = ctx.getWidth();
        int height = ctx.getHeight();
        int iterationCount = asteroids ? 6 : 10; // Less asteroid density
        boolean[][] cells = new boolean[width][height];
        List<Cave> caves = new ArrayList<>();
        
        // Multiple layers to increase cave density.
        // I'm not sure if there are better ways to do this, but eh whatever.
        for(int i = 0; i < iterationCount; i++) {
            boolean[][] currentCells = generateCells(ctx, 0.4625, 5);
            List<Cave> currentCaves = indexCaves(ctx, currentCells);
            
            // Remove caves that overlap with existing ones
            for(Cave cave : currentCaves) {
                boolean overlaps = false;
                
                if(!caves.isEmpty()) {
                    for(Vector2i block : cave.getBlocks()) {
                        int x = block.getX();
                        int y = block.getY();
                        
                        for(int j = x - 3; j < x + 3; j++) {
                            for(int k = y - 3; k < y + 3; k++) {
                                if(ctx.inBounds(j, k) && cells[j][k]) {
                                    overlaps = true;
                                }
                            }
                        }
                        
                        if(overlaps) {
                            break;
                        }
                    }
                }
                
                if(!overlaps) {
                    caves.add(cave);
                    
                    for(Vector2i block : cave.getBlocks()) {
                        cells[block.getX()][block.getY()] = true;
                    }
                }
            }
        }
        
        for(Cave cave : caves) {
            ctx.addCave(cave);
            StoneType stoneType = cave.getStoneType();
            int spawnerCount = 0;
            
            for(Vector2i block : cave.getBlocks()) {
                int x = block.getX();
                int y = block.getY();
                
                if(y - 1 <= ctx.getZone().getSurface()[x]) {
                    ctx.updateBlock(x, y - 1, Layer.FRONT, 0);
                    cave.removeCeilingBlock(block);
                }
                
                ctx.updateBlock(x, y, Layer.FRONT, 0);
                
                // Generate a cave wall with a thickness depending on the size of the cave
                if(asteroids || stoneType != StoneType.DEFAULT) {
                    ctx.updateBlock(x, y, Layer.BASE, stoneType.getBaseItem());
                    int checkDistance = asteroids? 5 : 3;
                    
                    for(int i = x - checkDistance; i <= x + checkDistance; i++) {
                        for(int j = y - checkDistance; j <= y + checkDistance; j++) {
                            if(ctx.inBounds(i, j) && !cells[i][j]) {
                                double maxDistance = asteroids ? 4.5 + ctx.nextDouble() - 1 :
                                        MathUtils.clamp(cave.getSize() / 16.0, 1.8, checkDistance) + (ctx.nextDouble() - 0.5);
                                double distance = Math.hypot(i - x, j - y);
                                
                                if(distance <= maxDistance) {
                                    ctx.updateBlock(i, j, Layer.BASE, stoneType.getBaseItem());
                                    
                                    if(stoneType == StoneType.DEFAULT) {
                                        ctx.updateBlock(i, j, Layer.FRONT, ctx.getEarthLayer(j));
                                    } else {
                                        ctx.updateBlock(i, j, Layer.FRONT, stoneType.getFrontItem());
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Hollow out the cave if applicable
                boolean hollow = !asteroids && (terrainType == TerrainType.FILLED || cave.getDepth() >= 0.1) && ctx.nextDouble() <= 0.1;
                
                if(hollow && SimplexNoise.noise2(ctx.getSeed(), x / 20.0, y / 20.0) > 0.4) {
                    ctx.updateBlock(x, y, Layer.BASE, 1);
                }
                
                // Try to generate an entity spawner (maw or pipe)
                double spawnerChance = Math.log(cave.getSize()) / 40 / (spawnerCount + 1);
                boolean spawnerEligible = ctx.nextDouble() <= spawnerChance;
                
                if(spawnerEligible) {
                    for(int i = x - 2; i <= x + 2; i++) {
                        for(int j = y - 2; j <= y + 2; j++) {
                            if(ctx.inBounds(i, j)) {
                                int baseItem = ctx.getZone().getBlock(i, j).getBaseItem().getId();
                                double distance = Math.hypot(i - x, j - y);
                                
                                // Prevent spawners from generating near each other
                                if((!cells[i][j] || baseItem == 1 || baseItem == 5 || baseItem == 6) && distance < 2.5) {
                                    spawnerEligible = false;
                                }
                            }
                        }
                    }
                }
                
                if(spawnerEligible) {
                    int type = ctx.nextDouble() < 0.2 ? 6 : 5;
                    
                    // Only pipes can spawn in sandstone/limestone
                    if(type == 6 || ctx.getZone().getBlock(x, y).getBaseItem().getId() == 2) {
                        ctx.updateBlock(x, y, Layer.BASE, type);
                        spawnerCount++;
                    }
                }
            }
        }
    }
    
    public boolean[][] generateCells(GeneratorContext ctx, double cellRate, int smoothCount) {
        int width = ctx.getWidth();
        int height = ctx.getHeight();
        boolean[][] cells = new boolean[width][height];
        boolean[][] result = new boolean[width][height];
        
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                if((y >= ctx.getZone().getSurface()[x] + ctx.nextInt(3)) && ctx.nextDouble() <= cellRate) {
                    cells[x][y] = true;
                }
            }
        }
        
        for(int i = 0; i < smoothCount; i++) {            
            for(int x = 0; x < width; x++) {
                for(int y = 0; y < height; y++) {
                    int neighbours = getAdjacentCells(ctx, cells, x, y);
                    
                    if(neighbours > 4) {
                        result[x][y] = true;
                    } else if(neighbours < 4) {
                        result[x][y] = false;
                    }
                }
            }
            
            cells = result;
        }
        
        return cells;
    }
    
    private int getAdjacentCells(GeneratorContext ctx, boolean[][] cells, int x, int y) {
        int neighbours = 0;
        
        for(int i = x - 1; i <= x + 1; i++) {
            for(int j = y - 1; j <= y + 1; j++) {
                if(ctx.inBounds(i, j) && !(i == x && j == y) && cells[i][j]) {
                    neighbours++;
                }
            }
        }
        
        return neighbours;
    }
    
    private List<Cave> indexCaves(GeneratorContext ctx, boolean[][] cells) {
        int width = ctx.getWidth();
        int height = ctx.getHeight();
        boolean[][] indexed = new boolean[width][height];
        List<Cave> caves = new ArrayList<>();
        
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                if(cells[x][y] && !indexed[x][y]) {
                    Cave cave = indexCave(ctx, indexed, cells, x, y);
                    
                    if(cave != null) {
                        caves.add(cave);
                    }
                }
            }
        }
        
        return caves;
    }
    
    private Cave indexCave(GeneratorContext ctx, boolean[][] indexed, boolean[][] cells, int x, int y) {
        Queue<Vector2i> queue = new ArrayDeque<>();
        List<Vector2i> blocks = new ArrayList<>();
        Vector2i first = new Vector2i(x, y);
        queue.add(first);
        blocks.add(first);
        indexed[x][y] = true;
        
        while(!queue.isEmpty()) {
            Vector2i caveBlock = queue.poll();
            int bX = caveBlock.getX();
            int bY = caveBlock.getY();
            
            for(int i = bX - 1; i <= bX + 1; i++) {
                if(ctx.inBounds(i, bY) && !indexed[i][bY] && cells[i][bY]) {
                    Vector2i newBlock = new Vector2i(i, bY);
                    blocks.add(newBlock);
                    queue.add(newBlock);
                    indexed[i][bY] = true;
                }
            }
            
            for(int j = bY - 1; j <= bY + 1; j++) {
                if(ctx.inBounds(bX, j) && !indexed[bX][j] && cells[bX][j]) {
                    Vector2i newBlock = new Vector2i(bX, j);
                    blocks.add(newBlock);
                    queue.add(newBlock);
                    indexed[bX][j] = true;
                }
            }
        }
        
        int size = blocks.size();
        
        if(size > 20 && size < 800) {
            int surface = ctx.getZone().getSurface()[x];
            double depth = (double)(y - surface) / (ctx.getHeight() - surface);
            Cave cave = new Cave(getRandomEligibleCaveType(ctx, size, depth), 
                    stoneVariants.next(ctx.getRandom(), StoneType.DEFAULT), depth);
            cave.addBlock(first);
            
            for(Vector2i block : blocks) {
                int bX = block.getX();
                int bY = block.getY();
                
                if(ctx.inBounds(bX, bY - 1) && !cells[bX][bY - 1]) {
                    cave.addCeilingBlock(block);
                } else if(ctx.inBounds(bX, bY + 1) && !cells[bX][bY + 1]) {
                    cave.addFloorBlock(block);
                }
                
                cave.addBlock(block);
            }
            
            return cave;
        } else {
            for(Vector2i block : blocks) {
                int bX = block.getX();
                int bY = block.getY();
                cells[bX][bY] = false;
            }
        }
        
        return null;
    }
    
    private List<CaveType> getEligibleCaveTypes(GeneratorContext ctx, int size, double depth) {
        return caveTypes.stream()
                .filter(type -> size >= type.getMinSize() && size < type.getMaxSize()
                        && depth >= type.getMinDepth() && depth <= type.getMaxDepth())
                .collect(Collectors.toList());
    }
    
    private CaveType getRandomEligibleCaveType(GeneratorContext ctx, int size, double depth) {
        return new WeightedMap<>(getEligibleCaveTypes(ctx, size, depth), CaveType::getFrequency).next(ctx.getRandom());
    }
}
