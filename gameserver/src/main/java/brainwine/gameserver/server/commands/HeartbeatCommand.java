package brainwine.gameserver.server.commands;

import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.server.PlayerCommand;
import brainwine.gameserver.server.RegisterCommand;

@RegisterCommand(id = 143)
public class HeartbeatCommand extends PlayerCommand {
    
    public int latency;
    public int commandLatency;
    
    @Override
    public void process(Player player) {
        player.heartbeat();
    }
}
