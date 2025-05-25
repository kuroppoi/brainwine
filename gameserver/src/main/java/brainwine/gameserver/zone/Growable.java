package brainwine.gameserver.zone;

import java.beans.ConstructorProperties;

import brainwine.gameserver.item.Item;

public class Growable {
    
    private final int maxMod;
    private final double chance;
    private final Item replaceSource;
    
    @ConstructorProperties({"max_mod", "chance", "replace_source"})
    public Growable(int maxMod, double chance, Item replaceSource) {
        this.maxMod = maxMod;
        this.chance = chance;
        this.replaceSource = replaceSource;
    }
    
    public int getMaxMod() {
        return maxMod;
    }
    
    public double getChance() {
        return chance;
    }
    
    public Item getReplaceSource() {
        return replaceSource;
    }
}
