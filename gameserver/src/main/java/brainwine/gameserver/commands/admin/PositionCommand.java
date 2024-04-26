package brainwine.gameserver.commands.admin;

import static brainwine.gameserver.entity.player.NotificationType.SYSTEM;

import brainwine.gameserver.annotations.CommandInfo;
import brainwine.gameserver.commands.Command;
import brainwine.gameserver.commands.CommandExecutor;
import brainwine.gameserver.entity.player.Player;

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
