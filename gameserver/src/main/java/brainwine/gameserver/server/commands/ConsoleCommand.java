package brainwine.gameserver.server.commands;

import brainwine.gameserver.command.CommandManager;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.server.PlayerCommand;
import brainwine.gameserver.server.RegisterCommand;

@RegisterCommand(id = 47)
public class ConsoleCommand extends PlayerCommand {
    
    public String commandName;
    public String[] arguments;
    
    @Override
    public void process(Player player) {
        CommandManager.executeCommand(player, commandName, arguments);
    }
}
