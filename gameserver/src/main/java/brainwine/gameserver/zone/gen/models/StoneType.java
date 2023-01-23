package brainwine.gameserver.zone.gen.models;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum StoneType {
    
    @JsonEnumDefaultValue
    DEFAULT("base/earth", "ground/earth"),
    SANDSTONE("base/sandstone", "ground/sandstone"),
    LIMESTONE("base/limestone", "ground/limestone");
    
    private final String baseItem;
    private final String frontItem;
    
    private StoneType(String baseItem, String frontItem) {
        this.baseItem = baseItem;
        this.frontItem = frontItem;
    }
    
    public String getBaseItem() {
        return baseItem;
    }
    
    public String getFrontItem() {
        return frontItem;
    }
}
