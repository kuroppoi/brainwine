package brainwine.gameserver.achievements;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;

import brainwine.gameserver.player.Player;

public class ExploringAchievement extends Achievement {
    
    @JsonCreator
    public ExploringAchievement(@JacksonInject("title") String title) {
        super(title);
    }
    
    @Override
    public int getProgress(Player player) {
        return player.getStatistics().getAreasExplored();
    }
}
