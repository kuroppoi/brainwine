package brainwine.gameserver.commands.admin;

import static brainwine.gameserver.player.NotificationType.SYSTEM;

import brainwine.gameserver.annotations.CommandInfo;
import brainwine.gameserver.commands.Command;
import brainwine.gameserver.commands.CommandExecutor;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.MetaBlock;

@CommandInfo(name = "find", description = "Displays the location of a random meta block of the specified type.")
public class FindCommand extends Command {
    
    @Override
    public void execute(CommandExecutor executor, String[] args) {
        if(args.length == 0) {
            executor.notify(String.format("Usage: %s", getUsage(executor)), SYSTEM);
            return;
        }
        
        Player player = (Player)executor;
        MetaBlock metaBlock = player.getZone().getMetaBlocks().stream().filter(x -> x.getItem().hasId(args[0])).findAny().orElse(null);
        
        if(metaBlock == null) {
            player.notify(String.format("No meta block of type '%s' exists in this zone.", args[0]), SYSTEM);
            return;
        }
        
        player.notify(String.format("Target found at X: %s, Y: %s", metaBlock.getX(), metaBlock.getY()), SYSTEM);
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return "/find <type>";
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor.isAdmin() && executor instanceof Player;
    }
}
