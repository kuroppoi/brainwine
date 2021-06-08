package brainwine.gameserver.command.commands;

import static brainwine.gameserver.entity.player.NotificationType.ALERT;

import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemRegistry;
import brainwine.gameserver.item.Layer;

public class FillCommand extends Command {
    
    @Override
    public void execute(CommandExecutor executor, String[] args) {
        if(args.length < 4 || args.length > 6) {
            executor.notify(String.format("Usage: %s", getUsage(executor)), ALERT);
            return;
        }
        
        Item item = Item.AIR;
        Layer layer = Layer.FRONT;
        
        if (args.length > 4) {
        	try {
	            item = ItemRegistry.getItem(Integer.parseInt(args[4]));
	        } catch(NumberFormatException e) {
	            item = ItemRegistry.getItem(args[4]);
	        }
	    }
        
        if (args.length > 5) {
            try {
            	layer = Layer.valueOf(args[5].toUpperCase());
            } catch(Exception e) {
            	layer = Layer.FRONT;
            }
        }
        
        try {
            for (int y = 0; y < Integer.parseInt(args[3]); y++) {
            	for (int x = 0; x < Integer.parseInt(args[2]); x++) {
            		((Player)executor).getZone().updateBlock(Integer.parseInt(args[0]) + x, Integer.parseInt(args[1]) + y, layer, item);
                }
            }
            executor.notify(String.format("Filled Region: %s %s - %s %s with %s on the %s layer", args[0], args[1], 
            		Integer.parseInt(args[0]) + Integer.parseInt(args[2]),
            		Integer.parseInt(args[1]) + Integer.parseInt(args[3]), item.getTitle(), layer.toString()), ALERT);
            return;
        } catch(NumberFormatException e) {
            executor.notify(String.format("Usage: %s", getUsage(executor)), ALERT);
            return;
        }
    }

    @Override
    public String getName() {
        return "fill";
    }
    
    @Override
    public String getDescription() {
        return "Fills region with block of type.";
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return "/fill <x> <y> <w> <h> <block> [layer / front]";
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor.isAdmin();
    }
}