package brainwine.gameserver.command.commands;

import static brainwine.gameserver.entity.player.NotificationType.ALERT;
import static brainwine.gameserver.entity.player.NotificationType.SYSTEM;

import java.util.ArrayList;
import java.util.List;

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
            executor.notify(String.format("Usage: %s", getUsage(executor)), ALERT);
            return;
        }
        
        Player target = GameServer.getInstance().getPlayerManager().getPlayer(args[0]);
        
        if(target == null) {
            executor.notify("That player does not exist.", ALERT);
            return;
        }
        
        List<Item> items = new ArrayList<>();
        String title = null;
        
        if(args[1].equalsIgnoreCase("all")) {
            title = "of every item";
            
            for(Item item : ItemRegistry.getItems()) {
                if(!item.isClothing() && !item.isAir()) {
                    items.add(item);
                }
            }
        } else {
            Item item = Item.AIR;
            
            try {
                item = ItemRegistry.getItem(Integer.parseInt(args[1]));
            } catch(NumberFormatException e) {
                item = ItemRegistry.getItem(args[1]);
            }
            
            if(item.isAir()) {
                executor.notify("This item does not exist.", ALERT);
                return;
            }
            
            title = item.getTitle();
            items.add(item);
        }
        
        int quantity = 1;
        
        if(args.length > 2) {
            try {
                quantity = Integer.parseInt(args[2]);
            } catch(NumberFormatException e) {
                executor.notify("Quantity must be a valid number.", ALERT);
                return;
            }
        }
        
        if(quantity > 0) {
            for(Item item : items) {
                target.getInventory().addItem(item, quantity, true);
            }
            
            target.alert(String.format("You received %s %s from an administrator.", quantity, title));
            executor.notify(String.format("Gave %s %s to %s", quantity, title, target.getName()), SYSTEM);
        } else {
            for(Item item : items) {
                target.getInventory().removeItem(item, -quantity, true);
            }
            
            target.alert(String.format("%s %s was taken from your inventory.", -quantity, title));
            executor.notify(String.format("Took %s %s from %s", -quantity, title, target.getName()), SYSTEM);
        }
    }

    @Override
    public String getName() {
        return "give";
    }
    
    @Override
    public String getDescription() {
        return "Adds items to a player's inventory.";
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return "/give <player> <item|all> [quantity]";
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor.isAdmin();
    }
}
