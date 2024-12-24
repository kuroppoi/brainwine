package brainwine.gameserver.command.world;

import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.command.CommandInfo;
import brainwine.gameserver.dialog.DialogHelper;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.Zone;

@CommandInfo(name = "wprotected", description = "Turn off the protected status of a private world.")
public class WorldProtectedCommand extends WorldCommand {
    
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
        
        if(value == zone.isProtected()) {
            player.notify(String.format("Your world is already %s.", value ? "protected" : "unprotected"));
            return;
        }
        
        if(!value) {
            // Show confirmation dialog
            player.showDialog(DialogHelper.messageDialog("Are you sure?", 
                    "WARNING: You cannot revert back to protected status once your world is unprotected. Are you sure you want to make it unprotected?")
                    .setActions("yesno"), input -> {
                // Check cancellation
                if(input.length == 1 && "cancel".equals(input[0])) {
                    return;
                }
                
                // Disable world protection
                zone.setProtected(false);
            });
        } else {
            // Deny request if player is not in god mode
            if(!player.isGodMode()) {
                player.notify("Sorry, you cannot revert your world back to protected status.");
                return;
            }
            
            // Enable world protection
            zone.setProtected(true);
        }
    }

    @Override
    public String getUsage(CommandExecutor executor) {
        return executor.isAdmin() ? "/wprotected <on|off>" : "/wprotected off";
    }
}
