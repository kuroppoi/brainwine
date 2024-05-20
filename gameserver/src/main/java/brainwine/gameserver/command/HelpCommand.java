package brainwine.gameserver.command;

import static brainwine.gameserver.player.NotificationType.SYSTEM;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;

@CommandInfo(name = "help", description = "Displays a list of commands.")
public class HelpCommand extends Command {

    @Override
    public void execute(CommandExecutor executor, String[] args) {
        List<Command> commands = new ArrayList<>(CommandManager.getCommands());
        commands.removeIf(command -> !command.canExecute(executor));
        commands.sort((a, b) -> {
            CommandInfo info1 = a.getClass().getAnnotation(CommandInfo.class);
            CommandInfo info2 = b.getClass().getAnnotation(CommandInfo.class);
            return info1.name().compareTo(info2.name());
        });
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
                
                Command command = CommandManager.getCommand(arg, true);
                
                // If command does not exist (or can not be used by the executor) then notify the executor of this.
                if(command == null || !command.canExecute(executor)) {
                    executor.notify(String.format("Command with name '%s' does not exist.", arg), SYSTEM);
                    return;
                }
                
                CommandInfo info = command.getClass().getAnnotation(CommandInfo.class);
                executor.notify(String.format("========== Information about '/%s' ==========", info.name()), SYSTEM);
                executor.notify(String.format("Description: %s", info.description()), SYSTEM);
                executor.notify(String.format("Usage: %s", command.getUsage(executor)), SYSTEM);
                executor.notify(String.format("Aliases: %s", info.aliases() == null ? "None :("
                        : Arrays.toString(info.aliases())), SYSTEM);
                return;
            }
        }
        
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(page * pageSize, commands.size());
        List<Command> commandsToDisplay = commands.subList(fromIndex, toIndex);
        executor.notify(String.format("========== Command List (Page %s of %s) ==========", page, pageCount), SYSTEM);
        
        for(Command command : commandsToDisplay) {
            CommandInfo info = command.getClass().getAnnotation(CommandInfo.class);
            executor.notify(String.format("%s - %s", command.getUsage(executor), info.description()), SYSTEM);
        }
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return "/help [page|command]";
    }
}
