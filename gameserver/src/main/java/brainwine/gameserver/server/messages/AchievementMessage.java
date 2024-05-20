package brainwine.gameserver.server.messages;

import brainwine.gameserver.server.Message;
import brainwine.gameserver.server.MessageInfo;

@MessageInfo(id = 29, collection = true)
public class AchievementMessage extends Message {
    
    public String title;
    public int experience;
    
    public AchievementMessage(String title, int experience) {
        this.title = title;
        this.experience = experience;
    }
}
