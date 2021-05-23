package brainwine.gameserver.command.commands;

import static brainwine.gameserver.entity.player.NotificationType.ALERT;

import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.dialog.dialogs.RegistrationDialog;
import brainwine.gameserver.entity.player.Player;

public class RegisterCommand extends Command {

    @Override
    public void execute(CommandExecutor executor, String[] args) {
        Player player = (Player)executor;
        
        if(player.isRegistered()) {
            player.notify("You have already registered your account.", ALERT);
            return;
        }
        
        player.showDialog(new RegistrationDialog());
    }
    
    @Override
    public String getName() {
        return "register";
    }
    
    @Override
    public String getDescription() {
        return "Shows a prompt with which you can register your account.";
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor instanceof Player;
    }
}
