package brainwine.gameserver.zone;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import brainwine.gameserver.item.Item;

/**
 * Distributes steam through collectors to nearby machines via pipes.
 */
public class SteamManager {
    
    public static final int STEAM_UPDATE_INTERVAL = 3000; // Update interval in milliseconds
    public static final int MAX_ITERATIONS = 300; // Maximum number of iterations before giving up
    public static final int MAX_COLLECTOR_DISTANCE = 350; // Collectors that are not within this distance of any players in the zone will be skipped
    public static final byte STATE_EMPTY = 0x0; // Nothing or unrelated
    public static final byte STATE_PIPE = 0x1; // Pipe
    public static final byte STATE_COLLECTOR = 0x2; // Active collector
    private final Set<Integer> collectorIndices = new HashSet<>();
    private final Set<Integer> steamableIndices = new HashSet<>();
    private final Set<Integer> processedIndices = new HashSet<>();
    private final List<Integer> expiredSteamableIndices = new ArrayList<>();
    private final Queue<SteamIteration> processQueue = new ArrayDeque<>();
    private final Zone zone;
    private byte[] data;
    private long lastUpdateAt;
    
    public SteamManager(Zone zone) {
        this.zone = zone;
        this.data = new byte[(zone.getWidth() * zone.getHeight()) >> 2];
    }
    
    public void tick(double deltaTime) {
        long now = System.currentTimeMillis();
        
        // Check if it's time to update steam yet
        if(now > lastUpdateAt + STEAM_UPDATE_INTERVAL) {
            updateSteam();
            lastUpdateAt = now;
        }
    }
    
    private void updateSteam() {
        // Do nothing if there are no players in this zone
        if(zone.getPlayerCount() == 0) {
            return;
        }
        
        // Clear data from previous run
        processedIndices.clear();
        expiredSteamableIndices.clear();
        
        // Turn off all steam-powered objects
        for(int index : steamableIndices) {
            int x = index % zone.getWidth();
            int y = index / zone.getWidth();
            
            // Skip if chunk isn't loaded
            if(!zone.isChunkLoaded(x, y)) {
                expiredSteamableIndices.add(index);
                continue;
            }
            
            Item item = zone.getBlock(x, y).getFrontItem();
            
            // Skip if front item doesn't use steam
            if(!item.usesSteam()) {
                expiredSteamableIndices.add(index);
                continue;
            }
            
            // Update block
            zone.updateBlock(x, y, item.getLayer(), item, 0);
        }
        
        // Unindex expired steamables
        for(int index : expiredSteamableIndices) {
            steamableIndices.remove(index);
        }
        
        // Enqueue blocks at the spouts of all collectors
        for(int index : collectorIndices) {
            int x = index % zone.getWidth();
            int y = index / zone.getWidth();
            
            // Skip if no player is close to this collector
            if(zone.getPlayersInRange(x, y, MAX_COLLECTOR_DISTANCE).isEmpty()) {
                continue;
            }
            
            // Queue spouts
            processQueue.add(new SteamIteration(x + 1, y - 3, 0, 0)); // Top
            processQueue.add(new SteamIteration(x + 3, y - 1, 1, 0)); // Right
            processQueue.add(new SteamIteration(x + 1, y + 1, 2, 0)); // Bottom
            processQueue.add(new SteamIteration(x - 1, y - 1, 3, 0)); // Left
        }
        
        // Travel down the pipeline and power on any machines that are reached by it
        while(!processQueue.isEmpty()) {
            SteamIteration iteration = processQueue.poll();
            int depth = iteration.getDepth();
            
            // Skip if depth limit has been reached
            if(depth >= MAX_ITERATIONS) {
                continue;
            }
            
            int x = iteration.getX();
            int y = iteration.getY();
            
            // Skip if coordinates are out of bounds
            if(!zone.areCoordinatesInBounds(x, y)) {
                continue;
            }
            
            int index = zone.getBlockIndex(x, y);
            
            // Skip if block has already been processed
            if(processedIndices.contains(index)) {
                continue;
            }
            
            processedIndices.add(index);
            
            // Skip if block is not a pipe
            if(getState(x, y) != STATE_PIPE) {
                
                // ...but activate it first if it uses steam!
                if(steamableIndices.contains(index)) {
                    Item item = zone.getBlock(x, y).getFrontItem();
                    zone.updateBlock(x, y, item.getLayer(), item, 1);
                }
                
                continue;
            }
            
            byte direction = iteration.getDirection();
            int nextDepth = depth + 1;
            
            // Enqueue adjacent blocks for processing
            if(direction != 2) processQueue.add(new SteamIteration(x, y - 1, 0, nextDepth)); // Top
            if(direction != 3) processQueue.add(new SteamIteration(x + 1, y, 1, nextDepth)); // Right
            if(direction != 0) processQueue.add(new SteamIteration(x, y + 1, 2, nextDepth)); // Bottom
            if(direction != 1) processQueue.add(new SteamIteration(x - 1, y, 3, nextDepth)); // Left
        }
    }
    
    public void indexBlock(int x, int y, Item item) {
        int index = zone.getBlockIndex(x, y);
        
        // Does it use steam?
        if(!item.usesSteam()) {
            steamableIndices.remove(index);
            
            // Is it a pipe?
            if(!item.hasId("mechanical/pipe")) {

                // Is it a collector and is it on top of a steam vent?
                if(!item.hasId("mechanical/collector") || !isCollectorActive(x, y)) {
                    collectorIndices.remove(index);
                    setState(index, STATE_EMPTY);
                    return;
                }
                
                collectorIndices.add(index);
                setState(index, STATE_COLLECTOR);
                return;
            }
            
            setState(index, STATE_PIPE);
            return;
        }
        
        steamableIndices.add(index);
        setState(index, STATE_EMPTY);
    }
    
    private boolean isCollectorActive(int x, int y) {
        return zone.isChunkLoaded(x + 1, y - 1) && zone.getBlock(x + 1, y - 1).getBaseItem().hasId("base/vent");
    }
    
    protected void setData(byte[] data) {
        // Do nothing if data is null
        if(data == null) {
            return;
        }
        
        int size = zone.getWidth() * zone.getHeight();
        
        // Do nothing if data size is incorrect
        if(data.length << 2 != size) {
            return;
        }
        
        this.data = data;
        
        // Index active collectors
        for(int i = 0; i < size; i++) {
            if(getState(i) == STATE_COLLECTOR) {
                collectorIndices.add(i);
            }
        }
    }
    
    private void setState(int index, byte state) {
        int byteOffset = index >> 2;
        int bitOffset = (index % 4) << 1;
        data[byteOffset] &= ~(0x3 << bitOffset); // Clear bits
        data[byteOffset] |= (state & 0x3) << bitOffset; // Set bits
    }
    
    private int getState(int x, int y) {
        return getState(zone.getBlockIndex(x, y));
    }
    
    private int getState(int index) {
        return (data[index >> 2] >> ((index % 4) << 1)) & 0x3;
    }
    
    protected byte[] getData() {
        return data;
    }
}
