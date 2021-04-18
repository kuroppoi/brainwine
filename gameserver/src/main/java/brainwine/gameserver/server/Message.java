package brainwine.gameserver.server;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.msgpack.packer.Packer;

/**
 * Messages are outgoing packets to the client.
 */
public abstract class Message {
    
    public void pack(Packer packer) throws IOException, IllegalArgumentException, IllegalAccessException {
        Field[] fields = getClass().getFields();
        
        if(isPrepacked()) {
            if(fields.length != 1 || !Collection.class.isAssignableFrom(fields[0].getType())) {
                throw new IOException("Prepacked messages may only contain 1 field that must be a Collection.");
            }
            
            packer.write(fields[0].get(this));
        } else {
            List<Object> data = new ArrayList<>();
            
            for(Field field : fields) {
                data.add(field.get(this));
            }
            
            if(isCollection()) {
                packer.write(Arrays.asList(data));
            } else {
                packer.write(data);
            }
        }
    }
    
    public boolean isJson() {
        return false;
    }
    
    public boolean isCompressed() {
        return false;
    }
    
    public boolean isCollection() {
        return false;
    }
    
    public boolean isPrepacked() {
        return false;
    }
}
