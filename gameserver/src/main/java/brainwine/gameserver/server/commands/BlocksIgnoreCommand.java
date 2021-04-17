package brainwine.gameserver.server.commands;

import java.io.IOException;

import org.msgpack.unpacker.Unpacker;

import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.server.PlayerCommand;
import brainwine.gameserver.server.RegisterCommand;

/**
 * No longer used. See {@link Player#removeOutOfRangeChunks()}
 */
@RegisterCommand(id = 25)
public class BlocksIgnoreCommand extends PlayerCommand {

    public int[] chunkIndexes;
    
    @Override
    public void unpack(Unpacker unpacker) throws IOException {
        chunkIndexes = unpacker.read(int[].class);
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
