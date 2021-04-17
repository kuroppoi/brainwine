package brainwine.gameserver.server.commands;

import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.server.PlayerCommand;
import brainwine.gameserver.server.RegisterCommand;

@RegisterCommand(id = 51)
public class EntitiesRequestCommand extends PlayerCommand {
    
    public int[] entityIds;
    
    public void process(Player player) {
        // TODO
    }
}
