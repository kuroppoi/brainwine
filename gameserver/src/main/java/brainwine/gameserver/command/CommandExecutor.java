package brainwine.gameserver.command;

public interface CommandExecutor {
    
    public void notify(String text, NotificationType type);
    public boolean isAdmin();
}
