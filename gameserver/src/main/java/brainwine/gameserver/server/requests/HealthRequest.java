package brainwine.gameserver.server.requests;

import brainwine.gameserver.annotations.OptionalField;
import brainwine.gameserver.annotations.RequestInfo;
import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.item.DamageType;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.server.PlayerRequest;

@RequestInfo(id = 18)
public class HealthRequest extends PlayerRequest {
    
    public int health;
    public int attackerId;
    
    @OptionalField
    public int damageType;
    
    @Override
    public void process(Player player) {
        float health = this.health / 1000.0F;
        
        // Prevent self-healing unless player has god mode enabled
        if(health >= player.getHealth()) {
            if(player.isGodMode()) {
                player.setHealth(health);
            }
            
            return;
        }
        
        // TODO attacker ID is always zero on v3 and damage type seems to do nothing on both v2 and v3 so we'll just have to do what we can here
        Entity attacker = player.getZone().getEntity(attackerId);
        float damage = player.getHealth() - health;
        player.attack(attacker, Item.AIR, damage, DamageType.ACID, true); // Deal true damage; the client should have already applied any damage modifiers
    }
}
