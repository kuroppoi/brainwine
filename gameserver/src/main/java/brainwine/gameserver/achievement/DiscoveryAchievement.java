package brainwine.gameserver.achievement;

import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemGroup;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.player.PlayerStatistics;

public class DiscoveryAchievement extends Achievement {
    
    @JsonProperty("item")
    protected Item item;
    
    @JsonProperty("group")
    protected ItemGroup group;
    
    @JsonCreator
    public DiscoveryAchievement(@JacksonInject("title") String title) {
        super(title);
    }
    
    @Override
    public int getProgress(Player player) {
        PlayerStatistics statistics = player.getStatistics();
        
        if(item != null) {
            return statistics.getDiscoveries(item);
        } else if(group != null) {
            return statistics.getDiscoveries().entrySet().stream()
                    .filter(entry -> entry.getKey().getGroup() == group)
                    .map(Entry::getValue)
                    .reduce(Integer::sum)
                    .orElse(0);
        }
        
        return 0;
    }
}
