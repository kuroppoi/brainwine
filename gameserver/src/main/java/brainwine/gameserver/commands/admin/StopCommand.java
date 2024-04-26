package brainwine.gameserver.commands.admin;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.annotations.CommandInfo;
import brainwine.gameserver.commands.Command;
import brainwine.gameserver.commands.CommandExecutor;

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
