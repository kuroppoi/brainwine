package brainwine.gameserver.command.commands;

import static brainwine.gameserver.entity.player.NotificationType.ALERT;

import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.entity.EntityConfig;
import brainwine.gameserver.entity.EntityRegistry;
import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.entity.player.NotificationType;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.zone.Zone;

public class EntityCommand extends Command {
    
    @Override
    public void execute(CommandExecutor executor, String[] args) {
        if(args.length == 0) {
            executor.notify(String.format("Usage: %s", getUsage(executor)), ALERT);
            return;
        }
        
        Player player = (Player)executor;
        String name = args[0];
        EntityConfig config = EntityRegistry.getEntityConfig(name);
        
        if(config == null) {
            executor.notify(String.format("Entity with name '%s' does not exist.", name), NotificationType.ALERT);
            return;
        }
        
        Zone zone = player.getZone();
        zone.spawnEntity(new Npc(zone, config), (int)player.getX(), (int)player.getY(), true);
    }

    @Override
    public String getName() {
        return "entity";
    }
    
    @Override
    public String getDescription() {
        return "Spawns an entity at your current location.";
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return "/entity <name>";
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor.isAdmin() && executor instanceof Player;
    }
}
