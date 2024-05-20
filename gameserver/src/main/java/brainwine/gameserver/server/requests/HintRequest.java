package brainwine.gameserver.server.requests;

import brainwine.gameserver.annotations.RequestInfo;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.server.PlayerRequest;

@RequestInfo(id = 36)
public class HintRequest extends PlayerRequest {
    
    public String hint;
    
    @Override
    public void process(Player player) {
        player.ignoreHint(hint);
    }
}
