package brainwine.gameserver.zone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.Layer;

public class LiquidManager {
    
    public static final int LIQUID_UPDATE_INTERVAL = 250;
    public static final int MAX_TRANSFER_DISTANCE = 20;
    public static final int MAX_SETTLE_UPDATE_COUNT = 10000;
    private final Set<Integer> liquidIndices = new HashSet<>();
    private final Zone zone;
    private long lastLiquidUpdate;
    
    public LiquidManager(Zone zone) {
        this.zone = zone;
    }
    
    public void tick(double deltaTime) {
        long now = System.currentTimeMillis();
        
        // Check if it's time to update liquids yet
        if(now > lastLiquidUpdate + LIQUID_UPDATE_INTERVAL) {
            updateLiquids();
            lastLiquidUpdate = now;
        }
    }
    
    private int updateLiquids() {
        // Sort in reverse order so that lower liquid blocks are updated first
        List<Integer> liquidIndicesToUpdate = new ArrayList<>(liquidIndices);
        Collections.sort(liquidIndicesToUpdate, Collections.reverseOrder());
        liquidIndices.clear();
        int liquidsChanged = 0;
        
        for(int liquidIndex : liquidIndicesToUpdate) {
            int x = liquidIndex % zone.getWidth();
            int y = liquidIndex / zone.getWidth();
            
            // Skip if the chunk at the liquid's location isn't loaded
            if(!zone.isChunkLoaded(x, y)) {
                continue;
            }
            
            Block block = zone.getBlock(x, y);
            Item item = block.getLiquidItem();
            int mod = block.getLiquidMod();
            
            // Skip if for whatever reason it turns out that there is no liquid here
            if(item.isAir() || mod == 0) {
                continue;
            }
            
            // Remove liquid and skip if a whole block is placed over the liquid
            if(block.getFrontItem().isWhole()) {
                zone.updateBlock(x, y, Layer.LIQUID, 0);
                continue;
            }
            
            // Attempt to transfer liquid downwards
            int newMod = transferLiquid(item, mod, x, y, x, y + 1, false);
            
            if(newMod == mod) {
                // If nothing was transferred downwards, try to transfer some to the left
                newMod = transferLiquid(item, newMod, x, y, x - 1, y, true);
                
                if(newMod > 0) {
                    // If there is still some liquid left, try to transfer some to the right
                    newMod = transferLiquid(item, newMod, x, y, x + 1, y, true);
                    
                    if(newMod > 0) {
                        // If there is still some liquid left, look for a place further away where the liquid may settle
                        for(int direction = -1; direction <= 1 && newMod > 0; direction += 2) {
                            for(int distance = 2; distance < MAX_TRANSFER_DISTANCE && newMod > 0; distance++) {
                                int previousX = x + (distance - 1) * direction;
                                int currentX = x + distance * direction;
                                
                                if(zone.isChunkLoaded(currentX, y) && !zone.getBlock(previousX, y).getFrontItem().isWhole() &&
                                        (!zone.getBlock(currentX, y).getLiquidItem().isAir() ||
                                        (!zone.isChunkLoaded(currentX, y + 1) || zone.getBlock(currentX, y + 1).getFrontItem().isWhole()))) {
                                    newMod = transferLiquid(item, newMod, x, y, currentX, y, false);
                                } else {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            
            // Re-index liquid if there is still any left
            if(newMod > 0) {
                indexLiquidBlock(x, y);
            }
            
            // Increase total change count if the liquid mod has changed
            if(newMod != mod) {
                liquidsChanged++;
            }
        }
        
        return liquidsChanged;
    }
    
    private int transferLiquid(Item item, int mod, int sourceX, int sourceY, int destX, int destY, boolean allowDiagonal) {
        // Return input if chunk isn't loaded
        if(!zone.isChunkLoaded(destX, destY)) {
            return mod;
        }
        
        Block destBlock = zone.getBlock(destX, destY);
        int destMod = destBlock.getLiquidMod();
        int transferAmount = 0;
        
        // Return input if the destination is occupied
        if(destBlock.getFrontItem().isWhole() || destMod >= 5) {
            return mod;
        }
        
        if(sourceY != destY) {
            // Transfer as much as we can if we're moving vertically
            transferAmount = Math.min(5 - destMod, mod);
        } else if(mod - destMod >= 2) {
            // Otherwise, transfer 1 unit if the difference in volume between the source and destination
            // is greater than or equal to 2 units
            transferAmount = 1;
        } else if(allowDiagonal && zone.isChunkLoaded(destX, destY + 1)) {
            Block block = zone.getBlock(destX, destY + 1);
            
            // Otherwise, transfer 1 unit if the block below the destination block isn't occupied
            if((block.getLiquidItem().isAir() || block.getLiquidMod() < 5) && !block.getFrontItem().isWhole()) {
                transferAmount = 1;
            }
        }
        
        // Update the source and destination blocks if anything was transferred!
        if(transferAmount > 0) {
            zone.updateBlock(sourceX, sourceY, Layer.LIQUID, mod - transferAmount == 0 ? 0 : item.getCode(), mod - transferAmount);
            zone.updateBlock(destX, destY, Layer.LIQUID, item, destMod + transferAmount);
        }
        
        return mod - transferAmount;
    }
    
    public int settleLiquids() {
        int updateCount = 1;
        
        while(updateLiquids() > 0 && updateCount < MAX_SETTLE_UPDATE_COUNT) {
            updateCount++;
        }
        
        return updateCount;
    }
    
    public void indexLiquidBlock(int x, int y) {
        indexLiquidBlock(zone.getBlockIndex(x, y));
    }
    
    public void indexLiquidBlock(int index) {
        liquidIndices.add(index);
    }
}
