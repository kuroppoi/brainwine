package brainwine.gameserver.command.commands;

import static brainwine.gameserver.entity.player.NotificationType.ALERT;

import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.entity.player.Player;

public class TeleportCommand extends Command {

    @Override
    public void execute(CommandExecutor executor, String[] args) {
        if(args.length !=  2) {
            executor.notify(String.format("Usage: %s", getUsage(executor)), ALERT);
            return;
        }
        
        Player player = (Player)executor;
        int x = 0;
        int y = 0;
        
        try {
            x = Integer.parseInt(args[0]);
            y = Integer.parseInt(args[1]);
        } catch(NumberFormatException e) {
            player.notify("x and y must be numerical.", ALERT);
            return;
        }
        
        if(!player.getZone().areCoordinatesInBounds(x, y)) {
            player.notify("Cannot teleport out of bounds!", ALERT);
            return;
        }
        
        player.teleport(x, y);
    }
    
    @Override
    public String getName() {
        return "teleport";
    }
    
    @Override
    public String[] getAliases() {
        return new String[] { "tp" };
    }
    
    @Override
    public String getDescription() {
        return "Teleports you to the specified position.";
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
