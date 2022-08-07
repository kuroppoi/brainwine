package brainwine.gameserver.achievements;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonValue;

import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.util.MathUtils;

@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "type", defaultImpl = Achievement.class)
@JsonSubTypes({
    @Type(name = "MiningAchievement", value = MiningAchievement.class),
    @Type(name = "CraftingAchievement", value = CraftingAchievement.class),
    @Type(name = "RaiderAchievement", value = RaiderAchievement.class),
    @Type(name = "LooterAchievement", value = LooterAchievement.class),
    @Type(name = "ExploringAchievement", value = ExploringAchievement.class),
    @Type(name = "HuntingAchievement", value = HuntingAchievement.class),
    @Type(name = "SidekickAchievement", value = SidekickAchievement.class),
    @Type(name = "ScavengingAchievement", value = ScavengingAchievement.class),
    @Type(name = "DiscoveryAchievement", value = DiscoveryAchievement.class),
    @Type(name = "Journeyman", value = JourneymanAchievement.class)
})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Achievement {
    
    protected final String title;
    
    @JsonProperty("xp")
    protected int experience;
    
    @JsonProperty("quantity")
    protected int quantity;
    
    @JsonProperty("progress")
    protected String progressKey;
    
    @JsonProperty("notify")
    protected String notification;
    
    @JsonProperty("previous")
    protected LazyAchievementGetter previous;
    
    public Achievement(String title) {
        this.title = title;
    }
    
    @JsonCreator(mode = Mode.DELEGATING)
    private static Achievement fromTitle(String title) {
        Achievement achievement = AchievementManager.getAchievement(title);
        
        if(achievement == null) {
            throw new IllegalArgumentException(String.format("No achievement exists with title '%s'", title));
        }
        
        return achievement;
    }
    
    public boolean isCompleted(Player player) {
        return getProgressPercent(player) >= 100;
    }
    
    public int getProgress(Player player) {
        return 0;
    }
    
    public final int getProgressPercent(Player player) {
        return getProgressPercent(getProgress(player));
    }
    
    public final int getProgressPercent(int progress) {
        return MathUtils.clamp((int)((progress - getPreviousTotalQuantity()) / (double)Math.max(1, getQuantity()) * 100), 0, 100);
    }
    
    public int getPreviousTotalQuantity() {
        int quantity = 0;
        
        if(progressKey != null) {
            Achievement current = this;
            
            while(current.hasPrevious()) {
                current = current.getPrevious();
                quantity += current.getQuantity();
            }
        }
        
        return quantity;
    }
    
    @JsonValue
    public String getTitle() {
        return title;
    }
    
    public int getExperience() {
        return experience;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public String getNotification() {
        return notification;
    }
    
    public boolean hasPrevious() {
        return previous != null;
    }
    
    public Achievement getPrevious() {
        return hasPrevious() ? previous.get() : null;
    }
}
