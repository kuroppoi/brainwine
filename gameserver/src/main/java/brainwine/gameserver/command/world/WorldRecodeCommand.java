package brainwine.gameserver.command.world;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.command.CommandInfo;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.Zone;

@CommandInfo(name = "wrecode", description = "Issues a new entry code for your private world.")
public class WorldRecodeCommand extends WorldCommand {
    
    @Override
    public void execute(Zone zone, Player player, String[] args) {
        // Try to generate a new entry code
        if(!GameServer.getInstance().getZoneManager().issueEntryCode(zone)) {
            player.notify("Unable to change the entry code, please try again.");
            return;
        }
        
        player.notify(String.format("Your world entry code has been changed to %s.", zone.getEntryCode()));
    }

    @Override
    public String getUsage(CommandExecutor executor) {
        return "/wrecode";
    }
}
