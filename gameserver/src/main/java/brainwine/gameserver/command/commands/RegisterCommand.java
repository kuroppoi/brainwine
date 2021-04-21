package brainwine.gameserver.command.commands;

import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.dialog.dialogs.RegistrationDialog;
import brainwine.gameserver.entity.player.Player;

public class RegisterCommand extends Command{

    @Override
    public void execute(CommandExecutor executor, String[] args) {
        if(!(executor instanceof Player)) {
            executor.sendMessage("Only players can use this command.");
            return;
        }
        
        Player player = (Player)executor;
        
        if(player.isRegistered()) {
            player.sendMessage("You have already registered your account.");
            return;
        }
        
        player.showDialog(new RegistrationDialog());
    }
}
