package brainwine.gameserver.zone.gen.models;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum StoneType {
    
    @JsonEnumDefaultValue
    DEFAULT(2, 512),
    SANDSTONE(3, 510),
    LIMESTONE(4, 511);
    
    private final int baseItem;
    private final int frontItem;
    
    private StoneType(int baseItem, int frontItem) {
        this.baseItem = baseItem;
        this.frontItem = frontItem;
    }
    
    public int getBaseItem() {
        return baseItem;
    }
    
    public int getFrontItem() {
        return frontItem;
    }
}
