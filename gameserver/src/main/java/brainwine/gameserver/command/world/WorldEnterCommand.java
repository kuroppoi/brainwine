package brainwine.gameserver.command.world;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.command.CommandInfo;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.Zone;

@CommandInfo(name = "wenter", description = "Enter a world with a specific entry code.")
public class WorldEnterCommand extends Command {

    @Override
    public void execute(CommandExecutor executor, String[] args) {        
        if(!checkArgumentCount(executor, args, 1)) {
            return;
        }
        
        Player player = (Player)executor;
        String entryCode = args[0];
        Zone zone = GameServer.getInstance().getZoneManager().getZoneByEntryCode(entryCode);
        
        // Check if zone exists
        if(zone == null) {
            player.notify("Can't find a zone for that code.");
            return;
        }
        
        // Check if player is already a member of the target zone
        if(zone.isOwner(player) || zone.isMember(player)) {
            player.notify(String.format("You're already a member of %s.\nFind yourself a teleporter.", zone.getName()));
            return;
        }
        
        // Add player to zone
        if(!zone.isOwned()) {
            zone.setOwner(player);
        } else {
            zone.addMember(player);
        }
        
        // Send player to zone if they're not there already
        if(zone != player.getZone()) {
            player.changeZone(zone);
        }
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return "/wenter <code>";
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor instanceof Player;
    }
}
