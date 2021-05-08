package brainwine.gameserver.server.requests;

import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.server.OptionalField;
import brainwine.gameserver.server.PlayerRequest;

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
        
        // TODO
        player.setHealth(10);
    }
}
