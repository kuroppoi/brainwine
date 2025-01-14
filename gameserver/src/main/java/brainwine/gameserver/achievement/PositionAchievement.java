package brainwine.gameserver.achievement;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import brainwine.gameserver.player.Player;
import brainwine.gameserver.zone.Zone;

public class PositionAchievement extends Achievement {
    
    @JsonProperty("top")
    protected int top = -1;
    
    @JsonProperty("bottom")
    protected int bottom = -1;
    
    @JsonProperty("left")
    protected int left = -1;
    
    @JsonProperty("right")
    protected int right = -1;
    
    @JsonCreator
    public PositionAchievement(@JacksonInject("title") String title) {
        super(title);
    }
    
    @Override
    public boolean isCompleted(Player player) {
        Zone zone = player.getZone();
        int x = left >= 0 ? left : right >= 0 ? zone.getWidth() - right : -1;
        int y = top >= 0 ? top : bottom >= 0 ? zone.getHeight() - bottom : -1;
        return (x < 0 || Math.abs(player.getBlockX() - x) <= 1) && (y < 0 || Math.abs(player.getBlockY() - y) <= 1);
    }
}
