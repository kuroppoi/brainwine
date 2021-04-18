package brainwine.gameserver.server;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.server.pipeline.Connection;

/**
 * Requests that require a player instance.
 */
public abstract class PlayerRequest extends Request {
    
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
