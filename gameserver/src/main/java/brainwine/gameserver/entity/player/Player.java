package brainwine.gameserver.entity.player;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;

import brainwine.gameserver.GameConfiguration;
import brainwine.gameserver.GameServer;
import brainwine.gameserver.achievements.Achievement;
import brainwine.gameserver.achievements.AchievementManager;
import brainwine.gameserver.achievements.JourneymanAchievement;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.dialog.Dialog;
import brainwine.gameserver.dialog.DialogListItem;
import brainwine.gameserver.dialog.DialogSection;
import brainwine.gameserver.dialog.DialogType;
import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.entity.EntityStatus;
import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemRegistry;
import brainwine.gameserver.item.ItemUseType;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.item.MiningBonus;
import brainwine.gameserver.item.consumables.Consumable;
import brainwine.gameserver.loot.Loot;
import brainwine.gameserver.server.Message;
import brainwine.gameserver.server.messages.AchievementMessage;
import brainwine.gameserver.server.messages.AchievementProgressMessage;
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
import brainwine.gameserver.server.messages.LevelMessage;
import brainwine.gameserver.server.messages.NotificationMessage;
import brainwine.gameserver.server.messages.PlayerPositionMessage;
import brainwine.gameserver.server.messages.SkillMessage;
import brainwine.gameserver.server.messages.StatMessage;
import brainwine.gameserver.server.messages.TeleportMessage;
import brainwine.gameserver.server.messages.WardrobeMessage;
import brainwine.gameserver.server.messages.XpMessage;
import brainwine.gameserver.server.messages.ZoneStatusMessage;
import brainwine.gameserver.server.models.EntityStatusData;
import brainwine.gameserver.server.pipeline.Connection;
import brainwine.gameserver.util.MapHelper;
import brainwine.gameserver.util.MathUtils;
import brainwine.gameserver.util.Vector2i;
import brainwine.gameserver.zone.Chunk;
import brainwine.gameserver.zone.MetaBlock;
import brainwine.gameserver.zone.Zone;

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
    private final String documentId;
    private String email;
    private String password;
    private boolean admin;
    private int experience;
    private int skillPoints;
    private int karma;
    private int crowns;
    private Inventory inventory;
    private PlayerStatistics statistics;
    private List<String> authTokens;
    private List<PlayerRestriction> mutes;
    private List<PlayerRestriction> bans;
    private Set<Achievement> achievements;
    private Map<String, Float> ignoredHints;
    private Map<Skill, Integer> skills;
    private Map<Item, List<Skill>> bumpedSkills;
    private Map<ClothingSlot, Item> equippedClothing;
    private Map<ColorSlot, String> equippedColors;
    private final Map<String, Object> settings = new HashMap<>();
    private final Set<Integer> activeChunks = new HashSet<>();
    private final Map<Integer, Consumer<Object[]>> dialogs = new HashMap<>();
    private final List<Entity> trackedEntities = new ArrayList<>();
    private String clientVersion;
    private Placement lastPlacement;
    private Item heldItem = Item.AIR;
    private Vector2i spawnPoint = new Vector2i(0, 0);
    private int teleportX;
    private int teleportY;
    private boolean godMode;
    private long lastHeartbeat;
    private long lastTrackedEntityUpdate;
    private Zone nextZone;
    private Connection connection;
    
    protected Player(String documentId, PlayerConfigFile config) {
        super(config.getCurrentZone());
        this.documentId = documentId;
        this.name = config.getName();
        this.email = config.getEmail();
        this.password = config.getPasswordHash();
        this.admin = config.isAdmin();
        this.experience = config.getExperience();
        this.skillPoints = config.getSkillPoints();
        this.karma = config.getKarma();
        this.crowns = config.getCrowns();
        this.inventory = config.getInventory();
        this.statistics = config.getStatistics();
        this.authTokens = config.getAuthTokens();
        this.mutes = config.getMutes();
        this.bans = config.getBans();
        this.achievements = config.getAchievements();
        this.ignoredHints = config.getIgnoredHints();
        this.skills = config.getSkills();
        this.bumpedSkills = config.getBumpedSkills();
        this.equippedClothing = config.getEquippedClothing();
        this.equippedColors = config.getEquippedColors();
        health = getMaxHealth();
        inventory.setPlayer(this);
        statistics.setPlayer(this);
    }
    
    public Player(String documentId, String name, Zone zone) {
        super(zone);
        this.documentId = documentId;
        this.name = name;
        this.inventory = new Inventory(this);
        this.statistics = new PlayerStatistics(this);
        this.authTokens = new ArrayList<>();
        this.mutes = new ArrayList<>();
        this.bans = new ArrayList<>();
        this.achievements = new HashSet<>();
        this.ignoredHints = new HashMap<>();
        this.skills = new HashMap<>();
        this.bumpedSkills = new HashMap<>();
        this.equippedClothing = new HashMap<>();
        this.equippedColors = new HashMap<>();
    }
    
    @JsonCreator
    private static Player fromId(String id) {
        return GameServer.getInstance().getPlayerManager().getPlayerById(id);
    }
    
    @Override
    public void tick(float deltaTime) {
        long now = System.currentTimeMillis();
        statistics.trackPlayTime(deltaTime);
        
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
            sendMessage(new EntityPositionMessage(trackedEntities));
            lastTrackedEntityUpdate = now;
        }
    }
    
    @Override
    public void die(Player killer) {
        statistics.trackDeath();
        sendMessageToPeers(new EntityStatusMessage(this, EntityStatus.DEAD)); // TODO killer id
        GameServer.getInstance().notify(String.format("%s died", name), NotificationType.CHAT);
    }
    
    @Override
    public void notify(Object message, NotificationType type) {
        if(type == NotificationType.SYSTEM && isV3()) {
            sendMessage(new NotificationMessage(message, NotificationType.PEER_ACCOMPLISHMENT));
        } else {
            sendMessage(new NotificationMessage(message, type));
        }
    }
    
    @Override
    public boolean isAdmin() {
        return admin;
    }
    
    @Override
    public float getMaxHealth() {
        int stamina = Math.min(10, getTotalSkillLevel(Skill.STAMINA));
        return DEFAULT_HEALTH + (stamina < 10 ? (stamina - 1) * 0.5F : 5);
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
        
        // Set skills for new players
        for(Skill skill : Skill.values()) {
            if(!skills.containsKey(skill)) {
                skills.put(skill, 1);
            }
        }
        
        // Add some default items if the player has none
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
        
        spawnPoint.setX((int)x);
        spawnPoint.setY((int)y);
        sendMessage(new ConfigurationMessage(id, getClientConfig(), GameConfiguration.getClientConfig(this), zone.getClientConfig(this)));
        sendMessage(new ZoneStatusMessage(zone.getStatusConfig()));
        sendMessage(new ZoneStatusMessage(zone.getStatusConfig()));
        sendMessage(new PlayerPositionMessage((int)x, (int)y));
        sendMessage(new HealthMessage(health));
        sendMessage(new InventoryMessage(inventory));
        sendMessage(new WardrobeMessage(inventory.getWardrobe()));
        sendMessage(new BlockMetaMessage(zone.getGlobalMetaBlocks()));
        
        // Send skill data
        for(Skill skill : skills.keySet()) {
            sendMessage(new SkillMessage(skill, skills.get(skill)));
        }
        
        // Send peer data
        Collection<Player> peers = zone.getPlayers();
        sendMessage(new EntityStatusMessage(peers, EntityStatus.ENTERING));
        sendMessage(new EntityPositionMessage(peers));
        
        // TODO prepack this as well
        for(Player peer : peers) {
            sendMessage(new EntityItemUseMessage(peer.getId(), 0, peer.getHeldItem(), 0));
        }
        
        // Send achievement data
        for(Achievement achievement : AchievementManager.getAchievements()) {
            if(hasAchievement(achievement)) {
                sendMessage(new AchievementMessage(achievement.getTitle(), 0));
            } else {
                int progress = achievement.getProgressPercent(this);
                
                if(progress > 0) {
                    sendMessage(new AchievementProgressMessage(achievement.getTitle(), progress));
                }
            }
        }
        
        // And finally, enter the zone!
        if(isV3()) {
            sendMessage(new EventMessage("zoneEntered", null));
            notify("Welcome to " + zone.getName(), NotificationType.LARGE);
        } else {
            notify("Welcome to " + zone.getName(), NotificationType.WELCOME);
        }
        
        updateAchievementProgress(JourneymanAchievement.class);
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
        
        // Are we switching zones? Then set the new zone.
        if(nextZone != null) {
            zone = nextZone;
            nextZone = null;
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
        nextZone = zone;
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
        if(id == 0) {
            return;
        }
        
        Consumer<Object[]> handler = dialogs.remove(id);
        
        if(handler == null) {
            if(!(input.length == 1 && input[0].equals("cancel"))) {
                notify("Sorry, the request has expired.");
            }
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
    
    public void setGodMode(boolean godMode) {
        this.godMode = godMode;
    }
    
    public boolean isGodMode() {
        return admin && godMode;
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
        GameServer.getInstance().notify(message, type);
        
        for(Player player : zone.getPlayers()) {
             if(player != this) {
                 player.notify(message, type);
             }
         }
    }
    
    public void notify(Object message) {
        notify(message, NotificationType.POPUP);
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
                    zone.updateBlock(x, y, Layer.FRONT, item, mod, null, metadata);
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
        return 5 + getTotalSkillLevel(Skill.MINING) / 3.0;
    }
    
    public double getPlacementRange() {
        return Math.ceil(MathUtils.lerp(5.0, 13.0, (double)getTotalSkillLevel(Skill.BUILDING) / MAX_SKILL_LEVEL));
    }
    
    public int getMaxTargetableEntities() {
        return 1 + getTotalSkillLevel(Skill.AGILITY) / 2;
    }
    
    public double getMiningBonusChance(MiningBonus bonus) {
        if(heldItem.getGroup() != bonus.getTool()) {
            return 0.0;
        }
        
        return bonus.getChance() * (getTotalSkillLevel(bonus.getSkill()) / (double)MAX_SKILL_LEVEL) * heldItem.getToolBonus();
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
    
    public void mute(String reason, OffsetDateTime until) {
        mute(null, reason, until);
    }
    
    public void mute(Player issuer, String reason, OffsetDateTime endDate) {
        mutes.add(new PlayerRestriction(issuer, reason, endDate));
        notify(String.format("You have been muted until %s for: %s", 
                endDate.format(DateTimeFormatter.RFC_1123_DATE_TIME), reason) , NotificationType.SYSTEM);
    }
    
    public void unmute() {
        unmute(null);
    }
    
    public void unmute(Player issuer) {
        PlayerRestriction currentMute = getCurrentMute();
        
        if(currentMute != null) {
            currentMute.pardon(issuer);
            notify("You have been unmuted.", NotificationType.SYSTEM);
        }
    }
    
    public boolean isMuted() {
        return getCurrentMute() != null;
    }
    
    public PlayerRestriction getCurrentMute() {
        return mutes.stream().filter(PlayerRestriction::isActive).findFirst().orElse(null);
    }
    
    public List<PlayerRestriction> getMutes() {
        return Collections.unmodifiableList(mutes);
    }
    
    public void ban(String reason, OffsetDateTime until) {
        ban(null, reason, until);
    }
    
    public void ban(Player issuer, String reason, OffsetDateTime endDate) {
        bans.add(new PlayerRestriction(issuer, reason, endDate));
        kick(String.format("You have been banned: %s", reason));
    }
    
    public void unban() {
        unban(null);
    }
    
    public void unban(Player issuer) {
        PlayerRestriction currentBan = getCurrentBan();
        
        if(currentBan != null) {
            currentBan.pardon(issuer);
        }
    }
    
    public boolean isBanned() {
        return getCurrentBan() != null;
    }
    
    public PlayerRestriction getCurrentBan() {
        return bans.stream().filter(PlayerRestriction::isActive).findFirst().orElse(null);
    }
    
    public List<PlayerRestriction> getBans() {
        return Collections.unmodifiableList(bans);
    }
    
    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
    
    public void addExperience(int amount) {
        addExperience(amount, null);
    }
    
    public void addExperience(int amount, String message) {
        if(amount > 0) {
            setExperience(experience + amount, message);
        }
    }
    
    public void setExperience(int experience) {
        setExperience(experience, null);
    }
    
    public void setExperience(int experience, String message) {
        int amount = experience - this.experience;
        int oldLevel = getLevel();
        this.experience = experience;
        sendMessage(new XpMessage(amount, experience, message));
        int newLevel = getLevel();
        
        if(newLevel != oldLevel) {
            skillPoints += Math.max(0, newLevel - oldLevel);
            sendDelayedMessage(new LevelMessage(newLevel), 5000);
            sendDelayedMessage(new EffectMessage(0, 0, "levelup", 1), 5000);
            sendDelayedMessage(new StatMessage("points", skillPoints), 5000);
            notifyPeers(String.format("%s leveled up to level %s!", name, newLevel), NotificationType.SYSTEM);
        }
    }
    
    public int getExperienceForLevel(int level) {
        return (level * level * 250) + (level * 1750) - 2000; // I regret nothing!
    }
    
    public int getExperience() {
        return experience;
    }
    
    public void setLevel(int level) {
        setExperience(getExperienceForLevel(level));
    }
    
    // I regret everything!
    public int getLevelFromExperience(int experience) {
        int level = 1;
        
        while(level < getMaxLevel() && experience >= getExperienceForLevel(level + 1)) {
            level++;
        }
        
        return level;
    }
    
    public int getMaxLevel() {
        return 1 + Skill.values().length * 7;
    }
    
    public int getLevel() {
        return getLevelFromExperience(experience);
    }
    
    public void setSkillPoints(int skillPoints) {
        this.skillPoints = skillPoints;
        sendMessage(new StatMessage("points", skillPoints));
    }
    
    public int getSkillPoints() {
        return skillPoints;
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
    
    public void ignoreHint(String hint) {
        ignoredHints.put(hint, statistics.getPlayTime());
    }
    
    public boolean ignoresHint(String hint) {
        return ignoredHints.containsKey(hint);
    }
    
    public Map<String, Float> getIgnoredHints() {
        return Collections.unmodifiableMap(ignoredHints);
    }
    
    public <T extends Achievement> void updateAchievementProgress(Class<T> achievementType) {
        List<Achievement> achievementsToCheck = AchievementManager.getAchievements().stream()
                .filter(achievement -> !hasAchievement(achievement) 
                && achievementType.isAssignableFrom(achievement.getClass())
                && (achievement.getPrevious() == null || hasAchievement(achievement.getPrevious())))
                .collect(Collectors.toList());
        
        for(Achievement achievement : achievementsToCheck) {
            if(achievement.isCompleted(this)) {
                addAchievement(achievement);
            } else {
                int progress = achievement.getProgress(this);
                int percentage = achievement.getProgressPercent(progress);
                sendMessage(new AchievementProgressMessage(achievement.getTitle(), percentage));
                
                if(percentage >= 75) {
                    notifyAchievementProgress(achievement, progress, "75%", "almost");
                } else if(percentage >= 50) {
                    notifyAchievementProgress(achievement, progress, "50%", "halfway");
                } else if(percentage >= 25) {
                    notifyAchievementProgress(achievement, progress, "25%", "a quarter of the way");
                }
            }
        }
    }
    
    public void notifyAchievementProgress(Achievement achievement, int progress, String percentage, String description) {
        String title = achievement.getTitle();
        String hint = String.format("%s %s", title, percentage);
        
        if(!ignoresHint(hint)) {
            String notification = achievement.getNotification();
            
            if(notification == null) {
                notify(String.format("You're %s to the %s achievement!", description, title));
            } else {
                notify(String.format("You've %s - %s to the %s achievement!",
                        notification.replace("*", String.valueOf(progress)), description, title));
            }
            
            ignoreHint(hint);
        }
    }
    
    public void addAchievement(Achievement achievement) {
        if(achievements.add(achievement)) {
            int experience = achievement.getExperience();
            String title = achievement.getTitle();
            addExperience(experience);
            sendMessage(new AchievementMessage(title, experience)); 
            notifyPeers(String.format("%s has earned the %s achievement.", name, title), NotificationType.SYSTEM);
            
            if(isV3()) {
                notify(title, NotificationType.ACHIEVEMENT);
            }
        }
    }
    
    public void removeAchievement(Achievement achievement) {
        achievements.remove(achievement);
    }
    
    public boolean hasAchievement(Achievement achievement) {
        return achievements.contains(achievement);
    }
    
    public Set<Achievement> getAchievements() {
        return Collections.unmodifiableSet(achievements);
    }
    
    public void setClothing(ClothingSlot slot, Item item) {
        if(!item.isClothing()) {
            return;
        }
        
        equippedClothing.put(slot, item);
        zone.sendMessage(new EntityChangeMessage(id, getAppearanceConfig()));
    }
    
    public Map<ClothingSlot, Item> getEquippedClothing() {
        return Collections.unmodifiableMap(equippedClothing);
    }
    
    public void setColor(ColorSlot slot, String hex) {
        // TODO check if the string is actually a valid hex color
        equippedColors.put(slot, hex);
        zone.sendMessage(new EntityChangeMessage(id, getAppearanceConfig()));
    }
    
    public Map<ColorSlot, String> getEquippedColors() {
        return Collections.unmodifiableMap(equippedColors);
    }
    
    public void setSkillLevel(Skill skill, int level) {
        skills.put(skill, level);
        sendMessage(new SkillMessage(skill, level));
    }
    
    public int getTotalSkillLevel(Skill skill) {
        int accessorySkillLevel = 0;
        
        // Get the highest skill bonus accessory
        for(Item accessory : inventory.getAccessories().getItems()) {
            int skillBonus = accessory.getSkillBonuses().getOrDefault(skill, 0);
            
            if(skillBonus > accessorySkillLevel) {
                accessorySkillLevel = skillBonus;
            }
        }
        
        // TODO account for exoskeleton bonuses
        return getSkillLevel(skill) + accessorySkillLevel;
    }
    
    public int getSkillLevel(Skill skill) {
        return skills.getOrDefault(skill, 1);
    }
    
    public Set<Skill> getUpgradeableSkills() {
        Set<Skill> upgradeableSkills = skills.keySet().stream()
                .filter(skill -> getSkillLevel(skill) < MAX_NATURAL_SKILL_LEVEL)
                .collect(Collectors.toSet());
        
        if(getLevel() < 10) {
            upgradeableSkills.removeAll(Arrays.asList(Skill.getAdvancedSkills()));
        }
        
        return upgradeableSkills;
    }
    
    public Map<Skill, Integer> getSkills() {
        return Collections.unmodifiableMap(skills);
    }
    
    public void trackSkillBump(Item item, Skill skill) {
        List<Skill> skills = bumpedSkills.get(item);
        
        if(skills == null) {
            skills = new ArrayList<>();
            bumpedSkills.put(item, skills);
        }
        
        skills.add(skill);
    }
    
    public boolean hasSkillBeenBumped(Item item, Skill skill) {
        return bumpedSkills.getOrDefault(item, Collections.emptyList()).contains(skill);
    }
    
    public Map<Item, List<Skill>> getBumpedSkills() {
        return bumpedSkills;
    }
    
    public void consume(Item item) {
        consume(item, null);
    }
    
    public void consume(Item item, Object details) {
        Consumable consumable = item.getAction().getConsumable();
        
        if(consumable == null) {
            sendMessage(new InventoryMessage(inventory.getClientConfig(item)));
            notify("Sorry, this action hasn't been implemented yet.");
            return;
        }
        
        consumable.consume(item, this, details);
    }
    
    public void awardLoot(Loot loot) {
        awardLoot(loot, DialogType.LOOT);
    }
    
    public void awardLoot(Loot loot, DialogType dialogType) {
        Dialog dialog = new Dialog();
        DialogSection section = new DialogSection();
        dialog.addSection(section);
        
        loot.getItems().forEach((item, quantity) -> {
            inventory.addItem(item, quantity, true);
            section.addItem(new DialogListItem()
                    .setItem(item.getCode())
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
        }
    }
    
    public Inventory getInventory() {
        return inventory;
    }
    
    public PlayerStatistics getStatistics() {
        return statistics;
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
        // Get all entities in range of the player
        List<Entity> entitiesInRange = zone.getEntitiesInRange(x, y, ENTITY_VISIBILITY_RANGE);
        
        // Exclude self
        entitiesInRange.remove(this);
        
        // Get entities that have entered the player's view
        List<Entity> enteredEntities = entitiesInRange.stream()
                .filter(entity -> !trackedEntities.contains(entity))
                .collect(Collectors.toList());
        
        // Get entities that have left the player's view
        List<Entity> departedEntities = trackedEntities.stream()
                .filter(entity -> !entitiesInRange.contains(entity))
                .collect(Collectors.toList());
        
        // Create status data for relevant entities
        List<EntityStatusData> statuses = new ArrayList<>();
        
        for(Entity entity : enteredEntities) {
            if(entity instanceof Npc) {
                statuses.add(EntityStatusData.entering(entity));
            }
            
            entity.addTracker(this);
        }
        
        for(Entity entity : departedEntities) {
            if(entity instanceof Npc) {
                statuses.add(EntityStatusData.exiting(entity));
            }
            
            entity.removeTracker(this);
        }
        
        // Send status data if there is any
        if(!statuses.isEmpty()) {
            sendMessage(new EntityStatusMessage(statuses));
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
    
    private Map<String, Object> getAppearanceConfig() {
        Map<String, Object> appearance = new HashMap<>();
        
        for(Entry<ClothingSlot, Item> entry : equippedClothing.entrySet()) {
            appearance.put(entry.getKey().getId(), entry.getValue().getCode());
        }
        
        for(Entry<ColorSlot, String> entry : equippedColors.entrySet()) {
            appearance.put(entry.getKey().getId(), entry.getValue());
        }
        
        appearance.put(ClothingSlot.SUIT.getId(), inventory.findJetpack().getCode()); // Jetpack
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
        config.put("premium", true);
        config.put("level", getLevel());
        config.put("xp", experience);
        config.put("points", skillPoints);
        config.put("karma", getKarmaLevel());
        config.put("crowns", crowns);
        config.put("hints", ignoredHints);
        config.put("show_hints", true);
        config.put("items_mined", statistics.getTotalItemsMined());
        config.put("items_placed", statistics.getItemsPlaced());
        config.put("items_crafted", statistics.getTotalItemsCrafted());
        config.put("play_time", (int)(statistics.getPlayTime()));
        config.put("deaths", statistics.getDeaths());
        config.put("appearance", getAppearanceConfig());
        config.put("settings", settings);
        return config;
    }
}
