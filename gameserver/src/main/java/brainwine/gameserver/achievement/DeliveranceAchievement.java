package brainwine.gameserver.achievement;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;

import brainwine.gameserver.player.Player;

public class DeliveranceAchievement extends Achievement {
    
    @JsonCreator
    public DeliveranceAchievement(@JacksonInject("title") String title) {
        super(title);
    }

    @Override
    public int getProgress(Player player) {
        return player.getStatistics().getDeliverances();
    }
}