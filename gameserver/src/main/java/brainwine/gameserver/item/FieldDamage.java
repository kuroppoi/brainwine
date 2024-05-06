package brainwine.gameserver.item;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
public class FieldDamage {
    
    private DamageType type;
    private float maxDamage;
    private float radius;
    
    public DamageType getType() {
        return type;
    }
    
    public float getMaxDamage() {
        return maxDamage;
    }
    
    public float getRadius() {
        return radius;
    }
}
