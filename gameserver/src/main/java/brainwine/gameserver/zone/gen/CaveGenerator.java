package brainwine.gameserver.zone.gen;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import brainwine.gameserver.item.Layer;
import brainwine.gameserver.util.MathUtils;
import brainwine.gameserver.util.WeightedList;
import brainwine.gameserver.zone.gen.models.BlockPosition;
import brainwine.gameserver.zone.gen.models.Cave;
import brainwine.gameserver.zone.gen.models.CaveDecorator;
import brainwine.gameserver.zone.gen.models.StoneVariant;

public class CaveGenerator implements GeneratorTask {
    
    private final WeightedList<StoneVariant> stoneVariants = new WeightedList<>();
    private final List<CaveDecorator> decorators;
    
    public CaveGenerator(GeneratorConfig config) {
        config.getStoneVariants().forEach((k, v) -> {
            stoneVariants.addEntry(k, v);
        });
        
        decorators = config.getCaveDecorators();
    }
    
    @Override
    public void generate(GeneratorContext ctx) {
        boolean[][] cells = generateCells(ctx, 0.4625, 9);
        List<Cave> caves = indexCaves(ctx, cells);
        
        for(Cave cave : caves) {
            ctx.addCave(cave);
            StoneVariant variant = cave.getVariant();
            
            for(BlockPosition block : cave.getBlocks()) {
                int x = block.getX();
                int y = block.getY();
                
                if(y - 1 <= ctx.getZone().getSurface()[x]) {
                    ctx.updateBlock(x, y - 1, Layer.FRONT, 0);
                    cave.removeCeilingBlock(block);
                }
                
                ctx.updateBlock(x, y, Layer.FRONT, 0);
                
                if(variant != StoneVariant.DEFAULT) {
                    ctx.updateBlock(x, y, Layer.BASE, variant.getBaseItem());
                    
                    for(int i = x - 3; i <= x + 3; i++) {
                        for(int j = y - 3; j <= y + 3; j++) {
                            if(ctx.isEarth(i, j) && !cells[i][j]) {
                                double maxDistance = MathUtils.clamp(cave.getSize() / 16.0, 1.8, 3) + (ctx.nextDouble() - 0.5);
                                double distance = Math.hypot(i - x, j - y);
                                
                                if(distance <= maxDistance) {
                                    ctx.updateBlock(i, j, Layer.BASE, variant.getBaseItem());
                                    ctx.updateBlock(i, j, Layer.FRONT, variant.getFrontItem());
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private boolean[][] generateCells(GeneratorContext ctx, double cellRate, int smoothCount) {
        int width = ctx.getWidth();
        int height = ctx.getHeight();
        boolean[][] cells = new boolean[width][height];
        
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                if(ctx.isUnderground(x, y) && ctx.nextDouble() <= cellRate) {
                    cells[x][y] = true;
                }
            }
        }
        
        for(int i = 0; i < smoothCount; i++) {
            for(int x = 0; x < width; x++) {
                for(int y = 0; y < height; y++) {
                    int count = getAdjacentCells(ctx, cells, x, y);
                    cells[x][y] = count > 4 ? true : count < 4 ? false : cells[x][y];
                }
            }
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
        Queue<BlockPosition> queue = new ArrayDeque<>();
        List<BlockPosition> blocks = new ArrayList<>();
        BlockPosition first = new BlockPosition(x, y);
        queue.add(first);
        blocks.add(first);
        indexed[x][y] = true;
        
        while(!queue.isEmpty()) {
            BlockPosition caveBlock = queue.poll();
            int bX = caveBlock.getX();
            int bY = caveBlock.getY();
            
            for(int i = bX - 1; i <= bX + 1; i++) {
                for(int j = bY - 1; j <= bY + 1; j++) {
                    if(ctx.inBounds(i, j) && !indexed[i][j] && cells[i][j]) {
                        BlockPosition newBlock = new BlockPosition(i, j);
                        blocks.add(newBlock);
                        queue.add(newBlock);
                        indexed[i][j] = true;
                    }
                }
            }
        }
        
        int size = blocks.size();
        
        if(size >= 20) {
            int surface = ctx.getZone().getSurface()[x];
            double depth = (double)(y - surface) / (ctx.getHeight() - surface);
            Cave cave = new Cave(getRandomEligibleDecorator(ctx, size, depth), stoneVariants.next(ctx.getRandom(), StoneVariant.DEFAULT));
            cave.addBlock(first);
            
            for(BlockPosition block : blocks) {
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
            for(BlockPosition block : blocks) {
                int bX = block.getX();
                int bY = block.getY();
                cells[bX][bY] = false;
            }
        }
        
        return null;
    }
    
    private CaveDecorator getRandomEligibleDecorator(GeneratorContext ctx, int size, double depth) {
        WeightedList<CaveDecorator> list = new WeightedList<>();
        
        for(CaveDecorator decorator : decorators) {
            if(size >= decorator.getMinSize() && size <= decorator.getMaxSize() && depth >= decorator.getMinDepth() && depth <= decorator.getMaxDepth()) {
                list.addEntry(decorator, decorator.getFrequency());
            }
        }
        
        return list.next(ctx.getRandom());
    }
}
