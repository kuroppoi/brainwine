package brainwine.gameserver.commands.admin;

import static brainwine.gameserver.entity.player.NotificationType.SYSTEM;

import brainwine.gameserver.annotations.CommandInfo;
import brainwine.gameserver.commands.Command;
import brainwine.gameserver.commands.CommandExecutor;
import brainwine.gameserver.entity.player.NotificationType;
import brainwine.gameserver.entity.player.Player;

@CommandInfo(name = "entity", description = "Spawns an entity at your current location.")
public class EntityCommand extends Command {
    
    @Override
    public void execute(CommandExecutor executor, String[] args) {
        if(args.length == 0) {
            executor.notify(String.format("Usage: %s", getUsage(executor)), SYSTEM);
            return;
        }
        
        Player player = (Player)executor;
        String type = args[0];
        
        if(player.getZone().spawnEntity(type, player.getBlockX(), player.getBlockY(), true) == null) {
            executor.notify(String.format("Entity type '%s' does not exist.", type), NotificationType.SYSTEM);
            return;
        }
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return "/entity <type>";
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor.isAdmin() && executor instanceof Player;
    }
}
