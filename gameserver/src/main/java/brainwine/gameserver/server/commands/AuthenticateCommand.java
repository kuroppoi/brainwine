package brainwine.gameserver.server.commands;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.server.Command;
import brainwine.gameserver.server.RegisterCommand;
import brainwine.gameserver.server.pipeline.Connection;

@RegisterCommand(id = 1)
public class AuthenticateCommand extends Command {
    
    public String version;
    public String name;
    public String authToken;
    
    @Override
    public void process(Connection connection) {
        GameServer.getInstance().getPlayerManager().onPlayerAuthenticate(connection, version, name, authToken);
    }
}
