package brainwine.gameserver.command.world;

import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.command.CommandInfo;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.Zone;

@CommandInfo(name = "wpvp", description = "Turn PvP on or off in a private world.")
public class WorldPvpCommand extends WorldCommand {
    
    @Override
    public void execute(Zone zone, Player player, String[] args) {
        if(!checkArgumentCount(player, args, 1)) {
            return;
        }
        
        if(!args[0].equalsIgnoreCase("on") && !args[0].equalsIgnoreCase("off")) {
            sendUsageMessage(player);
            return;
        }
        
        boolean value = args[0].equalsIgnoreCase("on");
        
        if(value == zone.isPvp()) {
            player.notify(String.format("PvP is already %s.", value ? "enabled" : "disabled"));
            return;
        }
        
        zone.setPvp(value);
    }

    @Override
    public String getUsage(CommandExecutor executor) {
        return "/wpvp <on|off>";
    }
}
