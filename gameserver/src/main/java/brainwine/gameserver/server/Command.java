package brainwine.gameserver.server;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.msgpack.unpacker.Unpacker;

import brainwine.gameserver.reflections.ReflectionsHelper;
import brainwine.gameserver.server.pipeline.Connection;

@SuppressWarnings("unchecked")
public abstract class Command {
    
    private static final Logger logger = LogManager.getLogger();
    private static final Map<Integer, Class<? extends Command>> commands = new HashMap<>();
    
    static {
        Set<Class<?>> classes = ReflectionsHelper.getTypesAnnotatedWith(RegisterCommand.class);
        
        for(Class<?> clazz : classes) {
            if(!Command.class.isAssignableFrom(clazz)) {
                logger.warn("Attempted to register non-command {}", clazz.getName());
                continue;
            }
            
            RegisterCommand info = clazz.getAnnotation(RegisterCommand.class);
            registerCommand(info.id(), (Class<? extends Command>)clazz);
        }
    }
    
    public static void registerCommand(int id, Class<? extends Command> type) {
        if(commands.containsKey(id)) {
            logger.warn("Attempted to register duplicate command {}", type.getTypeName());
            return;
        }
        
        commands.put(id, type);
    }
    
    public static Command instantiateCommand(int id) throws InstantiationException, IllegalAccessException {
        if(!commands.containsKey(id)) {
            return null;
        }
        
        return commands.get(id).newInstance();
    }
    
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
