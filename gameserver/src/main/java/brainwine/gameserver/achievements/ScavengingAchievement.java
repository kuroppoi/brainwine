package brainwine.gameserver.achievements;

import java.util.List;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.entity.player.PlayerStatistics;
import brainwine.gameserver.item.Item;

public class ScavengingAchievement extends Achievement {
    
    @JsonProperty("items")
    protected List<Item> items;
    
    @JsonCreator
    public ScavengingAchievement(@JacksonInject("title") String title) {
        super(title);
    }

    @Override
    public int getProgress(Player player) {
        PlayerStatistics statistics = player.getStatistics();
        
        if(items == null) {
            return statistics.getUniqueItemsScavenged();
        } else {
            return (int)(statistics.getItemsScavenged().entrySet().stream()
                    .filter(entry -> entry.getValue() > 0 && items.contains(entry.getKey()))
                    .count());
        }
    }
    
    @Override
    public int getQuantity() {
        return items == null ? quantity : items.size();
    }
}
