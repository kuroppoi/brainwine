package brainwine.gameserver.command;

public abstract class Command {
    
    public abstract void execute(CommandExecutor executor, String[] args);
    public abstract String getUsage(CommandExecutor executor);
    
    public boolean canExecute(CommandExecutor executor) {
        return true;
    }
}
