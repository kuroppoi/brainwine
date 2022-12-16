package brainwine.gameserver.command.commands;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;

public class StopCommand extends Command {

    @Override
    public void execute(CommandExecutor executor, String[] args) {
        GameServer.getInstance().stopGracefully(); // YEET!!
    }
    
    @Override
    public String getName() {
        return "stop";
    }
    
    @Override
    public String[] getAliases() {
        return new String[] { "exit", "close", "shutdown" };
    }
    
    @Override
    public String getDescription() {
        return "Gracefully shuts down the server after the current tick.";
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor.isAdmin();
    }
}
