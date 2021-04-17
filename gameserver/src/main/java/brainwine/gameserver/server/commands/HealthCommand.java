package brainwine.gameserver.server.commands;

import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.server.PlayerCommand;
import brainwine.gameserver.server.RegisterCommand;

@RegisterCommand(id = 18)
public class HealthCommand extends PlayerCommand {
    
    public int health;
    public int attackerId;
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
