package brainwine.gameserver.server.requests;

import java.io.IOException;

import org.msgpack.type.ValueType;
import org.msgpack.unpacker.Unpacker;

import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.server.PlayerRequest;

/**
 * No longer used. See {@link Player#removeOutOfRangeChunks()}
 */
public class BlocksIgnoreRequest extends PlayerRequest {

    public int[] chunkIndexes;
    
    @Override
    public void unpack(Unpacker unpacker) throws IOException {
        int length = unpacker.readArrayBegin();
        
        if(unpacker.getNextType() == ValueType.INTEGER) {
            chunkIndexes = new int[length];
            
            for(int i = 0; i < length; i++) {
                chunkIndexes[i] = unpacker.readInt();
            }
        } else {
            chunkIndexes = unpacker.read(int[].class);
        }
        
        unpacker.readArrayEnd();
    }
    
    @Override
    public void process(Player player) {
        /**
        for(int index : chunkIndexes) {
            player.removeActiveChunk(index);
        }
        **/
    }
}
