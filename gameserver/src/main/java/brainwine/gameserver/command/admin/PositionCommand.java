package brainwine.gameserver.command.admin;

import static brainwine.gameserver.player.NotificationType.SYSTEM;

import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.command.CommandInfo;
import brainwine.gameserver.player.Player;

@CommandInfo(name = "position", description = "Displays the coordinates of the block you are standing on.", aliases = { "pos", "feet", "coords", "location" })
public class PositionCommand extends Command {

    @Override
    public void execute(CommandExecutor executor, String[] args) {
        Player player = (Player)executor;   
        player.notify(String.format("X: %s Y: %s", (int)player.getX(), (int)player.getY() + 1), SYSTEM);
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
