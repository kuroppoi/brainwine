package brainwine.gameserver.msgpack.templates;

import java.io.IOException;

import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.type.ValueType;
import org.msgpack.unpacker.Unpacker;

import brainwine.gameserver.msgpack.models.BlockUseData;

public class BlockUseDataTemplate extends AbstractTemplate<BlockUseData> {
    
    @Override
    public void write(Packer packer, BlockUseData data, boolean required) throws IOException {
        if(data == null) {
            if(required) {
                throw new MessageTypeException("Attempted to write null");
            }
            
            packer.writeNil();
            return;
        }
        
        packer.write(data.getData());
    }
    
    @Override
    public BlockUseData read(Unpacker unpacker, BlockUseData to, boolean required) throws IOException {
        if(!required && unpacker.trySkipNil()) {
            return null;
        }
        
        if(unpacker.getNextType() == ValueType.ARRAY) {
            Object[] data = new Object[unpacker.readArrayBegin()];
            
            for(int i = 0; i < data.length; i++) {
                data[i] = readObject(unpacker);
            }
            
            unpacker.readArrayEnd();
            return new BlockUseData(data);
        } else if(unpacker.getNextType() == ValueType.MAP) {
            Object[] data = new Object[unpacker.readMapBegin()];
            
            for(int i = 0; i < data.length; i++) {
                unpacker.readString(); // Key, ignore
                data[i] = readObject(unpacker);
            }
            
            unpacker.readMapEnd();
            return new BlockUseData(data);
        }
        
        throw new MessageTypeException("Invalid data type");
    }
    
    private Object readObject(Unpacker unpacker) throws IOException {
        switch(unpacker.getNextType()) {
        case RAW:
            return unpacker.readString();
        case INTEGER:
            return unpacker.readInt();
        case FLOAT:
            return unpacker.readFloat();
        default:
            throw new MessageTypeException("Invalid data type");
        }
    }
}
