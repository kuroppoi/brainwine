package brainwine.gameserver.server;

import java.io.IOException;
import java.lang.reflect.Field;

import org.msgpack.unpacker.Unpacker;

import brainwine.gameserver.server.pipeline.Connection;
import brainwine.gameserver.server.requests.BlocksIgnoreRequest;

/**
 * Requests are incoming packets from the client.
 */
public abstract class Request {
    
    public abstract void process(Connection connection);
    
    /**
     * Can be overriden for custom unpacking rules, as seen in {@link BlocksIgnoreRequest}
     */
    public void unpack(Unpacker unpacker) throws IllegalArgumentException, IllegalAccessException, IOException {
        unpacker.readArrayBegin();
        Field[] fields = this.getClass().getFields();
        
        for(Field field : fields) {
            try {
                unpacker.getNextType();
            } catch(Exception e) {
                if(field.getAnnotation(OptionalField.class) == null) {
                    System.out.println(field.getName());
                    throw e;
                }
                
                break;
            }
            
            field.set(this, unpacker.read(field.getType()));
        }
        
        unpacker.readArrayEnd(true);
    }
}
