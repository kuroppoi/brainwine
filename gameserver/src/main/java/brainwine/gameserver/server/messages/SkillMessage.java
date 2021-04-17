package brainwine.gameserver.server.messages;

import brainwine.gameserver.entity.player.Skill;
import brainwine.gameserver.server.Message;
import brainwine.gameserver.server.RegisterMessage;

@RegisterMessage(id = 35, collection = true)
public class SkillMessage extends Message {
    
    public Skill skill;
    public int level;
    
    public SkillMessage(Skill skill, int level) {
        this.skill = skill;
        this.level = level;
    }
}
