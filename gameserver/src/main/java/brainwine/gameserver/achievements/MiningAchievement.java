package brainwine.gameserver.achievements;

import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.item.ItemGroup;
import brainwine.gameserver.player.Player;

public class MiningAchievement extends Achievement {
    
    @JsonProperty(value = "group", required = true)
    protected ItemGroup group;
    
    @JsonCreator
    public MiningAchievement(@JacksonInject("title") String title) {
        super(title);
    }
    
    @Override
    public int getProgress(Player player) {
        return player.getStatistics().getItemsMined().entrySet().stream()
                .filter(entry -> entry.getKey().getGroup() == group)
                .map(Entry::getValue)
                .reduce(Integer::sum)
                .orElse(0);
    }
}
