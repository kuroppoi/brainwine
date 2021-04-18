package brainwine.gameserver.command;

public abstract class Command {
    
    public abstract void execute(CommandExecutor executor, String[] args);
    
    public boolean requiresAdmin() {
        return false;
    }
}
