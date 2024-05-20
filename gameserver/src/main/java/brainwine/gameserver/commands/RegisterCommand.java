package brainwine.gameserver.commands;

import static brainwine.gameserver.player.NotificationType.SYSTEM;

import org.apache.commons.validator.routines.EmailValidator;
import org.mindrot.jbcrypt.BCrypt;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.annotations.CommandInfo;
import brainwine.gameserver.dialog.DialogHelper;
import brainwine.gameserver.player.Player;

@CommandInfo(name = "register", description = "Shows a prompt with which you can register your account.")
public class RegisterCommand extends Command {
    
    @Override
    public void execute(CommandExecutor executor, String[] args) {
        Player player = (Player)executor;
        
        if(player.isRegistered()) {
            player.notify("You have already registered your account.", SYSTEM);
            return;
        }
        
        player.showDialog(DialogHelper.getDialog("request_email_registration"), input -> {
            if(input.length != 2) {
                return;
            }
            
            String email = input[0].toString();
            String password = input[1].toString();
            
            if(email.length() > 128 || !EmailValidator.getInstance().isValid(email)) {
                player.notify("Please enter a valid e-mail address.");
                return;
            }
            
            if(GameServer.getInstance().getPlayerManager().isEmailTaken(email)) {
                player.notify("Sorry, this e-mail address is already in use.");
                return;
            }
            
            if(!password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,64}$")) {
                player.notify("Please enter a valid password.");
                return;
            }
            
            // TODO e-mail validation code
            
            player.setEmail(email);
            player.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
            player.checkRegistration();
            player.notify("Your account has been successfully registered!");
        });
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return "/register";
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor instanceof Player;
    }
}
