package brainwine.gameserver.achievement;

import java.util.List;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.player.PlayerStatistics;

public class CraftingAchievement extends Achievement {
    
    @JsonProperty("items")
    protected List<Item> items;
    
    @JsonProperty("workshopped")
    protected boolean workshopped;
    
    @JsonCreator
    public CraftingAchievement(@JacksonInject("title") String title) {
        super(title);
    }
    
    @Override
    public int getProgress(Player player) {
        PlayerStatistics statistics = player.getStatistics();
        
        if(items == null) {
            return workshopped ? statistics.getUniqueItemsWorkshopped() : statistics.getUniqueItemsCrafted();
        }
        
        return (int)(statistics.getItemsCrafted().entrySet().stream()
                .filter(entry -> entry.getValue() > 0 && (!workshopped || entry.getKey().requiresWorkshop()) && items.contains(entry.getKey()))
                .count());
    }
    
    @Override
    public int getQuantity() {
        return items == null ? quantity : items.size();
    }
}
