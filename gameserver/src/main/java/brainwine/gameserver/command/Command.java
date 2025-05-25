package brainwine.gameserver.command;

import static brainwine.gameserver.player.NotificationType.SYSTEM;

public abstract class Command {
    
    public abstract void execute(CommandExecutor executor, String[] args);
    public abstract String getUsage(CommandExecutor executor);
    
    public boolean canExecute(CommandExecutor executor) {
        return true;
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
}
