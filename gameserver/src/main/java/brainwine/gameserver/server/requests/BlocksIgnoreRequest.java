package brainwine.gameserver.server.requests;

import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.server.PlayerRequest;

/**
 * No longer used. See {@link Player#removeOutOfRangeChunks()}
 */
public class BlocksIgnoreRequest extends PlayerRequest {

    public int[] chunkIndexes;
    
    @Override
    public void process(Player player) {
        /**
        for(int index : chunkIndexes) {
            player.removeActiveChunk(index);
        }
        **/
    }
}
