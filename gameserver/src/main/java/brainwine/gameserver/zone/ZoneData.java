package brainwine.gameserver.zone;

import java.beans.ConstructorProperties;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ZoneData {
    
    private final int[] surface;
    private final int[] sunlight;
    private final int[] depths;
    private final Collection<Integer> pendingSunlight;
    private final boolean[] chunksExplored;
    
    public ZoneData(Zone zone) {
        this(zone.getSurface(), zone.getSunlight(), zone.getDepths(), zone.getPendingSunlight(), zone.getChunksExplored());
    }
    
    @ConstructorProperties({"surface", "sunlight", "depths", "pending_sunlight", "chunks_explored"})
    public ZoneData(int[] surface, int[] sunlight, int[] depths, Collection<Integer> pendingSunlight, boolean[] chunksExplored) {
        this.surface = surface;
        this.sunlight = sunlight;
        this.depths = depths;
        this.pendingSunlight = pendingSunlight;
        this.chunksExplored = chunksExplored;
    }
    
    public int[] getSurface() {
        return surface;
    }
    
    public int[] getSunlight() {
        return sunlight;
    }
    
    public int[] getDepths() {
        return depths;
    }
    
    public Collection<Integer> getPendingSunlight() {
        return pendingSunlight;
    }
    
    public boolean[] getChunksExplored() {
        return chunksExplored;
    }
}
