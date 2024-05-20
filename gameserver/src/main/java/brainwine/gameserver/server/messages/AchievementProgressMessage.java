package brainwine.gameserver.server.messages;

import brainwine.gameserver.server.Message;
import brainwine.gameserver.server.MessageInfo;

@MessageInfo(id = 48, collection = true)
public class AchievementProgressMessage extends Message {
    
    public String title;
    public int progress;
    
    public AchievementProgressMessage(String title, int progress) {
        this.title = title;
        this.progress = progress;
    }
}
