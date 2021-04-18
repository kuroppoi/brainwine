package brainwine.gameserver.server.requests;

import brainwine.gameserver.command.CommandManager;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.server.PlayerRequest;

public class ConsoleRequest extends PlayerRequest {
    
    public String commandName;
    public String[] arguments;
    
    @Override
    public void process(Player player) {
        CommandManager.executeCommand(player, commandName, arguments);
    }
}
