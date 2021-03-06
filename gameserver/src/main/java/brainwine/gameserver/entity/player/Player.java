package brainwine.gameserver.entity.player;

import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import brainwine.gameserver.GameConfiguration;
import brainwine.gameserver.GameServer;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.dialog.Dialog;
import brainwine.gameserver.dialog.DialogListItem;
import brainwine.gameserver.dialog.DialogSection;
import brainwine.gameserver.dialog.DialogType;
import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.entity.EntityStatus;
import brainwine.gameserver.entity.FacingDirection;
import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.item.Action;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemRegistry;
import brainwine.gameserver.item.ItemUseType;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.loot.Loot;
import brainwine.gameserver.server.Message;
import brainwine.gameserver.server.messages.BlockMetaMessage;
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
import brainwine.gameserver.server.messages.NotificationMessage;
import brainwine.gameserver.server.messages.PlayerPositionMessage;
import brainwine.gameserver.server.messages.SkillMessage;
import brainwine.gameserver.server.messages.StatMessage;
import brainwine.gameserver.server.messages.TeleportMessage;
import brainwine.gameserver.server.messages.WardrobeMessage;
import brainwine.gameserver.server.messages.ZoneStatusMessage;
import brainwine.gameserver.server.pipeline.Connection;
import brainwine.gameserver.util.MapHelper;
import brainwine.gameserver.util.MathUtils;
import brainwine.gameserver.util.Vector2i;
import brainwine.gameserver.zone.Chunk;
import brainwine.gameserver.zone.MetaBlock;
import brainwine.gameserver.zone.Zone;

@JsonIncludeProperties({"name", "email", "password_hash", "auth_tokens", "admin", "karma", "crowns", "inventory", "equipped_clothing", "equipped_colors", "current_zone"})
public class Player extends Entity implements CommandExecutor {
    
    public static final int MAX_SKILL_LEVEL = 15;
    public static final int MAX_NATURAL_SKILL_LEVEL = 10;
    public static final int MAX_SPEED_X = 12;
    public static final int MAX_SPEED_Y = 25;
    public static final int HEARTBEAT_TIMEOUT = 30000;
    public static final int MAX_AUTH_TOKENS = 3;
    public static final int TRACKED_ENTITY_UPDATE_INTERVAL = 100;
    public static final int REGEN_NO_DAMAGE_TIME = 10000;
    public static final float ENTITY_VISIBILITY_RANGE = 40;
    public static final float BASE_REGEN_AMOUNT = 0.1F;
    private static int dialogDiscriminator;
    
    @JacksonInject("documentId")
    private final String documentId;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("password_hash")
    private String password;
    
    @JsonProperty("auth_tokens")
    private final List<String> authTokens = new ArrayList<>();
    
    @JsonProperty("admin")
    private boolean admin;
    
    @JsonProperty("karma")
    private int karma;
    
    @JsonProperty("crowns")
    private int crowns;
    
    @JsonManagedReference
    @JsonProperty("inventory")
    private final Inventory inventory = new Inventory(this);
    
    @JsonProperty("equipped_clothing")
    private final Map<ClothingSlot, Item> clothing = new HashMap<>();
    
    @JsonProperty("equipped_colors")
    private final Map<ColorSlot, String> colors = new HashMap<>();
    
    private final Set<Item> wardrobe = new HashSet<>();
    private final Map<String, Object> settings = new HashMap<>();
    private final Map<Skill, Integer> skills = new HashMap<>();
    private final Set<Integer> activeChunks = new HashSet<>();
    private final Map<Integer, Consumer<Object[]>> dialogs = new HashMap<>();
    private final List<Entity> trackedEntities = new ArrayList<>();
    private String clientVersion;
    private Placement lastPlacement;
    private Item heldItem = Item.AIR;
    private Vector2i spawnPoint = new Vector2i(0, 0);
    private int teleportX;
    private int teleportY;
    private long lastHeartbeat;
    private long lastTrackedEntityUpdate;
    private Connection connection;
    
    @ConstructorProperties({"documentId", "name", "current_zone"})
    public Player(@JacksonInject("documentId") String documentId, String name, Zone zone) {
        super(zone);
        
        for(Skill skill : Skill.values()) {
            skills.put(skill, 15);
        }
        
        for(Item item : ItemRegistry.getItems()) {
            if(item.isClothing()) {
                wardrobe.add(item);
            }
        }
        
        this.documentId = documentId;
        this.name = name;
        settings.put("hotbar_presets", new ArrayList<>());
        Map<String, Object> test = new HashMap<>();
        test.put("fg", true);
        test.put("to", true);
        test.put("lo", true);
        settings.put("appearance", test);
    }
    
    @Override
    public void tick(float deltaTime) {
        long now = System.currentTimeMillis();
        
        // Check timeout
        if(lastHeartbeat != 0) {
            if(System.currentTimeMillis() - lastHeartbeat >= HEARTBEAT_TIMEOUT) {
                kick("Connection timed out.");
            }
        }
        
        // Regenerate health out of combat
        if(!isDead() && now >= lastDamagedAt + REGEN_NO_DAMAGE_TIME) {
            heal(BASE_REGEN_AMOUNT * deltaTime);
        }
        
        // Update tracked entities
        if(now - lastTrackedEntityUpdate >= TRACKED_ENTITY_UPDATE_INTERVAL) {
            updateTrackedEntities();
            
            for(Entity entity : trackedEntities) {
                sendMessage(new EntityPositionMessage(entity));
            }
            
            lastTrackedEntityUpdate = now;
        }
    }
    
    @Override
    public void die(Player killer) {        
        sendMessageToPeers(new EntityStatusMessage(this, EntityStatus.DEAD)); // TODO killer id
    }
    
    @Override
    public void notify(Object message, NotificationType type) {
        if(type == NotificationType.SYSTEM && !isV3()) {
            sendMessage(new NotificationMessage(message, NotificationType.STANDARD));
            return;
        }
        
        sendMessage(new NotificationMessage(message, type));
    }
    
    @Override
    public boolean isAdmin() {
        return admin;
    }
    
    @Override
    public float getMaxHealth() {
        return 10; // TODO
    }
    
    @Override
    public void setHealth(float health) {
        super.setHealth(health);
        sendMessage(new HealthMessage(health));
    }
    
    /**
     * @return A {@link Map} containing all the data necessary for use in {@link EntityStatusMessage}.
     */
    @Override
    public Map<String, Object> getStatusConfig() {
        Map<String, Object> config = super.getStatusConfig();
        config.put("id", documentId);
        config.putAll(getAppearanceConfig());
        return config;
    }
    
    /**
     * Called by {@link Zone#addEntity(Entity)} when the player is added to it.
     */
    public void onZoneChanged() {
        // TODO handle spawns better
        MetaBlock spawn = zone.getRandomSpawnBlock();
        
        if(spawn == null) {
            x = zone.getWidth() / 2;
            y = 2;
        } else {
            x = spawn.getX() + 1;
            y = spawn.getY();
        }
        
        spawnPoint.setX((int)x);
        spawnPoint.setY((int)y);
        sendMessage(new ConfigurationMessage(id, getClientConfig(), GameConfiguration.getClientConfig(this), zone.getClientConfig(this)));
        sendMessage(new ZoneStatusMessage(zone.getStatusConfig()));
        sendMessage(new ZoneStatusMessage(zone.getStatusConfig()));
        sendMessage(new PlayerPositionMessage((int)x, (int)y));
        sendMessage(new HealthMessage(health));
        
        if(inventory.isEmpty()) {
            Item pickaxe = ItemRegistry.getItem("tools/pickaxe");
            Item pistol = ItemRegistry.getItem("tools/pistol");
            Item jetpack = ItemRegistry.getItem("accessories/jetpack");
            inventory.addItem(pickaxe);
            inventory.addItem(pistol);
            inventory.addItem(jetpack);
            inventory.moveItemToContainer(pickaxe, ContainerType.HOTBAR, 0);
            inventory.moveItemToContainer(pistol, ContainerType.HOTBAR, 1);
            inventory.moveItemToContainer(jetpack, ContainerType.ACCESSORIES, 0);
        }
        
        sendMessage(new InventoryMessage(inventory));
        sendMessage(new WardrobeMessage(wardrobe));
        
        for(MetaBlock metaBlock : zone.getGlobalMetaBlocks()) {
            sendMessage(new BlockMetaMessage(metaBlock));
        }
        
        for(Skill skill : skills.keySet()) {
            sendMessage(new SkillMessage(skill, skills.get(skill)));
        }
        
        for(Player peer : zone.getPlayers()) {
            sendMessage(new EntityStatusMessage(peer, EntityStatus.ENTERING));
            sendMessage(new EntityPositionMessage(peer.getId(), peer.getX(), peer.getY(), 0, 0, FacingDirection.EAST, 0, 0, 0));
            sendMessage(new EntityItemUseMessage(peer.getId(), 0, peer.getHeldItem(), 0));
        }
        
        if(isV3()) {
            sendMessage(new EventMessage("zoneEntered", null));
            notify("Welcome to " + zone.getName(), NotificationType.LARGE);
        } else {
            notify("Welcome to " + zone.getName(), NotificationType.WELCOME);
        }
        
        checkRegistration();
    }
    
    /**
     * Called from {@link Connection} when the channel becomes inactive.
     */
    public void onDisconnect() {
        lastHeartbeat = 0;
        lastPlacement = null;
        clientVersion = null;
        
        if(zone != null) {
            zone.removeEntity(this);
        }
        
        dialogs.clear();
        activeChunks.clear();
        
        for(Entity entity : trackedEntities) {
            entity.removeTracker(this);
        }
        
        trackedEntities.clear();
        GameServer.getInstance().getPlayerManager().onPlayerDisconnect(this);
        connection.setPlayer(null);
        connection = null;
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
        this.zone.removeEntity(this);
        this.zone = zone;
        sendMessage(new EventMessage("playerWillChangeZone", null));
        kick("Teleporting...", true);
    }
    
    public void showDialog(Dialog dialog) {
        showDialog(dialog, null);
    }
    
    public void showDialog(Dialog dialog, Consumer<Object[]> handler) {
        int id = handler == null ? 0 : ++dialogDiscriminator;
        
        if(id != 0) {
            dialogs.put(id, handler);
        }
        
        sendMessage(new DialogMessage(id, dialog));
    }
    
    public void handleDialogInput(int id, Object[] input) {
        if(id == 0 || (input.length == 1 && input[0].equals("cancel"))) {
            return;
        }
        
        Consumer<Object[]> handler = dialogs.remove(id);
        
        if(handler == null) {
            alert("Sorry, the request has expired.");
        } else {
            // TODO since we're dealing with user input, should we just try-catch this?
            handler.accept(input);
        }
    }
    
    public void checkRegistration() {
        if(!isRegistered()) {
            sendMessage(new EventMessage("playerRegistered", false));
            sendMessage(new EventMessage("playerLockDidChange", "Before you can log out, you must register your current account. Log in and type /register in the console to register."));
            sendMessage(new EventMessage("playerNeedsRegistration", true));
        } else {
            sendMessage(new EventMessage("playerRegistered", true));
            sendMessage(new EventMessage("playerLockDidChange", null));
            sendMessage(new EventMessage("playerNeedsRegistration", false));
        }
    }
    
    public boolean isRegistered() {
        return password != null && email != null;
    }
    
    public void heartbeat() {
        lastHeartbeat = System.currentTimeMillis();
        sendMessage(new HeartbeatMessage((int)(System.currentTimeMillis() / 1000L)));
    }
    
    public void setClientVersion(String version) {
        clientVersion = version;
    }
    
    public String getClientVersion() {
        return clientVersion;
    }
    
    public boolean isV3() {
        return clientVersion != null && clientVersion.startsWith("3");
    }
    
    /**
     * Rubberbands this player back to its last valid position.
     */
    public void rubberband() {
        sendMessage(new PlayerPositionMessage((int)x, (int)y + 1));
    }
    
    public void respawn() {
        if(isDead()) {
            setHealth(getMaxHealth());
        }
        
        int x = spawnPoint.getX();
        int y = spawnPoint.getY();
        sendMessage(new PlayerPositionMessage(x, y));
        sendMessageToPeers(new EntityStatusMessage(this, EntityStatus.REVIVED));
        zone.sendMessage(new EffectMessage(x, y, "spawn", 20));
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
        if(isOnline()) {
            connection.kick(reason, shouldReconnect);
        }
    }
    
    public void notifyPeers(Object message, NotificationType type) {
        sendMessageToPeers(new NotificationMessage(message, type));
    }
    
    public void alert(String text) {
        notify(text, NotificationType.ALERT);
    }
    
    public void setHeldItem(Item item) {
        heldItem = item;
    }
    
    public Item getHeldItem() {
        return heldItem;
    }
    
    public void trackPlacement(int x, int y, Item item) {
        if(item.getUses().isEmpty() || !zone.areCoordinatesInBounds(x, y)) {
            return;
        }
        
        boolean linked = false;
        
        if(lastPlacement != null) {
            if(item.hasUse(ItemUseType.SWITCHED) && !item.hasUse(ItemUseType.SWITCH)) {
                linked = tryLinkSwitchedItem(x, y, item);
            }
        }
        
        if(!linked) {
            lastPlacement = new Placement(x, y, item);
        }
    }
    
    private boolean tryLinkSwitchedItem(int x, int y, Item item) {
        int pX = lastPlacement.getX();
        int pY = lastPlacement.getY();
        Item pItem = lastPlacement.getItem();
        boolean linked = false;
        
        if(pItem.hasUse(ItemUseType.SWITCH)) {
            MetaBlock metaBlock = zone.getMetaBlock(pX, pY);
            Map<String, Object> metadata = metaBlock == null ? null : metaBlock.getMetadata();
            
            if(metadata != null) {
                MapHelper.appendList(metadata, ">", Arrays.asList(x, y));
                
                if(!(item.getUse(ItemUseType.SWITCHED) instanceof String)) {
                    int mod = zone.getBlock(pX, pY).getFrontMod();
                    zone.updateBlock(x, y, Layer.FRONT, item, mod, null, null);
                }
                
                linked = true;
                
                if(!pItem.hasUse(ItemUseType.MULTI) || MapHelper.getList(metadata, ">", Collections.emptyList()).size() >= 20) {
                    lastPlacement = null;
                }
            }
        }
        
        return linked;
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
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    protected String getPassword() {
        return password;
    }
    
    protected void clearAuthTokens() {
        authTokens.clear();
    }
    
    protected void clearOldestAuthTokens() {
        while(authTokens.size() > MAX_AUTH_TOKENS) {
            authTokens.remove(0);
        }
    }
    
    protected void addAuthToken(String authToken) {
        authTokens.add(authToken);
    }
    
    protected List<String> getAuthTokens() {
        return authTokens;
    }
    
    public void setAdmin(boolean admin) {
        this.admin = admin;
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
    
    public boolean hasEnoughCrowns(int crowns) {
        return this.crowns >= crowns;
    }
    
    public void addCrowns(int crowns) {
        setCrowns(this.crowns + crowns);
    }
    
    public void removeCrowns(int crowns) {
        setCrowns(this.crowns - crowns);
    }
    
    public void setCrowns(int crowns) {
        this.crowns = crowns;
        sendMessage(new StatMessage("crowns", crowns));
    }
    
    public int getCrowns() {
        return crowns;
    }
    
    public void setClothing(ClothingSlot slot, Item item) {
        if(!item.isClothing()) {
            return;
        }
        
        clothing.put(slot, item);
        zone.sendMessage(new EntityChangeMessage(id, getAppearanceConfig()));
    }
    
    public void setColor(ColorSlot slot, String hex) {
        // TODO check if the string is actually a valid hex color
        colors.put(slot, hex);
        zone.sendMessage(new EntityChangeMessage(id, getAppearanceConfig()));
    }
    
    public boolean hasClothing(Item item) {
        if(!item.isClothing()) {
            return false;
        }
        
        return item.isBase() || wardrobe.contains(item);
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
    
    public void consume(Item item) {
        Action action = item.getAction();
        
        // TODO some kind of abstraction for things like this would be pretty cool
        switch(action) {
            case HEAL: heal(item.getPower()); break;
            default: break;
        }
        
        // (Temporary?) measure to prevent consuming unimplemented consumables
        if(action != Action.NONE) {
            inventory.removeItem(item);
        }
    }
    
    public void awardLoot(Loot loot) {
        awardLoot(loot, DialogType.LOOT);
    }
    
    public void awardLoot(Loot loot, DialogType dialogType) {
        Dialog dialog = new Dialog();
        DialogSection section = new DialogSection();
        dialog.addSection(section);
        
        loot.getItems().forEach((item, quantity) -> {
            inventory.addItem(item, quantity);
            section.addItem(new DialogListItem()
                    .setItem(item.getId())
                    .setText(String.format("%s x %s", item.getTitle(), quantity)));
        });
        
        int crowns = loot.getCrowns();
        boolean v3 = isV3();
        
        if(crowns > 0) {
            addCrowns(crowns);
            
            if(v3) {
                section.setText(String.format("<color=#ffd95f>%s shiny crowns!</color>", crowns));
            } else {
                section.setText(String.format("%s shiny crowns!", crowns));
                section.setTextColor("ffd95f");
            }
        }
        
        if(v3) {
            dialog.setTitle("You found:");
            showDialog(dialog.setType(dialogType));
        } else {
            section.setTitle("You found:");
            notify(dialog, NotificationType.REWARD);
            
            // TODO Oh great, apparently the original loot sound (sfx-flourish-2) was downloaded from an external
            // source (defined by the API) on game startup along with a *lot* of other sound effects that are 
            // currently not working on v2 clients.
            // What do we do; rip all missing sound effects from the Unity version and put them in v2 game files,
            // or add a resource streaming feature to the API? Maybe both?
            sendMessage(new EffectMessage(x, y, "chime", 1));
        }
    }
    
    public Inventory getInventory() {
        return inventory;
    }
    
    public void addActiveChunk(int index) {
        activeChunks.add(index);
    }
    
    public void removeActiveChunk(int index) {
        activeChunks.remove(index);
    }
    
    public boolean isChunkActive(Chunk chunk) {
        return isChunkActive(chunk.getX(), chunk.getY());
    }
    
    public boolean isChunkActive(int x, int y) {
        return isChunkActive(zone.getChunkIndex(x, y));
    }
    
    public boolean isChunkActive(int index) {
        return activeChunks.contains(index);
    }
    
    public int getActiveChunkCount() {
        return activeChunks.size();
    }
    
    private void updateTrackedEntities() {
        List<Entity> entitiesInRange = zone.getEntitiesInRange(x, y, ENTITY_VISIBILITY_RANGE);
        entitiesInRange.remove(this);
        List<Entity> enteredEntities = entitiesInRange.stream().filter(entity -> !trackedEntities.contains(entity))
                .collect(Collectors.toList());
        List<Entity> departedEntities = trackedEntities.stream().filter(entity -> !entitiesInRange.contains(entity))
                .collect(Collectors.toList());
        
        for(Entity entity : enteredEntities) {            
            if(entity instanceof Npc) {
                sendMessage(new EntityStatusMessage(entity, EntityStatus.ENTERING));
            }
            
            entity.addTracker(this);
        }
        
        for(Entity entity : departedEntities) {
            if(entity instanceof Npc) {
                sendMessage(new EntityStatusMessage(entity, EntityStatus.EXITING));
            }
            
            entity.removeTracker(this);
        }
        
        trackedEntities.clear();
        trackedEntities.addAll(entitiesInRange);
    }
    
    public boolean isTrackingEntity(Entity entity) {
        return trackedEntities.contains(entity);
    }
    
    public List<Entity> getTrackedEntities() {
        return trackedEntities;
    }
    
    public void setConnection(Connection connection) {
        if(isOnline()) {
            kick("You logged in from another location.");
            onDisconnect();
        }
                
        if(connection != null) {
            connection.setPlayer(this);
        }
        
        this.connection = connection;
    }
    
    public Connection getConnection() {
        return connection;
    }
    
    public boolean isOnline() {
        return connection != null && connection.isOpen();
    }
    
    @JsonValue
    public Map<String, Object> getJsonValue() {
        Map<String, Object> map = new HashMap<>();
        map.put("email", email);
        map.put("password_hash", password);
        map.put("auth_tokens", authTokens);
        map.put("name", name);
        map.put("admin", admin);
        map.put("karma", karma);
        map.put("crowns", crowns);
        map.put("current_zone", zone.getDocumentId());
        map.put("equipped_colors", colors);
        map.put("equipped_clothing", clothing);
        map.put("inventory", inventory);
        return map;
    }
    
    private Map<String, Object> getAppearanceConfig() {
        Map<String, Object> appearance = new HashMap<>();
        for(Entry<ClothingSlot, Item> entry : clothing.entrySet()) {
            appearance.put(entry.getKey().getId(), entry.getValue().getId());
        }
        
        for(Entry<ColorSlot, String> entry : colors.entrySet()) {
            appearance.put(entry.getKey().getId(), entry.getValue());
        }
        
        appearance.put(ClothingSlot.SUIT.getId(), inventory.findJetpack().getId()); // Jetpack
        return appearance;
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
        config.put("crowns", crowns);
        config.put("appearance", getAppearanceConfig());
        config.put("settings", settings);
        return config;
    }
}
