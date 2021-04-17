package brainwine.gameserver.server;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.server.pipeline.Connection;

public abstract class PlayerCommand extends Command {
    
    public abstract void process(Player player);
    
    public final void process(Connection connection) {
        Player player = GameServer.getInstance().getPlayerManager().getPlayer(connection);
        
        if(player == null) {
            connection.kick("No player instance found.");
        }
        else {
            process(player);
        }
    }
}
