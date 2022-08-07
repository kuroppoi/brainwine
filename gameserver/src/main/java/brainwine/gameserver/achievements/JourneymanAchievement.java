package brainwine.gameserver.achievements;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;

import brainwine.gameserver.entity.player.Player;

public class JourneymanAchievement extends Achievement {
    
    @JsonCreator
    public JourneymanAchievement(@JacksonInject("title") String title) {
        super(title);
    }
    
    @Override
    public boolean isCompleted(Player player) {
        return true;
    }
}
