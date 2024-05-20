package brainwine.gameserver.command.admin;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.command.CommandInfo;

@CommandInfo(name = "stop", description = "Gracefully shuts down the server.", aliases = { "exit", "close", "shutdown" })
public class StopCommand extends Command {

    @Override
    public void execute(CommandExecutor executor, String[] args) {
        GameServer.getInstance().stopGracefully(); // YEET!!
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return "/stop";
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor.isAdmin();
    }
}
