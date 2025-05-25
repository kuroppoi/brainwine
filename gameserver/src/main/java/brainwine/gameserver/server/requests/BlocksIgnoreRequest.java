package brainwine.gameserver.server.requests;

import brainwine.gameserver.player.Player;
import brainwine.gameserver.server.PlayerRequest;
import brainwine.gameserver.server.RequestInfo;

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
