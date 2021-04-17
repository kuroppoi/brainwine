package brainwine.gameserver.zone.gen;

import java.util.ArrayDeque;
import java.util.Queue;

import brainwine.gameserver.item.Layer;
import brainwine.gameserver.util.MathUtils;

/**
 * This can probably be much more efficient, but I suck at this lmao
 */
public class CaveGeneratorTask implements GeneratorTask {

    public static final int MIN_CAVE_SIZE = 20;
    public static final int MAX_CAVE_SIZE = 400;
    
    @Override
    public void generate(GeneratorContext ctx) {
        int[][] cells = new int[ctx.getWidth()][ctx.getHeight()];
        Cave[][] caves = new Cave[ctx.getWidth()][ctx.getHeight()];
        
        // Generate random cell noise
        for(int x = 0; x < ctx.getWidth(); x++) {
            for(int y = 0; y < ctx.getHeight(); y++) {
                if(ctx.isUnderground(x, y) && ctx.nextDouble() < 0.4625) { // 0.46
                    cells[x][y] = 1;
                }
            }
        }
        
        // Utilize cellular automata to create smooth caves
        for(int i = 0; i < 9; i++) {
            for(int x = 0; x < ctx.getWidth(); x++) {
                for(int y = 0; y < ctx.getHeight(); y++) {
                    int neighbours = getNeighbouringCellCount(ctx, x, y, cells);
                    
                    if(neighbours > 4) {
                        cells[x][y] = 1;
                    } else if(neighbours < 4) {
                        cells[x][y] = 0;
                    }
                }
            }
        }
        
        // Index caves
        for(int x = 0; x < ctx.getWidth(); x++) {
            for(int y = 0; y < ctx.getHeight(); y++) {
                if(cells[x][y] == 1) {  
                    if(caves[x][y] == null) {
                        scanCave(ctx, x, y, cells, caves);
                    }
                }
            }
        }
        
        // Set cave blocks
        for(int x = 0; x < ctx.getWidth(); x++) {
            for(int y = 0; y < ctx.getHeight(); y++) {
                if(cells[x][y] == 0) {  
                    CaveType type = getCaveWallType(ctx, x, y, caves);
                    
                    if(type == CaveType.SANDSTONE) {
                        ctx.updateBlock(x, y, Layer.BASE, 3);
                        ctx.updateBlock(x, y, Layer.FRONT, 510);
                    } else if(type == CaveType.LIMESTONE) {
                        ctx.updateBlock(x, y, Layer.BASE, 4);
                        ctx.updateBlock(x, y, Layer.FRONT, 511);
                    }
                } else {
                    CaveType type = caves[x][y].getType();
                    
                    if(type == CaveType.SANDSTONE) {
                        ctx.updateBlock(x, y, Layer.BASE, 3);
                    } else if(type == CaveType.LIMESTONE) {
                        ctx.updateBlock(x, y, Layer.BASE, 4);
                    }
                    
                    ctx.updateBlock(x, y, Layer.FRONT, 0);
                    
                    if(y - 1 <= ctx.getSurface()[x]) {
                        ctx.updateBlock(x, y - 1, Layer.FRONT, 0);
                    }
                }
            }
        }
    }
    
    private int getNeighbouringCellCount(GeneratorContext ctx, int x, int y, int[][] cells) {
        int neighbours = 0;
        
        for(int i = x - 1; i <= x + 1; i++) {
            for(int j = y - 1; j <= y + 1; j++) {
                if(!ctx.areCoordinatesInBounds(i, j) || (i == x && j == y)) {
                    continue;
                }
                
                if(cells[i][j] == 1) {
                    neighbours++;
                }
            }
        }
        
        return neighbours;
    }
    
    private void scanCave(GeneratorContext ctx, int x, int y, int[][] cells, Cave[][] caves) {
        double random = ctx.nextDouble();
        CaveType type = random <= 0.095 ? CaveType.LIMESTONE : random <= 0.19 ? CaveType.SANDSTONE : CaveType.NORMAL;
        Cave cave = new Cave(type);
        cave.addBlock(x, y);
        caves[x][y] = cave;
        Queue<BlockPosition> blocks = new ArrayDeque<>();
        blocks.add(new BlockPosition(x, y));
        
        while(!blocks.isEmpty()) {
            int bX = blocks.peek().getX();
            int bY = blocks.peek().getY();
            blocks.poll();
            
            for(int i = bX - 1; i <= bX + 1; i++) {
                for(int j = bY - 1; j <= bY + 1; j++) {
                    if(!ctx.areCoordinatesInBounds(i, j) || cells[i][j] == 0 || caves[i][j] != null) {
                        continue;
                    }
                    
                    cave.addBlock(i, j);
                    caves[i][j] = cave;
                    blocks.add(new BlockPosition(i, j));
                }
            }
        }
        
        if(cave.getSize() < MIN_CAVE_SIZE) {
            for(BlockPosition block : cave.getBlocks()) {
                int bX = block.getX();
                int bY = block.getY();
                cells[bX][bY] = 0;
                caves[bX][bY] = null;
            }
        }
    }
    
    private CaveType getCaveWallType(GeneratorContext ctx, int x, int y, Cave[][] caves) {
        for(int i = x - 3; i <= x + 3; i++) {
            for(int j = y - 3; j <= y + 3; j++) {
                if(!ctx.areCoordinatesInBounds(i, j) || caves[i][j] == null) {
                    continue;
                }
                
                Cave cave = caves[i][j];
                
                if(cave.getType() != CaveType.NORMAL) {
                    double maxWallDistance = MathUtils.clamp(cave.getSize() / 16.0, 1.8, 3) + (ctx.nextDouble() - 0.5);
                    double distance = Math.hypot(i - x, j - y);
                    
                    if(distance <= maxWallDistance) {
                        return cave.getType();
                    }
                }
            }
        }
        
        return CaveType.NORMAL;
    }
}
