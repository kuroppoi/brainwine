package brainwine.gameserver.achievements;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;

import brainwine.gameserver.entity.player.Player;

public class RaiderAchievement extends Achievement {
    
    @JsonCreator
    public RaiderAchievement(@JacksonInject("title") String title) {
        super(title);
    }

    @Override
    public int getProgress(Player player) {
        return player.getStatistics().getDungeonsRaided();
    }
}
