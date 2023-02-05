package brainwine.gameserver.command.commands;

import static brainwine.gameserver.entity.player.NotificationType.SYSTEM;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;

import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.command.CommandManager;

public class HelpCommand extends Command {

    @Override
    public void execute(CommandExecutor executor, String[] args) {
        List<Command> commands = new ArrayList<>(CommandManager.getCommands());
        commands.removeIf(command -> !command.canExecute(executor));
        int pageSize = 8;
        int pageCount = (int)Math.ceil(commands.size() / (double)pageSize);
        int page = 1;
        
        if(args.length > 0) {
            String arg = args[0];
            
            if(NumberUtils.isDigits(arg)) {
                page = Math.max(1, Math.min(pageCount, Integer.parseInt(arg)));
            } else {
                // If command name starts with a prefix, omit it.
                if(arg.startsWith(CommandManager.CUSTOM_COMMAND_PREFIX) || arg.startsWith("/")) {
                    arg = arg.substring(1);
                }
                
                Command command = CommandManager.getCommand(arg);
                
                // If command does not exist (or can not be used by the executor) then notify the executor of this.
                if(command == null || !command.canExecute(executor)) {
                    executor.notify(String.format("Command with name '%s' does not exist.", arg), SYSTEM);
                    return;
                }
                
                executor.notify(String.format("========== Information about '/%s' ==========", command.getName()), SYSTEM);
                executor.notify(String.format("Description: %s", command.getDescription()), SYSTEM);
                executor.notify(String.format("Usage: %s", command.getUsage(executor)), SYSTEM);
                executor.notify(String.format("Aliases: %s", command.getAliases() == null ? "None :("
                        : Arrays.toString(command.getAliases())), SYSTEM);
                return;
            }
        }
        
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(page * pageSize, commands.size());
        List<Command> commandsToDisplay = commands.subList(fromIndex, toIndex);
        executor.notify(String.format("========== Command List (Page %s of %s) ==========", page, pageCount), SYSTEM);
        
        for(Command command : commandsToDisplay) {
            executor.notify(String.format("%s - %s", command.getUsage(executor), command.getDescription()), SYSTEM);
        }
    }

    @Override
    public String getName() {
        return "help";
    }
    
    @Override
    public String getDescription() {
        return "Displays command information.";
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return "/help [page|command]";
    }
}
