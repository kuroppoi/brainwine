package brainwine.gameserver.server.requests;

import brainwine.gameserver.annotations.RequestInfo;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.server.PlayerRequest;

@RequestInfo(id = 25)
public class BlocksIgnoreRequest extends PlayerRequest {

    public int[] chunkIndices;
    
    @Override
    public void process(Player player) {
        for(int index : chunkIndices) {
            player.removeActiveChunk(index);
        }
    }
}
