package brainwine.gameserver.server.messages;

import brainwine.gameserver.annotations.MessageInfo;
import brainwine.gameserver.server.Message;

@MessageInfo(id = 29, collection = true)
public class AchievementMessage extends Message {
    
    public String title;
    public int experience;
    
    public AchievementMessage(String title, int experience) {
        this.title = title;
        this.experience = experience;
    }
}
