package brainwine.gameserver.item.consumables;

import java.util.regex.Pattern;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.dialog.Dialog;
import brainwine.gameserver.dialog.DialogHelper;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.entity.player.PlayerManager;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.server.messages.EventMessage;
import brainwine.gameserver.server.messages.InventoryMessage;

/**
 * Consumable handler for name changers
 */
public class NameChangeConsumable implements Consumable {
    
    private static final Pattern namePattern = Pattern.compile("^[a-zA-Z0-9_.-]{4,20}$");
    
    @Override
    public void consume(Item item, Player player, Object details) {
        PlayerManager playerManager = GameServer.getInstance().getPlayerManager();
        Dialog dialog = DialogHelper.inputDialog("Change your name",
                "Your in-game name can include letters, numbers, dashes and periods, and must be between 4 and 20 characters in length.");
        
        player.showDialog(dialog, data -> {
            // Handle cancellation
            if(data.length == 1 && data[0].equals("cancel")) {
                player.sendMessage(new InventoryMessage(player.getInventory().getClientConfig(item)));
                return;
            }
            
            String name = data.length == 1 ? "" + data[0] : null;
            
            // Check if the data is present
            if(name == null) {
                fail(item, player, "Oops! There was a problem with your request.");
                return;
            }
            
            // Check if the name is valid
            if(!namePattern.matcher(name).matches()) {
                fail(item, player, "Please enter a valid name.");
                return;
            }
            
            // Check if name is already taken
            if(playerManager.getPlayer(name) != null) {
                fail(item, player, "That name is taken already.");
                return;
            }
            
            player.getInventory().removeItem(item); // Remove the consumable
            playerManager.changePlayerName(player, name); // Process the name change
            
            // TODO this creates a race condition
            player.sendMessage(new EventMessage("playerNameDidChange", name)); // Client side processing stuff
            player.kick("Your name has been changed."); // Force the player to reconnect
        });
    }
    
    private void fail(Item item, Player player, String message) {
        player.showDialog(DialogHelper.messageDialog(message));
        player.sendMessage(new InventoryMessage(player.getInventory().getClientConfig(item)));
    }
}
