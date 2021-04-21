package brainwine.gameserver.msgpack.templates;

import java.io.IOException;

import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.type.ValueType;
import org.msgpack.unpacker.Unpacker;

import brainwine.gameserver.msgpack.models.AppearanceData;

public class AppearanceDataTemplate extends AbstractTemplate<AppearanceData> {
    
    @Override
    public void write(Packer packer, AppearanceData data, boolean required) throws IOException {
        if(data == null) {
            if(required) {
                throw new MessageTypeException("Attempted to write null");
            }
            
            packer.writeNil();
            return;
        }
        
        packer.write(data);
    }
    
    @Override
    public AppearanceData read(Unpacker unpacker, AppearanceData to, boolean required) throws IOException {
        if(!required && unpacker.trySkipNil()) {
            return null;
        }
        
        if(unpacker.getNextType() != ValueType.MAP) {
            throw new MessageTypeException("Invalid data type");
        }
        
        AppearanceData data = new AppearanceData();
        int numEntries = unpacker.readMapBegin();
        
        for(int i = 0; i < numEntries; i++) {
            String key = unpacker.readString();
            Object value = null;
            
            if(unpacker.getNextType() == ValueType.RAW) {
                value = unpacker.readString();
            } else if(unpacker.getNextType() == ValueType.INTEGER) {
                value = unpacker.readInt();
            } else {
                throw new MessageTypeException("Invalid data type"); 
            }
            
            data.put(key, value);
        }
        
        unpacker.readMapEnd();
        return data;
    }
}
