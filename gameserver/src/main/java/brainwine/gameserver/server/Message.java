package brainwine.gameserver.server;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.msgpack.packer.Packer;

import brainwine.gameserver.reflections.ReflectionsHelper;

@SuppressWarnings("unchecked")
public abstract class Message {
    
    private static final Logger logger = LogManager.getLogger();
    private static final Map<Class<? extends Message>, Integer> messageIds = new HashMap<>();
    private static final Map<Class<? extends Message>, MessageOptions> messageOptions = new HashMap<>();

    static {
        Set<Class<?>> classes = ReflectionsHelper.getTypesAnnotatedWith(RegisterMessage.class);
        
        for(Class<?> clazz : classes) {
            if(!Message.class.isAssignableFrom(clazz)) {
                logger.warn("Attempted to register non-message {}", clazz.getName());
                continue;
            }
            
            RegisterMessage info = clazz.getAnnotation(RegisterMessage.class);
            registerMessage((Class<? extends Message>)clazz, info.id(), new MessageOptions(info));
        }
    }
    
    public static void registerMessage(Class<? extends Message> type, int id) {
        registerMessage(type, id, new MessageOptions(false, false, false, false));
    }
    
    public static void registerMessage(Class<? extends Message> type, int id, MessageOptions options) {
        if(messageIds.containsKey(type)) {
            logger.warn("Attempted to register duplicate message {}", type.getTypeName());
            return;
        }
        
        messageIds.put(type, id);
        messageOptions.put(type, options);
    }
    
    public static int getMessageId(Message message) {
        return messageIds.getOrDefault(message.getClass(), 0);
    }
    
    public static MessageOptions getMessageOptions(Message message) {
        return messageOptions.get(message.getClass());
    }
    
    public void pack(Packer packer) throws IOException, IllegalArgumentException, IllegalAccessException {
        MessageOptions options = getMessageOptions(this);
        Field[] fields = getClass().getFields();
        
        if(options.isPrepacked()) {
            if(fields.length != 1 || !Collection.class.isAssignableFrom(fields[0].getType())) {
                throw new IOException("Prepacked messages may only contain 1 field that must be a Collection.");
            }
            
            packer.write(fields[0].get(this));
        } else {
            List<Object> data = new ArrayList<>();
            
            for(Field field : fields) {
                data.add(field.get(this));
            }
            
            if(options.isCollection()) {
                packer.write(Arrays.asList(data));
            } else {
                packer.write(data);
            }
        }
    }
}
