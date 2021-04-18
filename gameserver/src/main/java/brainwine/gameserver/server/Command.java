package brainwine.gameserver.server;

import java.io.IOException;
import java.lang.reflect.Field;

import org.msgpack.unpacker.Unpacker;

import brainwine.gameserver.server.commands.BlocksIgnoreCommand;
import brainwine.gameserver.server.pipeline.Connection;

public abstract class Command {
    
    public abstract void process(Connection connection);
    
    /**
     * Can be overriden for custom unpacking rules, as seen in {@link BlocksIgnoreCommand}
     */
    public void unpack(Unpacker unpacker) throws IllegalArgumentException, IllegalAccessException, IOException {
        int dataCount = unpacker.readArrayBegin();
        Field[] fields = this.getClass().getFields();
        
        if(dataCount != fields.length) {
            throw new IOException(String.format("Amount of data received (%s) does not match expected amount (%s)", dataCount, fields.length));
        }
        
        for(Field field : fields) {
            field.set(this, unpacker.read(field.getType()));
        }
        
        unpacker.readArrayEnd();
    }
}
