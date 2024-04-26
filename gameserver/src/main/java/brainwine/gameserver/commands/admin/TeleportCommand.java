package brainwine.gameserver.commands.admin;

import static brainwine.gameserver.entity.player.NotificationType.SYSTEM;

import brainwine.gameserver.annotations.CommandInfo;
import brainwine.gameserver.commands.Command;
import brainwine.gameserver.commands.CommandExecutor;
import brainwine.gameserver.entity.player.Player;

@CommandInfo(name = "teleport", description = "Teleports you to the specified position.", aliases = "tp")
public class TeleportCommand extends Command {

    @Override
    public void execute(CommandExecutor executor, String[] args) {
        if(args.length !=  2) {
            executor.notify(String.format("Usage: %s", getUsage(executor)), SYSTEM);
            return;
        }
        
        Player player = (Player)executor;
        int x = 0;
        int y = 0;
        
        try {
            x = Integer.parseInt(args[0]);
            y = Integer.parseInt(args[1]);
        } catch(NumberFormatException e) {
            player.notify("x and y must be numerical.", SYSTEM);
            return;
        }
        
        if(!player.getZone().areCoordinatesInBounds(x, y)) {
            player.notify("Cannot teleport out of bounds!", SYSTEM);
            return;
        }
        
        player.teleport(x, y);
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return "/teleport <x> <y>";
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor instanceof Player && executor.isAdmin();
    }
}
