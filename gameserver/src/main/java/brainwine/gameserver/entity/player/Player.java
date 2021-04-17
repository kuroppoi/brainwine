package brainwine.gameserver.entity.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import brainwine.gameserver.GameConfiguration;
import brainwine.gameserver.GameServer;
import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.entity.EntityStatus;
import brainwine.gameserver.entity.EntityType;
import brainwine.gameserver.entity.FacingDirection;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemRegistry;
import brainwine.gameserver.server.Message;
import brainwine.gameserver.server.commands.BlocksIgnoreCommand;
import brainwine.gameserver.server.commands.BlocksRequestCommand;
import brainwine.gameserver.server.messages.BlockMetaMessage;
import brainwine.gameserver.server.messages.ConfigurationMessage;
import brainwine.gameserver.server.messages.EffectMessage;
import brainwine.gameserver.server.messages.EntityItemUseMessage;
import brainwine.gameserver.server.messages.EntityPositionMessage;
import brainwine.gameserver.server.messages.EntityStatusMessage;
import brainwine.gameserver.server.messages.EventMessage;
import brainwine.gameserver.server.messages.HealthMessage;
import brainwine.gameserver.server.messages.InventoryMessage;
import brainwine.gameserver.server.messages.KickMessage;
import brainwine.gameserver.server.messages.NotificationMessage;
import brainwine.gameserver.server.messages.PlayerPositionMessage;
import brainwine.gameserver.server.messages.SkillMessage;
import brainwine.gameserver.server.messages.TeleportMessage;
import brainwine.gameserver.server.messages.WardrobeMessage;
import brainwine.gameserver.server.messages.ZoneStatusMessage;
import brainwine.gameserver.server.pipeline.Connection;
import brainwine.gameserver.util.MathUtils;
import brainwine.gameserver.zone.Chunk;
import brainwine.gameserver.zone.MetaBlock;
import brainwine.gameserver.zone.Zone;
import brainwine.gameserver.zone.ZoneManager;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Player extends Entity {
    
    public static final int MAX_SKILL_LEVEL = 15;
    public static final int MAX_NATURAL_SKILL_LEVEL = 10;
    public static final int MAX_SPEED_X = 12;
    public static final int MAX_SPEED_Y = 25;
    public static final int HEARTBEAT_TIMEOUT = 30000;
    private final String documentId;
    private String password;
    private String authToken;
    private boolean admin;
    private int karma;
    private final Map<AvatarPart, Object> appearance = new HashMap<>();
    private final Map<String, Object> settings = new HashMap<>();
    private final Map<Skill, Integer> skills = new HashMap<>();
    private final Map<Integer, Long> activeChunks = new HashMap<>();
    private final Inventory inventory = new Inventory();
    private Item heldItem = Item.AIR; // TODO send on entity add
    private int teleportX;
    private int teleportY;
    private long lastHeartbeat;
    private Connection connection;
    
    public Player(String documentId, String name, Zone zone) {
        super(zone);
        
        for(Skill skill : Skill.values()) {
            skills.put(skill, 10);
        }
        
        this.documentId = documentId;
        this.name = name;
        appearance.put(AvatarPart.SKIN_COLOR, "fcebd0");
        appearance.put(AvatarPart.TOPS_OVERLAY, 1206);
        appearance.put(AvatarPart.LEGS_OVERLAY, 1207);
        appearance.put(AvatarPart.FACIAL_GEAR, 1208);
        settings.put("hotbar_presets", new ArrayList<>());
        Map<String, Object> test = new HashMap<>();
        test.put("fg", true);
        test.put("to", true);
        test.put("lo", true);
        settings.put("appearance", test);
    }
    
    @JsonCreator
    private static Player create(@JacksonInject("documentId") String documentId, @JsonProperty("name") String name, @JsonProperty("current_zone") String currentZone) {
        ZoneManager zoneManager = GameServer.getInstance().getZoneManager();
        Zone zone = zoneManager.getZone(currentZone);
        
        if(zone == null) {
            zone = zoneManager.getRandomZone();
        }
        
        return new Player(documentId, name, zone);
    }
    
    @Override
    public EntityType getType() {
        return EntityType.PLAYER;
    }
    
    @Override
    public void tick() {
        super.tick();
        
        if(lastHeartbeat != 0) {
            if(System.currentTimeMillis() - lastHeartbeat >= HEARTBEAT_TIMEOUT) {
                kick("Connection timed out.");
            }
        }
    }
    
    @Override
    public void setHealth(float health) {
        super.setHealth(health);
        sendMessage(new HealthMessage(health));
    }
    
    /**
     * Called by {@link Zone#addEntity(Entity)} when the player is added to it.
     */
    public void onZoneChanged() {
        // TODO handle spawns better
        MetaBlock spawn = zone.getRandomZoneTeleporter();
        
        if(spawn == null) {
            x = zone.getWidth() / 2;
            y = 2;
        } else {
            x = spawn.getX() + 1;
            y = spawn.getY();
        }
        
        sendMessage(new ConfigurationMessage(id, getClientConfig(), GameConfiguration.getClientConfig(), zone.getClientConfig()));
        
        for(MetaBlock metaBlock : zone.getGlobalMetaBlocks()) {
            sendMessage(new BlockMetaMessage(metaBlock));
        }
        
        sendMessage(new ZoneStatusMessage(zone.getStatusConfig()));
        sendMessage(new ZoneStatusMessage(zone.getStatusConfig()));
        sendMessage(new PlayerPositionMessage((int)x, (int)y));
        sendMessage(new HealthMessage(health));
        sendMessage(new InventoryMessage(inventory));
        
        List<Integer> wardrobe = new ArrayList<>();
        
        for(Item item : ItemRegistry.getItems()) {
            if(item.isClothing()) {
                wardrobe.add(item.getId());
            }
        }
        
        int[] a = new int[wardrobe.size()];
        
        for(int i = 0; i < a.length; i++)
            a[i] = wardrobe.get(i);
        
        sendMessage(new WardrobeMessage(a));
        
        for(Skill skill : skills.keySet()) {
            sendMessage(new SkillMessage(skill, skills.get(skill)));
        }
        
        for(Player peer : zone.getPlayers()) {
            sendMessage(new EntityStatusMessage(peer, EntityStatus.ENTERING));
            sendMessage(new EntityPositionMessage(peer.getId(), peer.getX(), peer.getY(), 0, 0, FacingDirection.EAST, 0, 0, 0));
            sendMessage(new EntityItemUseMessage(peer.getId(), 0, peer.getHeldItem(), 0));
        }
        
        notify("Welcome to " + zone.getName(), 6);
        sendMessage(new EventMessage("zoneEntered", null));
        sendMessage(new EffectMessage(x, y, "spawn", 20));
        checkRegistration();
    }
    
    /**
     * Called from {@link Connection} when the channel becomes inactive.
     */
    public void onDisconnect() {
        lastHeartbeat = 0;
        
        if(zone != null) {
            zone.removePlayer(this);
            activeChunks.clear();
        }
    }
    
    /**
     * Sends a message to the player if they are online.
     * 
     * @param message The message to send.
     */
    public void sendMessage(Message message) {
        if(isOnline()) {
            connection.sendMessage(message);
        }
    }
    
    public void sendDelayedMessage(Message message) {
        sendDelayedMessage(message, 250);
    }
    
    
    /**
     * Sends a message to the player after the specified delay.
     * 
     * @param message The message to send.
     * @param delay The delay in milliseconds.
     */
    public void sendDelayedMessage(Message message, int delay) {
        if(isOnline()) {
            connection.sendDelayedMessage(message, delay);
        }
    }
    
    /**
     * Sends a message to all players (except for this player) in the current zone.
     * 
     * @param message The message to send.
     */
    public void sendMessageToPeers(Message message) {
        for(Player player : zone.getPlayers()) {
            if(player != this) {
                player.sendMessage(message);
            }
        }
    }
    
    public void changeZone(Zone zone) {
        this.zone.removePlayer(this);
        this.zone = zone;
        kick("Teleporting...", true);
    }
    
    public void checkRegistration() {
        if(password == null) {
            sendMessage(new EventMessage("playerRegistered", false));
            sendMessage(new EventMessage("playerLockDidChange", "Before you can log out, you must register your current account. Log in and type /register in the console to register."));
            sendMessage(new EventMessage("playerNeedsRegistration", true));
        } else {
            sendMessage(new EventMessage("playerRegistered", true));
            sendMessage(new EventMessage("playerLockDidChange", null));
            sendMessage(new EventMessage("playerNeedsRegistration", false));
        }
    }
    
    public void heartbeat() {
        lastHeartbeat = System.currentTimeMillis();
    }
    
    /**
     * Rubberbands this player back to its last valid position.
     */
    public void rubberband() {
        sendMessage(new PlayerPositionMessage((int)x, (int)y + 1));
    }
    
    /**
     * Teleports the player to the specified position.
     * 
     * @param x The x coordinate of the position.
     * @param y The y coordinate of the position.
     */
    public void teleport(int x, int y) {
        setPosition(x, y);
        teleportX = x;
        teleportY = y;
        sendMessage(new TeleportMessage(x, y));
        sendMessage(new PlayerPositionMessage(x, y));
        zone.sendMessage(new EffectMessage(x, y, "teleport", 20));
    }
    
    public int getTeleportX() {
        return teleportX;
    }
    
    public int getTeleportY() {
        return teleportY;
    }
    
    /**
     * @see #kick(String, boolean)
     * @param reason The reason why this player is kicked.
     */
    public void kick(String reason) {
        kick(reason, false);
    }
    
    /**
     * Kicks this player for the specified reason.
     * 
     * @param reason The reason why this player is kicked.
     * @param shouldReconnect If true, the player will automatically reconnect.
     */
    public void kick(String reason, boolean shouldReconnect) {
        sendMessage(new KickMessage(reason, shouldReconnect));
    }
    
    public void notify(String text, int type) {
        sendMessage(new NotificationMessage(text, type));
    }
    
    public void alert(String text) {
        notify(text, 1);
    }
    
    public void setHeldItem(Item item) {
        heldItem = item;
    }
    
    public Item getHeldItem() {
        return heldItem;
    }
    
    public double getMiningRange() {
        return MathUtils.lerp(3.0, 5.0, (double)getSkillLevel(Skill.MINING) / MAX_SKILL_LEVEL);
    }
    
    public double getPlacementRange() {
        return Math.ceil(MathUtils.lerp(5.0, 13.0, (double)getSkillLevel(Skill.BUILDING) / MAX_SKILL_LEVEL));
    }
    
    public String getDocumentId() {
        return documentId;
    }
    
    protected void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
    
    @JsonProperty("token_hash")
    protected String getAuthToken() {
        return authToken;
    }
    
    protected void setPassword(String password) {
        this.password = password;
    }
    
    @JsonProperty("password_hash")
    protected String getPassword() {
        return password;
    }
    
    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
    
    @JsonProperty("admin")
    public boolean isAdmin() {
        return admin;
    }
    
    public void setKarma(int karma) {
        this.karma = karma;
    }
    
    public int getKarma() {
        return karma;
    }
    
    public KarmaLevel getKarmaLevel() {
        for(KarmaLevel level : KarmaLevel.values()) {
            if(karma <= level.getKarma()) {
                return level;
            }
        }
        
        return KarmaLevel.POOR;
    }
    
    public void increaseSkillLevel(Skill skill) {
        setSkillLevel(skill, getSkillLevel(skill) + 1);
    }
    
    public void setSkillLevel(Skill skill, int level) {
        skills.put(skill, level);
        sendMessage(new SkillMessage(skill, level));
    }
    
    public int getSkillLevel(Skill skill) {
        return MathUtils.clamp(skills.getOrDefault(skill, 1), 1, MAX_NATURAL_SKILL_LEVEL);
    }
    
    public Inventory getInventory() {
        return inventory;
    }
    
    public void addActiveChunk(int index) {
        activeChunks.put(index, System.currentTimeMillis());
    }
    
    /**
     * Iterates through all active chunks of this player and removes chunks that have been
     * active for 5 or more seconds, and are out of range of the player.
     * Its behavior is (mostly) the same as the clients, but instead of relying on {@link BlocksIgnoreCommand},
     * this function gets called each time {@link BlocksRequestCommand} is received.
     */
    public void removeOutOfRangeChunks() {
        Iterator<Entry<Integer, Long>> iterator = activeChunks.entrySet().iterator();
        
        while(iterator.hasNext()) {
            Entry<Integer, Long> entry = iterator.next();
            long activeSince = entry.getValue();
            
            // If chunk has been active for more than 5 seconds
            if(System.currentTimeMillis() >= activeSince + 5000) {
                Chunk chunk = zone.getChunk(entry.getKey());
                
                if(!MathUtils.inRange(x, y, chunk.getX(), chunk.getY(), chunk.getWidth() * 5)) {
                    iterator.remove();
                }
            }
        }
    }
    
    public boolean isChunkActive(Chunk chunk) {
        return isChunkActive(chunk.getX(), chunk.getY());
    }
    
    public boolean isChunkActive(int x, int y) {
        return isChunkActive(zone.getChunkIndex(x, y));
    }
    
    public boolean isChunkActive(int index) {
        return activeChunks.containsKey(index);
    }
    
    public void setConnection(Connection connection) {
        if(isOnline()) {
            kick("You logged in from another location.");
        }
        
        this.connection = connection;
        connection.setPlayer(this);
    }
    
    public boolean isOnline() {
        return connection != null && connection.isOpen();
    }
    
    @JsonValue
    public Map<String, Object> getJsonValue() {
        Map<String, Object> map = new HashMap<>();
        map.put("password_hash", password);
        map.put("token_hash", authToken);
        map.put("name", name);
        map.put("admin", admin);
        map.put("karma", karma);
        map.put("current_zone", zone.getDocumentId());
        return map;
    }
    
    /**
     * @return A {@link Map} containing all the data necessary for use in {@link ConfigurationMessage}.
     */
    public Map<String, Object> getClientConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("id", documentId);
        config.put("name", name);
        config.put("admin", admin);
        config.put("karma", getKarmaLevel());
        config.put("appearance", appearance);
        config.put("settings", settings);
        return config;
    }
}
