package brainwine.gameserver.server.requests;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.server.Request;
import brainwine.gameserver.server.pipeline.Connection;

public class AuthenticateRequest extends Request {
    
    public String version;
    public String name;
    public String authToken;
    
    @Override
    public void process(Connection connection) {
        GameServer.getInstance().getPlayerManager().onPlayerAuthenticate(connection, version, name, authToken);
    }
}
