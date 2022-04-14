package brainwine.gameserver.server.requests;

import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.server.PlayerRequest;

public class BlocksIgnoreRequest extends PlayerRequest {

    public int[] chunkIndices;
    
    @Override
    public void process(Player player) {
        for(int index : chunkIndices) {
            player.removeActiveChunk(index);
        }
    }
}
