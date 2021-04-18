package brainwine.gameserver.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import brainwine.gameserver.reflections.ReflectionsHelper;

public class NetworkRegistry {
    
    private static final Logger logger = LogManager.getLogger();
    private static final Map<Integer, Class<? extends Command>> commands = new HashMap<>();
    private static final Map<Class<? extends Message>, Integer> messageIds = new HashMap<>();
    private static final Map<Class<? extends Message>, MessageOptions> messageOptions = new HashMap<>();
    private static boolean initialized = false;
    
    public static void init() {
        if(initialized) {
            logger.warn("init() called twice");
            return;
        }
        
        initialized = true;
        registerCommands();
        registerMessages();
    }
    
    @SuppressWarnings("unchecked")
    private static void registerCommands() {
        logger.info("Registering commands ...");
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
    
    @SuppressWarnings("unchecked")
    private static void registerMessages() {
        logger.info("Registering messages ...");
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
}
