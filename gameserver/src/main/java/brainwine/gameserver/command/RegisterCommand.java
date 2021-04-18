package brainwine.gameserver.command;

import brainwine.gameserver.entity.player.Player;

public class RegisterCommand extends Command{

    @Override
    public void execute(CommandExecutor executor, String[] args) {
        if(!(executor instanceof Player)) {
            executor.alert("Only players can use this command.");
            return;
        }
        
        Player player = (Player)executor;
        player.alert("Sorry, this feature has not been implemented yet.");
    }
}
