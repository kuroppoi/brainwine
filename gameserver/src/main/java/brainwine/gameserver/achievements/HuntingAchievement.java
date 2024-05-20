package brainwine.gameserver.achievements;

import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.entity.EntityGroup;
import brainwine.gameserver.player.Player;

public class HuntingAchievement extends Achievement {
    
    @JsonProperty(value = "group", required = true)
    protected EntityGroup group;
    
    @JsonCreator
    public HuntingAchievement(@JacksonInject("title") String title) {
        super(title);
    }
    
    @Override
    public int getProgress(Player player) {
        return player.getStatistics().getKills().entrySet().stream()
                .filter(entry -> entry.getKey().getGroup() == group)
                .map(Entry::getValue)
                .reduce(Integer::sum)
                .orElse(0);
    }
}
