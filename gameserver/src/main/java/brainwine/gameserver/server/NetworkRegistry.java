package brainwine.gameserver.server;

import static brainwine.shared.LogMarkers.SERVER_MARKER;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;

import brainwine.gameserver.annotations.MessageInfo;
import brainwine.gameserver.annotations.RequestInfo;

@SuppressWarnings("unchecked")
public class NetworkRegistry {
    
    private static final Logger logger = LogManager.getLogger();
    private static final Map<Integer, Class<? extends Request>> requests = new HashMap<>();
    private static final Map<Class<? extends Message>, Integer> messageIds = new HashMap<>();
    private static boolean initialized = false;
    
    public static void init() {
        if(initialized) {
            logger.warn(SERVER_MARKER, "NetworkRegistry is already initialized - skipping!");
            return;
        }
        
        registerRequests();
        registerMessages();
        initialized = true;
    }
    
    private static void registerRequests() {
        logger.info(SERVER_MARKER, "Registering requests ...");
        Reflections reflections = new Reflections("brainwine.gameserver.server.requests");
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(RequestInfo.class);
        
        for(Class<?> clazz : classes) {
            if(!Request.class.isAssignableFrom(clazz)) {
                logger.warn(SERVER_MARKER, "Attempted to register non-request class {}", clazz.getSimpleName());
                continue;
            }
            
            RequestInfo info = clazz.getAnnotation(RequestInfo.class);
            registerRequest(info.id(), (Class<? extends Request>)clazz);
        }
    }
    
    private static void registerMessages() {
        logger.info(SERVER_MARKER, "Registering messages ...");
        Reflections reflections = new Reflections("brainwine.gameserver.server.messages");
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(MessageInfo.class);
        
        for(Class<?> clazz : classes) {
            if(!Message.class.isAssignableFrom(clazz)) {
                logger.warn(SERVER_MARKER, "Attempted to register non-message class {}", clazz.getSimpleName());
                continue;
            }
            
            MessageInfo info = clazz.getAnnotation(MessageInfo.class);
            registerMessage((Class<? extends Message>)clazz, info.id());
        }
    }
    
    public static void registerRequest(int id, Class<? extends Request> type) {
        if(!type.isAnnotationPresent(RequestInfo.class)) {
            logger.warn(SERVER_MARKER, "RequestInfo annotation not present for class {}", type.getTypeName());
            return;
        }
        
        if(requests.containsKey(id)) {
            logger.warn(SERVER_MARKER, "Attempted to register duplicate request {}", type.getTypeName());
            return;
        }
        
        requests.put(id, type);
    }
    
    public static Class<? extends Request> getRequestClass(int id){
        return requests.get(id);
    }
    
    public static void registerMessage(Class<? extends Message> type, int id) {
        if(!type.isAnnotationPresent(MessageInfo.class)) {
            logger.warn(SERVER_MARKER, "MessageInfo annotation not present for class {}", type.getTypeName());
            return;
        }
        
        if(messageIds.containsKey(type)) {
            logger.warn(SERVER_MARKER, "Attempted to register duplicate message {}", type.getTypeName());
            return;
        }
        
        messageIds.put(type, id);
    }
    
    public static int getMessageId(Message message) {
        return messageIds.getOrDefault(message.getClass(), 0);
    }
}
