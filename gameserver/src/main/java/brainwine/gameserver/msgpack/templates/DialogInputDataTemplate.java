package brainwine.gameserver.msgpack.templates;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.type.ValueType;
import org.msgpack.unpacker.Unpacker;

import brainwine.gameserver.msgpack.models.DialogInputData;

public class DialogInputDataTemplate extends AbstractTemplate<DialogInputData> {
    
    @Override
    public void write(Packer packer, DialogInputData data, boolean required) throws IOException {
        if(data == null) {
            if(required) {
                throw new MessageTypeException("Attempted to write null");
            }
            
            packer.writeNil();
            return;
        }
        
        if(data.isType1()) {
            packer.write(data.getDialogName());
        } else if(data.isType2()) {
            packer.write(data.getDialogId());
            packer.write(data.getInputData());
        }
    }
    
    @Override
    public DialogInputData read(Unpacker unpacker, DialogInputData to, boolean required) throws IOException {
        if(!required && unpacker.trySkipNil()) {
            return null;
        }
        
        if(unpacker.getNextType() == ValueType.RAW) {
            return new DialogInputData(unpacker.readString());
        } else if(unpacker.getNextType() == ValueType.INTEGER) {
            int id = unpacker.readInt();
            
            if(unpacker.getNextType() == ValueType.RAW) {
                return new DialogInputData(id, unpacker.readString());
            }
            
            Map<String, String> input = new HashMap<>();
            int numEntries = unpacker.readMapBegin();
            
            for(int i = 0; i < numEntries; i++) {
                input.put(unpacker.readString(), unpacker.readString());
            }
            
            unpacker.readMapEnd();
            return new DialogInputData(id, input);
        }
        
        throw new MessageTypeException("Invalid data type");
    }
}
