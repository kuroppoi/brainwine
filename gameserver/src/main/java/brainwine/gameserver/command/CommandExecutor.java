package brainwine.gameserver.command;

import brainwine.gameserver.player.NotificationType;

public interface CommandExecutor {
    
    public void notify(Object message, NotificationType type);
    public boolean isAdmin();
}
