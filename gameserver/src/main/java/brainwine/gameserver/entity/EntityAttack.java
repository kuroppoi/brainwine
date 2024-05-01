package brainwine.gameserver.entity;

import brainwine.gameserver.item.DamageType;
import brainwine.gameserver.item.Item;

public class EntityAttack {
    
    private final Entity attacker;
    private final Item weapon;
    private final float baseDamage;
    private final DamageType damageType;
    private final long time;
    
    public EntityAttack(Entity attacker, Item weapon, float baseDamage, DamageType damageType) {
        this.attacker = attacker;
        this.weapon = weapon;
        this.baseDamage = baseDamage;
        this.damageType = damageType;
        this.time = System.currentTimeMillis();
    }
    
    public Entity getAttacker() {
        return attacker;
    }
    
    public Item getWeapon() {
        return weapon;
    }
    
    public float getBaseDamage() {
        return baseDamage;
    }
    
    public DamageType getDamageType() {
        return damageType;
    }
    
    public long getTime() {
        return time;
    }
}
