package brainwine.gameserver.server.requests;

import brainwine.gameserver.annotations.OptionalField;
import brainwine.gameserver.annotations.RequestInfo;
import brainwine.gameserver.entity.player.Player;
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
        
        if(health >= player.getHealth()) {
            return;
        }
        
        player.damage(player.getHealth() - health, null);
    }
}
