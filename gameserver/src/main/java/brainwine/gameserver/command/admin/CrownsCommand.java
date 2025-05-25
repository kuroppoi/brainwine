package brainwine.gameserver.command.admin;

import static brainwine.gameserver.player.NotificationType.SYSTEM;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.command.Command;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.command.CommandInfo;
import brainwine.gameserver.player.Player;

@CommandInfo(name = "crowns", description = "Display or update a player's crown balance.")
public class CrownsCommand extends Command {
    
    @Override
    public void execute(CommandExecutor executor, String[] args) {
        if(args.length == 0 || args.length == 2) {
            executor.notify(String.format("Usage: %s", getUsage(executor)), SYSTEM);
            return;
        }

        Player target = GameServer.getInstance().getPlayerManager().getPlayer(args[0]);
        
        // Check if player exists
        if(target == null) {
            executor.notify("This player does not exist.", SYSTEM);
            return;
        }
        
        // Show number of crowns if no further arguments are given
        if(args.length == 1) {
            executor.notify(String.format("%s has %s crowns.", target.getName(), target.getCrowns()), SYSTEM);
            return;
        }
        
        int amount = 0;
        int currentAmount = target.getCrowns();
        
        try {
            amount = Integer.parseInt(args[2]);
        } catch(NumberFormatException e) {
            executor.notify("Amount must be a valid number.", SYSTEM);
            return;
        }
        
        // Update target player's crown balance
        switch(args[1]) {
        case "set":
            target.setCrowns(Math.max(0, amount));
            break;
        case "add":
            target.setCrowns(Math.max(0, currentAmount + amount)); // Can overflow but realistically won't matter
            break;
        case "remove":
            target.setCrowns(Math.max(0, currentAmount - amount));
            break;
        default:
            executor.notify(String.format("Usage: %s", getUsage(executor)), SYSTEM);
            return;
        }
        
        // Calculate balance difference and send notifications
        int difference = target.getCrowns() - currentAmount;
        amount = Math.abs(difference);
        
        if(difference > 0) {
            executor.notify(String.format("Gave %s crown%s to %s. (New balance: %s, was: %s)", amount, amount == 1 ? "" : "s", target.getName(), target.getCrowns(), currentAmount), SYSTEM);
            target.notify(String.format("You've received %s crown%s from an administrator.", amount, amount == 1 ? "" : "s"), SYSTEM);
            return;
        }
        
        if(difference < 0) {
            executor.notify(String.format("Took %s crown%s from %s. (New balance: %s, was: %s)", amount, amount  == 1 ? "" : "s", target.getName(), target.getCrowns(), currentAmount), SYSTEM);
            target.notify(String.format("%s crown%s %s been taken from your account by an administrator.", amount, amount == 1 ? "" : "s", amount == 1 ? "has" : "have"), SYSTEM);
            return;
        }
        
        executor.notify(String.format("No changes were made to %s's crown balance.", target.getName()), SYSTEM);
    }
    
    @Override
    public String getUsage(CommandExecutor executor) {
        return "/crowns <player> [<set|add|remove> <amount>]";
    }
    
    @Override
    public boolean canExecute(CommandExecutor executor) {
        return executor.isAdmin();
    }
}
