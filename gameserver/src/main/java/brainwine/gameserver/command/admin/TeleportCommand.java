package brainwine.gameserver.command.admin;

import static brainwine.gameserver.player.NotificationType.SYSTEM;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.command.CommandInfo;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.player.PlayerManager;
import brainwine.gameserver.zone.Zone;

@CommandInfo(name = "teleport", description = "Teleports you or another player to the specified target position or player.", aliases = "tp")
public class TeleportCommand extends Command {

    @Override
    public void execute(CommandExecutor executor, String[] args) {
        if(args.length == 0 || args.length > 3) {
            executor.notify(String.format("Usage: %s", getUsage(executor)), SYSTEM);
            return;
        }
        
        PlayerManager playerManager = GameServer.getInstance().getPlayerManager();
        Player player = (Player)executor;
        Player subject = player; // Player that is being teleported (executor by default)
        Zone targetZone = subject.getZone();
        int x = 0;
        int y = 0;
        
        if(args.length == 1) {
            // Teleport executor to target player
            Player target = playerManager.getPlayer(args[0]);
            
            if(target == null) {
                player.notify(String.format("Player '%s' not found.", args[0]));
                return;
            }
            
            if(!target.isOnline()) {
                player.notify(String.format("Player '%s' is not online.", target.getName()));
                return;
            }
            
            if(subject == target) {
                player.notify("You cannot teleport to yourself.");
                return;
            }
            
            x = target.getBlockX();
            y = target.getBlockY();
            targetZone = target.getZone();
        } else if(args.length == 2) {
            // Teleport executor to target position OR teleport subject to player
            try {
                x = Integer.parseInt(args[0]);
                y = Integer.parseInt(args[1]);
            } catch(NumberFormatException e) {
                // If first 2 params are not numbers then we are probably teleporting a player to another player
                subject = playerManager.getPlayer(args[0]); // Do null check later
                Player target = playerManager.getPlayer(args[1]);
                
                if(target == null) {
                    player.notify(String.format("Player '%s' not found.", args[1]));
                    return;
                }
                
                if(!target.isOnline()) {
                    player.notify(String.format("Player '%s' is not online.", target.getName()));
                    return;
                }
                
                if(subject == target) {
                    player.notify("You cannot teleport a player to themselves.");
                    return;
                }
                
                x = target.getBlockX();
                y = target.getBlockY();
                targetZone = target.getZone();
            }
        } else if(args.length == 3) {
            // Teleport subject to a position
            subject = playerManager.getPlayer(args[0]); // Do null check later
            
            try {
                x = Integer.parseInt(args[1]);
                y = Integer.parseInt(args[2]);
            } catch(NumberFormatException e) {
                player.notify("X and Y must be valid numbers.");
                return;
            }
        }
        
        // Check if subject is present
        if(subject == null) {
            player.notify(String.format("Player '%s' not found.", args[0])); // Subject is always first parameter so this should be fine
            return;
        }
        
        if(!subject.isOnline()) {
            player.notify(String.format("Player '%s' is not online.", subject.getName()));
            return;
        }
        
        // Check if coordinates are in bounds
        if(!player.getZone().areCoordinatesInBounds(x, y)) {
            player.notify("Cannot teleport out of bounds!", SYSTEM);
            return;
        }
        
        if(targetZone == subject.getZone()) {
            subject.teleport(x, y);
        } else {
            subject.changeZone(targetZone, x, y);
        }
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return "/teleport [player] <target>";
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor instanceof Player && executor.isAdmin();
    }
}
