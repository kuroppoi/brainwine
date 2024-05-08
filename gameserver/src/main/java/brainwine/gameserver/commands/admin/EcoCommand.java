package brainwine.gameserver.commands.admin;

import static brainwine.gameserver.entity.player.NotificationType.SYSTEM;

import java.util.Arrays;
import java.util.Collection;

import brainwine.gameserver.annotations.CommandInfo;
import brainwine.gameserver.commands.Command;
import brainwine.gameserver.commands.CommandExecutor;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemRegistry;
import brainwine.gameserver.zone.EcologicalMachine;
import brainwine.gameserver.zone.Zone;

@CommandInfo(name = "eco", description = "Manage ecological machine parts.")
public class EcoCommand extends Command {
    
    @Override
    public void execute(CommandExecutor executor, String[] args) {
        if(args.length != 1 && args.length != 3) {
            executor.notify(String.format("Usage: %s", getUsage(executor)), SYSTEM);
            return;
        }
        
        Player player = (Player)executor;
        Zone zone = player.getZone();
        EcologicalMachine machine = EcologicalMachine.fromName(args[0]);
        
        if(machine == null) {
            player.notify(String.format("Machine type must be one of %s", Arrays.toString(EcologicalMachine.values()).toLowerCase()), SYSTEM);
            return;
        }
        
        if(args.length == 3) {
            String action = args[1];
            Item part = null;
            
            if(!args[2].equals("all")) {
                part = ItemRegistry.getItem(args[2]);
                
                if(!machine.isMachinePart(part)) {
                    player.notify(String.format("Machine component must be one of %s", machine.getParts()), SYSTEM);
                    return;
                }
            }
            
            if(action.equals("add")) {
                if(part == null) {
                    machine.getParts().forEach(zone::addMachinePart);
                    player.notify(String.format("Added all %s components.", machine.getId()), SYSTEM);
                    return;
                }
                
                if(zone.addMachinePart(part)) {
                    player.notify(String.format("Added %s component '%s'", machine.getId(), part.getId()), SYSTEM);
                    return;
                }
                
                player.notify(String.format("That %s component has already been discovered.", machine.getId()), SYSTEM);
                return;
            }
            
            if(action.equals("remove")) {
                if(part == null) {                    
                    machine.getParts().forEach(zone::removeMachinePart);
                    player.notify(String.format("Removed all %s components.", machine.getId()), SYSTEM);
                    return;
                }
                
                if(zone.removeMachinePart(part)) {
                    player.notify(String.format("Removed %s component '%s'", machine.getId(), part.getId()), SYSTEM);
                    return;
                }
                
                player.notify(String.format("That %s component has not been discovered.", machine.getId()), SYSTEM);
                return;
            }
            
            executor.notify(String.format("Usage: %s", getUsage(executor)), SYSTEM);
            return;
        }
        
        Collection<Item> parts = zone.getDiscoveredParts(machine);
        player.notify(String.format("Discovered %s/%s %s components%s", parts.size(), machine.getPartCount(), machine.getId(), parts.isEmpty() ? "." : ": " + parts), SYSTEM);
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return "/eco <machine> [<add|remove> <component|all>]";
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor.isAdmin() && executor instanceof Player;
    }
}
