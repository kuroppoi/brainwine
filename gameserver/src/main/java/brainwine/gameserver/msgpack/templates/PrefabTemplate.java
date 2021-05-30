package brainwine.gameserver.msgpack.templates;

import java.io.IOException;

import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.unpacker.Unpacker;

import brainwine.gameserver.prefab.Prefab;
import brainwine.gameserver.zone.Block;

public class PrefabTemplate extends AbstractTemplate<Prefab> {
    
    @Override
    public void write(Packer packer, Prefab prefab, boolean required) throws IOException {
        if(prefab == null) {
            if(required) {
                throw new MessageTypeException("Attempted to write null");
            }
            
            packer.writeNil();
            return;
        }
        
        packer.write(prefab.getWidth());
        packer.write(prefab.getHeight());
        packer.write(prefab.getBlocks());
    }
    
    @Override
    public Prefab read(Unpacker unpacker, Prefab to, boolean required) throws IOException {
        if(!required && unpacker.trySkipNil()) {
            return null;
        }
        
        int width = unpacker.readInt();
        int height = unpacker.readInt();
        Block[] blocks = unpacker.read(Block[].class);
        
        if(to != null) {
            to.setWidth(width);
            to.setHeight(height);
            to.setBlocks(blocks);
            return to;
        }
        
        return new Prefab(width, height, blocks);
    }
}
