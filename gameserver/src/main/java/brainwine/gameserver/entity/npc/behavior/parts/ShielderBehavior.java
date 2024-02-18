package brainwine.gameserver.entity.npc.behavior.parts;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;

import brainwine.gameserver.entity.EntityAttack;
import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.entity.npc.behavior.Behavior;
import brainwine.gameserver.item.DamageType;

public class ShielderBehavior extends Behavior {
    
    private final Set<DamageType> defenses = new HashSet<>();
    private int duration = 5;
    private int recharge = 5;
    private long shieldStart;
    private long lastAttackedAt;
    private DamageType currentShield;
    
    @JsonCreator
    public ShielderBehavior(@JacksonInject Npc entity) {
        super(entity);
    }
    
    @Override
    public boolean behave() {
        long now = System.currentTimeMillis();
        EntityAttack attack = entity.getMostRecentAttack();
        
        if(attack != null) {
            lastAttackedAt = now;
            DamageType type = attack.getDamageType();
            
            if(currentShield == null && now >= shieldStart + (recharge * 1000)) {
                if(defenses.contains(type)) {
                    setShield(type);
                    shieldStart = now;
                }
            } else if(currentShield != null) {
                if(now >= shieldStart + (duration * 1000)) {
                    setShield(null);
                    shieldStart = now;
                } else if(currentShield != type && defenses.contains(type)) {
                    setShield(type);
                }
            }
        } else if(now >= lastAttackedAt + 2000) {
            setShield(null);
        }
        
        return true;
    }
    
    protected void setShield(DamageType type) {
        entity.setProperty("s", type, true);
        entity.setDefense(currentShield, 0);
        currentShield = type;
        
        if(type != null) {
            entity.setDefense(type, 1 - entity.getBaseDefense(type));
        }
    }
    
    public void setDefenses(String... defenses) {
        this.defenses.clear();
        
        for(String defense : defenses) {
            switch(defense) {
                case "all":
                    this.defenses.addAll(Arrays.asList(DamageType.values()));
                    break;
                case "elemental":
                    DamageType[] elementalDamageTypes = DamageType.getElementalDamageTypes();
                    this.defenses.add(elementalDamageTypes[ThreadLocalRandom.current().nextInt(elementalDamageTypes.length)]);
                    break;
                default:
                    DamageType type = DamageType.fromName(defense);
                    
                    if(type != DamageType.NONE) {
                        this.defenses.add(type);
                    }
                    
                    break;
            }
        }
    }
    
    public void setDuration(int duration) {
        this.duration = duration;
    }
    
    public void setRecharge(int recharge) {
        this.recharge = recharge;
    }
}
