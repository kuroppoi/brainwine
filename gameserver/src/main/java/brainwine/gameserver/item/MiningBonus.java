package brainwine.gameserver.item;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.player.Skill;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MiningBonus {
    
    private double chance;
    private Skill skill;
    private ItemGroup tool;
    private String item;
    private boolean doubleLoot;
    private String notification;
    private ItemUseType accessory;
    
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

    public ItemUseType getAccessory() {
        return accessory;
    }

    public String getItem() {
        return item;
    }

    public Item computeItem(Item minedItem) {
        if(this.item == null) {
            return Item.AIR;
        }
        if(this.item.startsWith("-") && minedItem != null) {
            return ItemRegistry.getItem(minedItem.getId() + this.item);
        }
        return ItemRegistry.getItem(this.item);
    }
    
    @JsonProperty("double")
    public boolean isDoubleLoot() {
        return doubleLoot;
    }
    
    public String getNotification() {
        return notification;
    }
}
