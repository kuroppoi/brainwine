package brainwine.gameserver.server;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import brainwine.gameserver.server.messages.BlockChangeMessage;
import brainwine.gameserver.server.messages.BlockMetaMessage;
import brainwine.gameserver.server.messages.BlocksMessage;
import brainwine.gameserver.server.messages.ChatMessage;
import brainwine.gameserver.server.messages.ConfigurationMessage;
import brainwine.gameserver.server.messages.DialogMessage;
import brainwine.gameserver.server.messages.EffectMessage;
import brainwine.gameserver.server.messages.EntityChangeMessage;
import brainwine.gameserver.server.messages.EntityItemUseMessage;
import brainwine.gameserver.server.messages.EntityPositionMessage;
import brainwine.gameserver.server.messages.EntityStatusMessage;
import brainwine.gameserver.server.messages.EventMessage;
import brainwine.gameserver.server.messages.HealthMessage;
import brainwine.gameserver.server.messages.HeartbeatMessage;
import brainwine.gameserver.server.messages.InventoryMessage;
import brainwine.gameserver.server.messages.KickMessage;
import brainwine.gameserver.server.messages.LightMessage;
import brainwine.gameserver.server.messages.NotificationMessage;
import brainwine.gameserver.server.messages.PlayerPositionMessage;
import brainwine.gameserver.server.messages.SkillMessage;
import brainwine.gameserver.server.messages.StatMessage;
import brainwine.gameserver.server.messages.TeleportMessage;
import brainwine.gameserver.server.messages.WardrobeMessage;
import brainwine.gameserver.server.messages.ZoneExploredMessage;
import brainwine.gameserver.server.messages.ZoneSearchMessage;
import brainwine.gameserver.server.messages.ZoneStatusMessage;
import brainwine.gameserver.server.requests.AuthenticateRequest;
import brainwine.gameserver.server.requests.BlockMineRequest;
import brainwine.gameserver.server.requests.BlockPlaceRequest;
import brainwine.gameserver.server.requests.BlockUseRequest;
import brainwine.gameserver.server.requests.BlocksIgnoreRequest;
import brainwine.gameserver.server.requests.BlocksRequest;
import brainwine.gameserver.server.requests.ChangeAppearanceRequest;
import brainwine.gameserver.server.requests.ChatRequest;
import brainwine.gameserver.server.requests.ConsoleRequest;
import brainwine.gameserver.server.requests.CraftRequest;
import brainwine.gameserver.server.requests.DialogRequest;
import brainwine.gameserver.server.requests.EntitiesRequest;
import brainwine.gameserver.server.requests.EventRequest;
import brainwine.gameserver.server.requests.HealthRequest;
import brainwine.gameserver.server.requests.HeartbeatRequest;
import brainwine.gameserver.server.requests.InventoryMoveRequest;
import brainwine.gameserver.server.requests.InventoryUseRequest;
import brainwine.gameserver.server.requests.MoveRequest;
import brainwine.gameserver.server.requests.RespawnRequest;
import brainwine.gameserver.server.requests.StatusRequest;
import brainwine.gameserver.server.requests.TransactionRequest;
import brainwine.gameserver.server.requests.ZoneChangeRequest;
import brainwine.gameserver.server.requests.ZoneSearchRequest;

public class NetworkRegistry {
    
    private static final Logger logger = LogManager.getLogger();
    private static final Map<Integer, Class<? extends Request>> requests = new HashMap<>();
    private static final Map<Class<? extends Message>, Integer> messageIds = new HashMap<>();
    private static boolean initialized = false;
    
    public static void init() {
        if(initialized) {
            logger.warn("init() called twice");
            return;
        }
        
        registerRequests();
        registerMessages();
        initialized = true;
    }
    
    private static void registerRequests() {
        logger.info("Registering requests ...");
        registerRequest(1, AuthenticateRequest.class);
        registerRequest(5, MoveRequest.class);
        registerRequest(10, InventoryUseRequest.class);
        registerRequest(11, BlockMineRequest.class);
        registerRequest(12, BlockPlaceRequest.class);
        registerRequest(13, ChatRequest.class);
        registerRequest(14, InventoryMoveRequest.class);
        registerRequest(16, BlocksRequest.class);
        registerRequest(18, HealthRequest.class);
        registerRequest(19, CraftRequest.class);
        registerRequest(21, BlockUseRequest.class);
        registerRequest(22, ChangeAppearanceRequest.class);
        registerRequest(23, ZoneSearchRequest.class);
        registerRequest(24, ZoneChangeRequest.class);
        registerRequest(25, BlocksIgnoreRequest.class);
        registerRequest(26, RespawnRequest.class);
        registerRequest(41, TransactionRequest.class);
        registerRequest(45, DialogRequest.class);
        registerRequest(47, ConsoleRequest.class);
        registerRequest(51, EntitiesRequest.class);
        registerRequest(54, StatusRequest.class);
        registerRequest(57, EventRequest.class);
        registerRequest(143, HeartbeatRequest.class);
    }
    
    private static void registerMessages() {
        logger.info("Registering messages ...");
        registerMessage(ConfigurationMessage.class, 2);
        registerMessage(BlocksMessage.class, 3);
        registerMessage(InventoryMessage.class, 4);
        registerMessage(PlayerPositionMessage.class, 5);
        registerMessage(EntityPositionMessage.class, 6);
        registerMessage(EntityStatusMessage.class, 7);
        registerMessage(EntityChangeMessage.class, 8);
        registerMessage(BlockChangeMessage.class, 9);
        registerMessage(EntityItemUseMessage.class, 10);
        registerMessage(ChatMessage.class, 13);
        registerMessage(LightMessage.class, 15);
        registerMessage(ZoneStatusMessage.class, 17);
        registerMessage(HealthMessage.class, 18);
        registerMessage(BlockMetaMessage.class, 20);
        registerMessage(ZoneSearchMessage.class, 23);
        registerMessage(EffectMessage.class, 30);
        registerMessage(NotificationMessage.class, 33);
        registerMessage(SkillMessage.class, 35);
        registerMessage(WardrobeMessage.class, 39);
        registerMessage(StatMessage.class, 44);
        registerMessage(DialogMessage.class, 45);
        registerMessage(TeleportMessage.class, 50);
        registerMessage(ZoneExploredMessage.class, 53);
        registerMessage(EventMessage.class, 57);
        registerMessage(HeartbeatMessage.class, 143);
        registerMessage(KickMessage.class, 255);
    }
    
    public static void registerRequest(int id, Class<? extends Request> type) {
        if(requests.containsKey(id)) {
            logger.warn("Attempted to register duplicate request {}", type.getTypeName());
            return;
        }
        
        requests.put(id, type);
    }
    
    public static Request instantiateRequest(int id) throws InstantiationException, IllegalAccessException {
        if(!requests.containsKey(id)) {
            return null;
        }
        
        return requests.get(id).newInstance();
    }
    
    public static void registerMessage(Class<? extends Message> type, int id) {
        if(messageIds.containsKey(type)) {
            logger.warn("Attempted to register duplicate message {}", type.getTypeName());
            return;
        }
        
        messageIds.put(type, id);
    }
    
    public static int getMessageId(Message message) {
        return messageIds.getOrDefault(message.getClass(), 0);
    }
}
