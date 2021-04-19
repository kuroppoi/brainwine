package brainwine.gameserver.command.commands;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;

public class StopCommand extends Command {

    @Override
    public void execute(CommandExecutor executor, String[] args) {
        GameServer.getInstance().shutdown(); // YEET!!
    }
    
    @Override
    public boolean requiresAdmin() {
        return true;
    }
}
