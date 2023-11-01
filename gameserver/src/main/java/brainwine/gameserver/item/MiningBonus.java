package brainwine.gameserver.item;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.entity.player.Skill;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MiningBonus {
    
    private double chance;
    private Skill skill;
    private ItemGroup tool;
    private LazyItemGetter item;
    private boolean doubleLoot;
    private String notification;
    
    @JsonCreator
    private MiningBonus() {}
    
    public double getChance() {
        return chance;
    }
    
    public Skill getSkill() {
        return skill;
    }
    
    public ItemGroup getTool() {
        return tool;
    }
    
    public Item getItem() {
        return item == null ? Item.AIR : item.get();
    }
    
    @JsonProperty("double")
    public boolean isDoubleLoot() {
        return doubleLoot;
    }
    
    public String getNotification() {
        return notification;
    }
}
