package brainwine.gameserver.msgpack.templates;

import java.io.IOException;

import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.unpacker.Unpacker;

import brainwine.gameserver.zone.Block;

public class BlockTemplate extends AbstractTemplate<Block> {
    
    @Override
    public void write(Packer packer, Block block, boolean required) throws IOException {
        if(block == null) {
            if(required) {
                throw new MessageTypeException("Attempted to write null");
            }
            
            packer.writeNil();
            return;
        }
        
        packer.write(block.getBaseItem().getId() | (((block.getLiquidItem().getId() & 255) << 8) | ((block.getLiquidMod() & 31) << 16)));
        packer.write(block.getBackItem().getId() | ((block.getBackMod() & 31) << 16));
        packer.write(block.getFrontItem().getId() | ((block.getFrontMod() & 31) << 16));
    }
    
    @Override
    public Block read(Unpacker unpacker, Block to, boolean required) throws IOException {
        if(!required && unpacker.trySkipNil()) {
            return null;
        }
        
        int base = unpacker.readInt();
        int back = unpacker.readInt();
        int front = unpacker.readInt();
        return new Block(base & 15, back & 65535, back >> 16 & 31, front & 65535, front >> 16 & 31, base >> 8 & 255, base >> 16 & 31);
    }
}
