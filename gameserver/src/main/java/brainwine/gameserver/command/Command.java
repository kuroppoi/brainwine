package brainwine.gameserver.command;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.Zone;

import static brainwine.gameserver.player.NotificationType.SYSTEM;

public abstract class Command {
    
    public abstract void execute(CommandExecutor executor, String[] args);
    public abstract String getUsage(CommandExecutor executor);
    
    public boolean canExecute(CommandExecutor executor) {
        return true;
    }

    public boolean useSmartArguments() {
        return false;
    }
    
    protected final boolean checkArgumentCount(CommandExecutor executor, String[] args, int... counts) {
        int highestCount = 0;
        
        for(int count : counts) {
            if(count > highestCount) {
                highestCount = count;
            }
            
            if(args.length == count) {
                return true;
            }
        }
        
        if(args.length > highestCount) {
            return true;
        }
        
        sendUsageMessage(executor);
        return false;
    }
    
    protected final void sendUsageMessage(CommandExecutor executor) {
        executor.notify(String.format("Usage: %s", getUsage(executor)), SYSTEM);
    }

    protected boolean isPrivileged(CommandExecutor executor, Zone zone, CommandAccessLevel needed) {
        if(executor == null || zone == null || needed == null) return false;
        if(executor instanceof GameServer) return true;
        if(executor.isAdmin()) return true;

        Player player = (Player)executor;
        if(needed == CommandAccessLevel.OWNERS) return zone.isOwner(player);
        if(needed == CommandAccessLevel.MEMBERS) return zone.isOwner(player) || zone.isMember(player);

        return true;

    }
}
