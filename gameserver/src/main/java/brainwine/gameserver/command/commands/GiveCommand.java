package brainwine.gameserver.command.commands;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemRegistry;

public class GiveCommand extends Command {
    
    @Override
    public void execute(CommandExecutor executor, String[] args) {
        if(args.length < 2) {
            executor.sendMessage(String.format("Usage: %s", getUsage()));
            return;
        }
        
        Player target = GameServer.getInstance().getPlayerManager().getPlayer(args[0]);
        
        if(target == null) {
            executor.sendMessage("That player does not exist.");
            return;
        }
        
        Item item = Item.AIR;
        
        try {
            item = ItemRegistry.getItem(Integer.parseInt(args[1]));
        } catch(NumberFormatException e) {
            item = ItemRegistry.getItem(args[1]);
        }
        
        if(item.isAir()) {
            executor.sendMessage("This item does not exist.");
            return;
        }
        
        int quantity = 1;
        
        if(args.length > 2) {
            try {
                quantity = Integer.parseInt(args[2]);
            } catch(NumberFormatException e) {
                executor.sendMessage("Quantity must be a valid number.");
                return;
            }
        }
        
        if(quantity > 0) {
            target.getInventory().addItem(item, quantity);
            target.alert(String.format("You received %s %s from an administrator.", quantity, item.getTitle()));
            executor.sendMessage(String.format("Gave %s %s to %s", quantity, item.getTitle(), target.getName()));
        } else {
            target.getInventory().removeItem(item, -quantity);
            target.alert(String.format("%s %s was taken from your inventory.", -quantity, item.getTitle()));
            executor.sendMessage(String.format("Took %s %s from %s", quantity, item.getTitle(), target.getName()));
        }
    }

    @Override
    public String getName() {
        return "give";
    }
    
    @Override
    public String getDescription() {
        return "Gives the specified amount of the specified item to the specified player.";
    }
    
    @Override
    public String getUsage() {
        return "/give <player> <item> [quantity]";
    }
    
    @Override
    public boolean requiresAdmin() {
        return true;
    }
}
