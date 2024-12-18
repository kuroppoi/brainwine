package brainwine.gameserver.command.world;

import java.time.temporal.ChronoUnit;

import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.command.CommandInfo;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.Zone;

@CommandInfo(name = "wpublic", description = "Toggle world accessibility.")
public class WorldPublicCommand extends WorldCommand {
    
    public static final String ACTION_ID = "wpublic";
    
    @Override
    public void execute(Zone zone, Player player, String[] args) {
        if(!checkArgumentCount(player, args, 1)) {
            return;
        }
        
        // Check if command is on cooldown
        if(!player.isGodMode() && zone.isActionOnCooldown(ACTION_ID, 1, ChronoUnit.HOURS)) {
            player.notify("Sorry, you can toggle accessibility only once an hour.");
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
        zone.recordActionTime(ACTION_ID);
    }

    @Override
    public String getUsage(CommandExecutor executor) {
        return "/wpublic <on|off>";
    }
}
