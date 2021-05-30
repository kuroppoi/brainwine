package brainwine.gameserver.command.commands;

import static brainwine.gameserver.entity.player.NotificationType.SYSTEM;

import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.entity.player.Player;

public class PositionCommand extends Command {

    @Override
    public void execute(CommandExecutor executor, String[] args) {
        Player player = (Player)executor;   
        player.notify(String.format("X: %s Y: %s", (int)player.getX(), (int)player.getY() + 1), SYSTEM);
    }

    @Override
    public String getName() {
        return "pos";
    }
    
    @Override
    public String[] getAliases() {
        return new String[] { "position", "feet", "coords", "location" };
    }
    
    @Override
    public String getDescription() {
        return "Displays the coordinates of the block you are standing on.";
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return "/pos";
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor instanceof Player && executor.isAdmin();
    }
}
