package brainwine.gameserver.msgpack.templates;

import java.io.IOException;

import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.unpacker.Unpacker;

import brainwine.gameserver.zone.Block;

public class BlockArrayTemplate extends AbstractTemplate<Block[]> {
    
    @Override
    public void write(Packer packer, Block[] blocks, boolean required) throws IOException {
        if(blocks == null) {
            if(required) {
                throw new MessageTypeException("Attempted to write null");
            }
            
            packer.writeNil();
            return;
        }
        
        packer.writeArrayBegin(blocks.length * 3);
        
        for(Block block : blocks) {
            packer.write(block);
        }
        
        packer.writeArrayEnd();
    }
    
    @Override
    public Block[] read(Unpacker unpacker, Block[] to, boolean required) throws IOException {
        if(!required && unpacker.trySkipNil()) {
            return null;
        }
        
        Block[] blocks = new Block[unpacker.readArrayBegin() / 3];
        
        for(int i = 0; i < blocks.length; i++) {
            blocks[i] = unpacker.read(Block.class);
        }
        
        unpacker.readArrayEnd();
        return blocks;
    }
}
