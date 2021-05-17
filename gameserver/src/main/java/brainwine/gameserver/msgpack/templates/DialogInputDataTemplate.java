package brainwine.gameserver.msgpack.templates;

import java.io.IOException;

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
            String dialogName = unpacker.readString();
            unpacker.readValue(); // TODO find out if this is the action, or just garbage data.
            return new DialogInputData(dialogName);
        } else if(unpacker.getNextType() == ValueType.INTEGER) {
            int id = unpacker.readInt();
            
            if(unpacker.getNextType() == ValueType.RAW) {
                return new DialogInputData(id, unpacker.readString());
            } else if(unpacker.getNextType() == ValueType.ARRAY) {
                return new DialogInputData(id, unpacker.read(String[].class));
            }
            
            int numEntries = unpacker.readMapBegin();
            String[] input = new String[numEntries];
            
            for(int i = 0; i < numEntries; i++) {
                unpacker.readString(); // Key, ignore
                input[i] = unpacker.readString();
            }
            
            unpacker.readMapEnd();
            return new DialogInputData(id, input);
        }
        
        throw new MessageTypeException("Invalid data type");
    }
}
