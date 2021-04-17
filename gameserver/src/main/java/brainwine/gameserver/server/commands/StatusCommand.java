package brainwine.gameserver.server.commands;

import org.msgpack.type.Value;

import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.server.PlayerCommand;
import brainwine.gameserver.server.RegisterCommand;

@RegisterCommand(id = 54)
public class StatusCommand extends PlayerCommand {
    
    public Value status;
    
    @Override
    public void process(Player player) {
        
    }
}
