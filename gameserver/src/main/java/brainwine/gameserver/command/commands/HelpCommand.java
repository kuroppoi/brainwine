package brainwine.gameserver.command.commands;

import java.util.Collection;

import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.command.CommandManager;
import brainwine.gameserver.entity.player.Player;

public class HelpCommand extends Command {

    @Override
    public void execute(CommandExecutor executor, String[] args) {
        boolean admin = true;
        
        if(executor instanceof Player) {
            admin = ((Player)executor).isAdmin();
        }
        
        Collection<Command> commands = CommandManager.getCommands();
        executor.sendMessage("Command List");
        
        for(Command command : commands) {
            if(!command.requiresAdmin() || admin) {
                executor.sendMessage(String.format("%s - %s", command.getUsage(), command.getDescription()));
            }
        }
    }

    @Override
    public String getName() {
        return "help";
    }
    
    @Override
    public String[] getAliases() {
        return new String[] { "commands" };
    }
    
    @Override
    public String getDescription() {
        return "Displays a list of commands.";
    }
}
