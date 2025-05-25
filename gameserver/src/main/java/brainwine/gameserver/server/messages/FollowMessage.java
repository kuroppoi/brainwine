package brainwine.gameserver.server.messages;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import brainwine.gameserver.player.Player;
import brainwine.gameserver.server.Message;
import brainwine.gameserver.server.MessageInfo;
import brainwine.gameserver.server.models.FollowData;

@MessageInfo(id = 27, prepacked = true)
public class FollowMessage extends Message {
    
    public Collection<FollowData> followData;
    
    public FollowMessage(Collection<FollowData> followData) {
        this.followData = followData;
    }
    
    public FollowMessage(Player player, int direction, boolean following) {
        this(Arrays.asList(new FollowData(player, direction, following)));
    }
    
    public FollowMessage(Collection<Player> players, int direction) {
        this(players.stream().map(player -> new FollowData(player, direction, true)).collect(Collectors.toList()));
    }
}
