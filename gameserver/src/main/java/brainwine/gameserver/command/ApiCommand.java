package brainwine.gameserver.command;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.dialog.Dialog;
import brainwine.gameserver.dialog.DialogSection;
import brainwine.gameserver.player.Player;

@CommandInfo(name = "api", description = "Lets you configure your API settings.")
public class ApiCommand extends Command {
    
    @Override
    public void execute(CommandExecutor executor, String[] args) {
        Player player = ((Player)executor);
        
        // Create & show settings dialog
        Dialog dialog = new Dialog();
        dialog.setTitle("API Settings");
        dialog.addSection(new DialogSection().setTitle("Your API token:").setText(String.format(player.isV3() ? "<color=#ffd95f>%s</color>" : "%s", player.getApiToken())).setTextColor("ffd95f"));
        dialog.addSection(new DialogSection().setText("Generate new API token").setChoice("reissue"));
        player.showDialog(dialog, input -> {
            // Handle cancellation
            if(input.length == 0 || (input.length == 1 && "cancel".equals(input[0]))) {
                return;
            }
            
            if("reissue".equals(input[0]) && GameServer.getInstance().getPlayerManager().issueApiToken(player)) {
                player.kick("API token changed.", true);
            }
        });
    }

    @Override
    public String getUsage(CommandExecutor executor) {
        return "/api";
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor instanceof Player;
    }
}
