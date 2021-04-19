package brainwine.gameserver.command.commands;

import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.entity.player.Player;

public class TeleportCommand extends Command {

    @Override
    public void execute(CommandExecutor executor, String[] args) {
        if(!(executor instanceof Player)) {
            executor.alert("Only players can use this command.");
            return;
        }
        
        if(args.length !=  2) {
            executor.alert("Usage: /teleport <x> <y>");
            return;
        }
        
        Player player = (Player)executor;
        int x = 0;
        int y = 0;
        
        try {
            x = Integer.parseInt(args[0]);
            y = Integer.parseInt(args[1]);
        } catch(NumberFormatException e) {
            player.alert("x and y must be numerical.");
            return;
        }
        
        if(!player.getZone().areCoordinatesInBounds(x, y)) {
            player.alert("Cannot teleport out of bounds!");
            return;
        }
        
        player.teleport(x, y);
    }
}
