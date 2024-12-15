package brainwine.gameserver.command.world;

import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.command.CommandInfo;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.Zone;

@CommandInfo(name = "wpublic", description = "Toggle world accessibility.")
public class WorldPublicCommand extends WorldCommand {
    
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
        
        if(value == zone.isPublic()) {
            player.notify(String.format("Your world is already %s.", value ? "public" : "private"));
            return;
        }
        
        zone.setPrivate(!value);
        player.notify(String.format("Your world has been made %s.", value ? "public" : "private"));
    }

    @Override
    public String getUsage(CommandExecutor executor) {
        return "/wpublic <on|off>";
    }
}
