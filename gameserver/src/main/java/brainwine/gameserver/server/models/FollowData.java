package brainwine.gameserver.server.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

import brainwine.gameserver.player.Player;

@JsonFormat(shape = Shape.ARRAY)
public class FollowData {

    private final String playerName;
    private final String playerId;
    private final int direction;
    private final boolean following;
    
    public FollowData(Player player, int direction, boolean following) {
        this.playerName = player.getName();
        this.playerId = player.getDocumentId();
        this.direction = direction;
        this.following = following;
    }
    
    public FollowData(Player player, int direction) {
        this(player, direction, true);
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public String getPlayerId() {
        return playerId;
    }
    
    public int getDirection() {
        return direction;
    }
    
    public boolean isFollowing() {
        return following;
    }
}
