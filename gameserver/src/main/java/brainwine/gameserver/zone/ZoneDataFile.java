package brainwine.gameserver.zone;

import java.util.ArrayList;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ZoneDataFile {
    
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private int[] surface = {};
    
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private int[] sunlight = {};
    
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private int[] depths = {};
    
    @JsonSetter(nulls = Nulls.AS_EMPTY, contentNulls = Nulls.SKIP)
    private Collection<Integer> pendingSunlight = new ArrayList<>();
    
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private boolean[] chunksExplored = {};
    
    public ZoneDataFile(Zone zone) {
        this(zone.getSurface(), zone.getSunlight(), zone.getDepths(), zone.getPendingSunlight(), zone.getChunksExplored());
    }
    
    public ZoneDataFile(int[] surface, int[] sunlight, int[] depths, Collection<Integer> pendingSunlight, boolean[] chunksExplored) {
        this.surface = surface;
        this.sunlight = sunlight;
        this.depths = depths;
        this.pendingSunlight = pendingSunlight;
        this.chunksExplored = chunksExplored;
    }
    
    @JsonCreator
    private ZoneDataFile() {}
    
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
