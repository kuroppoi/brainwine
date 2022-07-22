package brainwine.gameserver.item;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DamageType {
    
    ACID,
    BLUDGEONING,
    COLD,
    CRUSHING,
    ENERGY,
    FIRE,
    PIERCING,
    SLASHING,
    
    @JsonEnumDefaultValue
    NONE;
    
    public static DamageType[] getPhysicalDamageTypes() {
        return new DamageType[] { BLUDGEONING, CRUSHING, PIERCING, SLASHING };
    }
    
    public static DamageType[] getElementalDamageTypes() {
        return new DamageType[] { ACID, COLD, ENERGY, FIRE };
    }
    
    public static DamageType fromName(String id) {
        for(DamageType value : values()) {
            if(value.toString().equalsIgnoreCase(id)) {
                return value;
            }
        }
        
        return NONE;
    }
    
    @JsonValue
    public String getId() {
        return toString().toLowerCase();
    }
}
