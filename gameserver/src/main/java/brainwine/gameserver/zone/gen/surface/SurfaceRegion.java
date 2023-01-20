package brainwine.gameserver.zone.gen.surface;

public class SurfaceRegion {
    
    private final SurfaceRegionType type;
    private final int start;
    private final int end;
    
    public SurfaceRegion(SurfaceRegionType type, int start, int end) {
        this.type = type;
        this.start = start;
        this.end = end;
    }
    
    public SurfaceRegionType getType() {
        return type;
    }
    
    public int getStart() {
        return start;
    }
    
    public int getEnd() {
        return end;
    }
}
