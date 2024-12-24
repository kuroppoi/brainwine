package brainwine.gameserver.zone.gen.models;

import brainwine.gameserver.prefab.Prefab;

/**
 * Instance of a placed structure in a zone for use in generator contexts.
 * 
 * TODO we could store more information here in the future to allow for more specific structure edits after the fact.
 */
public class Structure {
    
    private final Prefab prefab;
    private final int x;
    private final int y;
    private final boolean mirrored;
    
    public Structure(Prefab prefab, int x, int y, boolean mirrored) {
        this.prefab = prefab;
        this.x = x;
        this.y = y;
        this.mirrored = mirrored;
    }
    
    public Prefab getPrefab() {
        return prefab;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public int getWidth() {
        return prefab.getWidth();
    }
    
    public int getHeight() {
        return prefab.getHeight();
    }
    
    public boolean isMirrored() {
        return mirrored;
    }
}
