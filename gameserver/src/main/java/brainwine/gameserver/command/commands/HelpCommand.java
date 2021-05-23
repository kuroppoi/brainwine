package brainwine.gameserver.command.commands;

import static brainwine.gameserver.entity.player.NotificationType.CHAT;

import java.util.Collection;

import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.command.CommandManager;

public class HelpCommand extends Command {

    @Override
    public void execute(CommandExecutor executor, String[] args) {
        Collection<Command> commands = CommandManager.getCommands();
        executor.notify("Command List", CHAT);
        
        for(Command command : commands) {
            if(command.canExecute(executor)) {
                executor.notify(String.format("%s - %s", command.getUsage(executor), command.getDescription()), CHAT);
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
