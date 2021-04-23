package brainwine.gameserver.command;

public abstract class Command {
    
    public abstract void execute(CommandExecutor executor, String[] args);
    
    public abstract String getName();
    
    public String[] getAliases() {
        return null;
    }
    
    public String getDescription() {
        return "No description for this command";
    }
    
    public String getUsage() {
        return "/" + getName();
    }
    
    public boolean requiresAdmin() {
        return false;
    }
}
