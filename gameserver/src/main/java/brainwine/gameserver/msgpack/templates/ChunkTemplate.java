package brainwine.gameserver.msgpack.templates;

import java.io.IOException;

import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.unpacker.Unpacker;

import brainwine.gameserver.zone.Block;
import brainwine.gameserver.zone.Chunk;

public class ChunkTemplate extends AbstractTemplate<Chunk> {

    @Override
    public void write(Packer packer, Chunk chunk, boolean required) throws IOException {
        if(chunk == null) {
            if(required) {
                throw new MessageTypeException("Attempted to write null");
            }
            
            packer.writeNil();
            return;
        }
        
        Block[] blocks = chunk.getBlocks();
        packer.writeArrayBegin(5);
        packer.write(chunk.getX());
        packer.write(chunk.getY());
        packer.write(chunk.getWidth());
        packer.write(chunk.getHeight());
        packer.write(blocks);
        packer.writeArrayEnd();
    }

    @Override
    public Chunk read(Unpacker unpacker, Chunk to, boolean required) throws IOException {
        if(!required && unpacker.trySkipNil()) {
            return null;
        }
        
        unpacker.readArrayBegin();
        int x = unpacker.readInt();
        int y = unpacker.readInt();
        int width = unpacker.readInt();
        int height = unpacker.readInt();
        Block[] blocks = unpacker.read(Block[].class);
        Chunk chunk = new Chunk(x, y, width, height);
        
        for(int i = 0; i < blocks.length; i++) {
            chunk.setBlock(i, blocks[i]);
        }
        
        unpacker.readArrayEnd();
        return chunk;
    }
}
