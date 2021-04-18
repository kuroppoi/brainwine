package brainwine.gameserver.command;

import brainwine.gameserver.GameServer;

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
