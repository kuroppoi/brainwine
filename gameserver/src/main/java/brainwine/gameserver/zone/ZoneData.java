package brainwine.gameserver.zone;

import java.beans.ConstructorProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ZoneData {
    
    private int[] surface;
    private int[] sunlight;
    private int[] pendingSunlight;
    private boolean[] chunksExplored;
    
    @ConstructorProperties({"surface", "sunlight", "pending_sunlight", "chunks_explored"})
    public ZoneData(int[] surface, int[] sunlight, int[] pendingSunlight, boolean[] chunksExplored) {
        this.surface = surface;
        this.sunlight = sunlight;
        this.pendingSunlight = pendingSunlight;
        this.chunksExplored = chunksExplored;
    }
    
    public int[] getSurface() {
        return surface;
    }
    
    public int[] getSunlight() {
        return sunlight;
    }
    
    public int[] getPendingSunlight() {
        return pendingSunlight;
    }
    
    public boolean[] getChunksExplored() {
        return chunksExplored;
    }
}
