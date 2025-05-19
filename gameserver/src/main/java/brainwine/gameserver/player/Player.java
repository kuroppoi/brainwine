package brainwine.gameserver.player;

import static brainwine.shared.LogMarkers.SERVER_MARKER;

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
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import brainwine.gameserver.GameConfiguration;
import brainwine.gameserver.GameServer;
import brainwine.gameserver.Timer;
import brainwine.gameserver.achievement.Achievement;
import brainwine.gameserver.achievement.AchievementManager;
import brainwine.gameserver.achievement.JourneymanAchievement;
import brainwine.gameserver.achievement.PositionAchievement;
import brainwine.gameserver.command.CommandExecutor;
import brainwine.gameserver.dialog.Dialog;
import brainwine.gameserver.dialog.DialogHelper;
import brainwine.gameserver.dialog.DialogListItem;
import brainwine.gameserver.dialog.DialogSection;
import brainwine.gameserver.dialog.DialogType;
import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.entity.EntityAttack;
import brainwine.gameserver.entity.EntityStatus;
import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.item.DamageType;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemRegistry;
import brainwine.gameserver.item.ItemUseType;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.item.MiningBonus;
import brainwine.gameserver.item.Tradeability;
import brainwine.gameserver.item.consumables.Consumable;
import brainwine.gameserver.loot.Loot;
import brainwine.gameserver.order.OrderManager;
import brainwine.gameserver.quest.DailyQuests;
import brainwine.gameserver.quest.PlayerQuests;
import brainwine.gameserver.quest.Quest;
import brainwine.gameserver.quest.QuestEvents;
import brainwine.gameserver.quest.QuestProgress;
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
import brainwine.gameserver.server.messages.FollowMessage;
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
import brainwine.gameserver.server.models.PlayerStat;
import brainwine.gameserver.server.pipeline.Connection;
import brainwine.gameserver.util.MapHelper;
import brainwine.gameserver.util.MathUtils;
import brainwine.gameserver.util.ValueWithExpiry;
import brainwine.gameserver.util.Vector2i;
import brainwine.gameserver.util.VersionUtils;
import brainwine.gameserver.zone.Biome;
import brainwine.gameserver.zone.Block;
import brainwine.gameserver.zone.Chunk;
import brainwine.gameserver.zone.MetaBlock;
import brainwine.gameserver.zone.Zone;
import brainwine.gameserver.zone.ZoneManager;

import com.fasterxml.jackson.annotation.JsonCreator;

public class Player extends Entity implements CommandExecutor {
    
    public static final int RECENT_ZONE_LIMIT = 12;
    public static final int BOOKMARKED_ZONE_LIMIT = 50;
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
    private static final Logger logger = LogManager.getLogger();
    private static int dialogDiscriminator;
    private final String documentId;
    private String email;
    private String password;
    private String apiToken;
    private boolean admin;
    private int experience;
    private int skillPoints;
    private int karma;
    private int crowns;
    private Inventory inventory;
    private PlayerStatistics statistics;
    private List<String> authTokens;
    private List<NameChange> nameChanges;
    private List<PlayerRestriction> mutes;
    private List<PlayerRestriction> bans;
    private List<String> recentZones;
    private List<String> bookmarkedZones;
    private Set<String> followees;
    private Set<String> followers;
    private Set<String> lootCodes;
    private Set<Achievement> achievements;
    private Map<String, Integer> orders = new HashMap<>();
    private String displayedOrder = null;
    private Map<String, Float> ignoredHints;
    private Map<Skill, Integer> skills;
    private Map<Item, List<Skill>> bumpedSkills;
    private Map<String, Object> appearance;
    private Map<String, QuestProgress> questProgresses = new HashMap<>();
    private ValueWithExpiry<List<Quest>> dailyQuest = ValueWithExpiry.getExpired();
    private Map<String, Quest> androidQuests = new HashMap<>();
    private String familyName = null;
    private final Map<String, Object> settings = new HashMap<>();
    private final Set<Integer> activeChunks = new HashSet<>();
    private final Map<Integer, Consumer<Object[]>> dialogs = new HashMap<>();
    private final List<Timer<String>> timers = new ArrayList<>();
    private final List<Entity> trackedEntities = new ArrayList<>();
    private String clientVersion;
    private TradeSession tradeSession;
    private Placement lastPlacement;
    private MetaBlock transmittableBlock;
    private Item heldItem = Item.AIR;
    private double breath = 1.0;
    private double thirst;
    private double cold;
    private int spawnX;
    private int spawnY;
    private int enterX;
    private int enterY;
    private int teleportX;
    private int teleportY;
    private boolean stealth;
    private boolean godMode;
    private boolean customSpawn;
    private boolean changingZones;
    private long lastBreathMessage;
    private long lastThirstMessage;
    private long lastThirstDamageAt;
    private long lastFreezeMessage;
    private long lastHeartbeat;
    private long lastTrackedEntityUpdate;
    private long lastLandmarkVoteAt;
    private long lastQuestTimeMessageAt;
    private Zone previousZone;
    private Zone nextZone;
    private Connection connection;

    protected Player(String documentId, PlayerConfigFile config) {
        super(config.getCurrentZone());
        this.documentId = documentId;
        this.name = config.getName();
        this.email = config.getEmail();
        this.password = config.getPasswordHash();
        this.apiToken = config.getApiToken();
        this.admin = config.isAdmin();
        this.experience = config.getExperience();
        this.skillPoints = config.getSkillPoints();
        this.karma = config.getKarma();
        this.crowns = config.getCrowns();
        this.displayedOrder = config.getDisplayedOrder();
        this.inventory = config.getInventory();
        this.statistics = config.getStatistics();
        this.authTokens = config.getAuthTokens();
        this.nameChanges = config.getNameChanges();
        this.mutes = config.getMutes();
        this.bans = config.getBans();
        this.recentZones = config.getRecentZones();
        this.bookmarkedZones = config.getBookmarkedZones();
        this.followees = config.getFollowees();
        this.followers = config.getFollowers();
        this.lootCodes = config.getLootCodes();
        this.achievements = config.getAchievements();
        this.orders = config.getOrders();
        this.ignoredHints = config.getIgnoredHints();
        this.skills = config.getSkills();
        this.bumpedSkills = config.getBumpedSkills();
        this.appearance = config.getAppearance();
        this.questProgresses = config.getQuestProgresses();
        this.dailyQuest = config.getDailyQuest();
        this.androidQuests = config.getAndroidQuests();
        this.familyName = config.getFamilyName();
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
        this.nameChanges = new ArrayList<>();
        this.mutes = new ArrayList<>();
        this.bans = new ArrayList<>();
        this.recentZones = new ArrayList<>();
        this.bookmarkedZones = new ArrayList<>();
        this.followees = new HashSet<>();
        this.followers = new HashSet<>();
        this.lootCodes = new HashSet<>();
        this.achievements = new HashSet<>();
        this.ignoredHints = new HashMap<>();
        this.skills = new HashMap<>();
        this.bumpedSkills = new HashMap<>();
        this.appearance = Appearance.getRandomAppearance();
    }
    
    @JsonCreator
    private static Player fromId(String id) {
        return GameServer.getInstance().getPlayerManager().getPlayerById(id);
    }
    
    @Override
    public void tick(float deltaTime) {
        super.tick(deltaTime);
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

        if(!isDead()) {
            applyBreath(deltaTime);
            applyThirst(deltaTime);
            applyFreeze(deltaTime);
            OrderManager.advance(this);
        }

        // Try to timeout trade
        if(isTrading()) {
            tradeSession.timeout();
        }
        
        // Process timers
        timers.removeIf(Timer::process);
        
        // Update tracked entities
        if(now - lastTrackedEntityUpdate >= TRACKED_ENTITY_UPDATE_INTERVAL) {
            updateTrackedEntities();
            sendMessage(new EntityPositionMessage(trackedEntities));
            lastTrackedEntityUpdate = now;
        }

        DailyQuests.tryIssueDailyQuest(this);

        long dailyQuestTimeLeft = getDailyQuest().getTimeUntilExpiry(System.currentTimeMillis());
        long dailyQuestRequiredInterval = dailyQuestTimeLeft >= 3600000 ? 600000 : 30000;
        if(System.currentTimeMillis() >= lastQuestTimeMessageAt + dailyQuestRequiredInterval) {
            lastQuestTimeMessageAt = System.currentTimeMillis();
            DailyQuests.sendDailyQuestTime(this, dailyQuestTimeLeft);
        }
    }
    
    @Override
    public void die(EntityAttack cause) {
        Entity killer = cause == null ? null : cause.getAttacker();
        String serverMessage = String.format("%s died.", name);
        Map<String, Object> details = new HashMap<>();
        
        if(killer != null) {
            details.put("<", killer.getId());
            
            if(killer.isPlayer()) {
                // TODO track kill for killer achievement in pvp zones
                serverMessage = String.format("%s killed %s.", killer.getName(), name);
            }
        }
        
        sendMessageToPeers(new EntityStatusMessage(this, EntityStatus.DEAD, details));
        GameServer.getInstance().notify(serverMessage, NotificationType.CHAT);
        statistics.trackDeath();
    }
    
    @Override
    public void notify(Object message, NotificationType type) {
        // TODO type SYSTEM (2) apparently plays the karma warning sound on v2 clients, so I guess we'll be mapping all of them to PEER_ACCOMPLISHMENT (11).
        sendMessage(new NotificationMessage(message, type == NotificationType.SYSTEM ? NotificationType.PEER_ACCOMPLISHMENT : type));
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

    @Override
    public void blockPositionChanged() {
        super.blockPositionChanged();
        updateAchievementProgress(PositionAchievement.class); // TODO check on interval rather than every block position change
    }

    public double getBreathCapacity() {
        return 15.0 + 1.25 * (getTotalSkillLevel(Skill.SURVIVAL) - 1);
    }

    public boolean isSubmerged() {
        Block headBlock = getZone().getBlock(getBlockX(), getBlockY() - 1);

        if(headBlock == null) return false;

        Item liquidItem = headBlock.getLiquidItem();

        return !liquidItem.isAir() && headBlock.getLiquidMod() > 2;
    }

    public void applyBreath(float deltaTime) {
        if(isGodMode() || !inventory.findAccessoryWithUse(ItemUseType.BREATH).isAir()) {
            breath = 1.0;
        } else {
            if(isSubmerged()) {
                breath -= deltaTime / getBreathCapacity();
            } else {
                breath += deltaTime / 5.0;
            }
            breath = MathUtils.clamp(breath, 0.0, 1.0);

            long currentTime = System.currentTimeMillis();
            if(lastBreathMessage + 1000 < currentTime) {
                sendMessage(new StatMessage(PlayerStat.BREATH, breath));
                if(breath < 0.001) attack(null, null, 0.5f, DamageType.SUFFOCATION);
                lastBreathMessage = currentTime;
            }
        }
    }

    public void applyThirst(float deltaTime) {
        long now = System.currentTimeMillis();

        // Update thirst stat
        if(isGodMode() || !inventory.findAccessoryWithUse(ItemUseType.HAZMAT).isAir()) {
            thirst = 0.0;
        } else {
            double thirstPeriod = MathUtils.lerp(5.0, 10.0, (getTotalSkillLevel(Skill.SURVIVAL) - 1) / 6.0) * 60;
            int direction = zone.getBiome() == Biome.DESERT && !zone.isPurified() ? 1 : -1;
            thirst = MathUtils.clamp(thirst + (direction * deltaTime / thirstPeriod), 0.0, 1.0);
        }

        // Send message if it is time
        if(now > lastThirstMessage + 1000) {
            sendMessage(new StatMessage(PlayerStat.THIRST, (float)thirst));
            lastThirstMessage = now;
        }

        if(thirst >= 1.0) {
            Item waterJar = ItemRegistry.getItem("containers/jar-water");

            // Consume a jar of water if the player has any and reset thirst
            if(inventory.hasItem(waterJar)) {
                inventory.removeItem(waterJar, true);
                inventory.addItem(ItemRegistry.getItem("containers/jar"), true); // Refund empty jar
                notify(String.format("-1 %s", waterJar.getTitle()));
                thirst = 0.0;
                return;
            }

            // Damage the player every 3 seconds instead if they have no water in their inventory
            if(now > lastThirstDamageAt + 3000 && health > 1.0) {
                attack(null, null, 0.25F, DamageType.FIRE, true); // Apply as true damage
                lastThirstDamageAt = now;
            }
        }
    }

    public void applyFreeze(float deltaTime) {
        long now = System.currentTimeMillis();

        // Update freeze stat
        if(isGodMode() || !inventory.findAccessoryWithUse(ItemUseType.HAZMAT).isAir()) {
            cold = 0.0;
        } else {
            double freezePeriod = MathUtils.lerp(3.0, 10.0, (getTotalSkillLevel(Skill.SURVIVAL) - 1) / 6.0) * 60;
            int direction = zone.getBiome() == Biome.ARCTIC ? 1 : -2; // Warm back up twice as fast
            cold = MathUtils.clamp(cold + (direction * deltaTime / freezePeriod), 0.0, 1.0);
        }

        // Send message & perform damage tick if it is time
        if(now > lastFreezeMessage + 1000) {
            if(cold >= 1.0 && health > 1.0) {
                attack(null, null, 0.25F, DamageType.COLD, true); // Apply as true damage
            }

            sendMessage(new StatMessage(PlayerStat.FREEZE, (float)cold));
            lastFreezeMessage = now;
        }
    }

    public void applyWarmth() {
        cold = 0.0;
        sendMessage(new StatMessage(PlayerStat.FREEZE, (float)cold));
    }

    @Override
    public float getAttackMultiplier(EntityAttack attack) {
        return isGodMode() ? 9999.0F : 1.0F;
    }
    
    @Override
    public float getDefense(EntityAttack attack) {
        return getNormalizedSkill(Skill.SURVIVAL) * 0.5F;
    }
    
    @Override
    public boolean isInvulnerable() {
        return invulnerable || isGodMode();
    }
    
    @Override
    public void setProperties(Map<String, Object> properties, boolean sendMessage) {
        super.setProperties(properties, sendMessage);
        
        if(sendMessage) {
            sendMessage(new EntityChangeMessage(id, properties));
        }
    }
    
    /**
     * @return A {@link Map} containing all the data necessary for use in {@link EntityStatusMessage}.
     */
    @Override
    public Map<String, Object> getStatusConfig() {
        Map<String, Object> config = super.getStatusConfig();
        config.put("id", documentId);
        config.putAll(getVisibleAppearance());
        config.put("u", inventory.findJetpack().getCode());
        config.put("ni", getIcon());
        return config;
    }
    
    /**
     * Called by {@link Zone#addEntity(Entity)} when the player is added to it.
     */
    public void onZoneEntered() {
        boolean spawnEffect = false;

        // Find new spawn point if zone has changed
        if(zone != previousZone) {
            MetaBlock spawn = zone.getRandomSpawnBlock();
            
            if(spawn == null) {
                spawnX = zone.getWidth() / 2;
                spawnY = 2;
            } else {
                spawnX = spawn.getX() + 1;
                spawnY = spawn.getY();
            }

            x = spawnX;
            y = spawnY;
            spawnEffect = true;
        }

        // Handle custom spawn location
        if(zone != nextZone) {
            x = spawnX;
            y = spawnY;
            spawnEffect = true;
        } else if(customSpawn && zone == nextZone) {
            x = enterX;
            y = enterY;
        }
        
        customSpawn = false;

        // Rescue player if they're out of bounds somehow
        // blockX and blockY might not be assigned yet so we check the absolute position
        if(!zone.areCoordinatesInBounds((int)x, (int)y)) {
            x = spawnX;
            y = spawnY;
            spawnEffect = true;
        }
        
        // Set skills for new players
        for(Skill skill : Skill.values()) {
            if(!skills.containsKey(skill)) {
                skills.put(skill, 1);
            }
        }
        
        ZoneManager zoneManager = GameServer.getInstance().getZoneManager();
        PlayerManager playerManager = GameServer.getInstance().getPlayerManager();

        // Issue an API token if the player doesn't have one
        if(apiToken == null) {
            playerManager.issueApiToken(this);
        }

        sendMessage(new ConfigurationMessage(id, getClientConfig(), GameConfiguration.getClientConfig(this), zone.getClientConfig(this)));
        sendMessage(new ZoneStatusMessage(zone.getStatusConfig(this)));
        zone.sendMachineStatus(this);
        sendMessage(new PlayerPositionMessage((int)x, (int)y));
        sendMessage(new HealthMessage(health));
        // Configuration message doesn't cause the Unity client to update the appearance when changing zones
        sendMessage(new EntityChangeMessage(getId(), getVisibleAppearance()));
        
        // Send skill data
        for(Skill skill : skills.keySet()) {
            sendMessage(new SkillMessage(skill, skills.get(skill)));
        }
        
        sendMessage(new InventoryMessage(inventory));
        sendMessage(new WardrobeMessage(inventory.getWardrobe()));
        sendMessage(new BlockMetaMessage(zone.getGlobalMetaBlocks()));
        
        // Send peer data
        Collection<Player> peers = zone.getPlayers();
        sendMessage(new EntityStatusMessage(peers, EntityStatus.ENTERING));
        sendMessage(new EntityPositionMessage(peers));
        sendMessage(new EntityItemUseMessage(peers));
        
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
        
        if(spawnEffect) {
            zone.spawnEffect(x + 0.5F, y - 0.75F, "spawn", 20);
        }

        // Send social info
        sendMessage(new FollowMessage(followees.stream().map(playerManager::getPlayerById).filter(Objects::nonNull).collect(Collectors.toList()), 0));
        sendMessage(new FollowMessage(followers.stream().map(playerManager::getPlayerById).filter(Objects::nonNull).collect(Collectors.toList()), 1));
        sendMessage(new EventMessage("socialInfoReady", null));
        
        // Clear invalid bookmarks
        bookmarkedZones.removeIf(bookmark -> zoneManager.getZone(bookmark) == null || !zoneManager.getZone(bookmark).canJoin(this));
        
        // Misc stuff
        updateAchievementProgress(JourneymanAchievement.class);
        checkRegistration();
        PlayerQuests.deleteUnknownQuestProgress(this);
        PlayerQuests.sendInitialPlayerQuestMessages(this);
        QuestEvents.handleEnterZone(this, zone);
        recentZones.remove(zone.getDocumentId()); // Remove first in case the zone has already been visited recently
        recentZones.add(0, zone.getDocumentId()); // Add at top so we don't have to reverse the list for the zone searcher
        
        while(recentZones.size() > RECENT_ZONE_LIMIT) {
            recentZones.remove(recentZones.size() - 1);
        }
    }
    
    /**
     * Called from {@link Connection} when the channel becomes inactive.
     * 
     * TODO Should we force process all timers on disconnect?
     */
    public void onDisconnect() {
        lastHeartbeat = 0;
        lastPlacement = null;
        transmittableBlock = null;
        clientVersion = null;
        previousZone = zone;

        if(zone != null) {
            zone.removeEntity(this);
        }
        
        // Are we switching zones? Then set the new zone.
        if(changingZones) {
            zone = nextZone;
            changingZones = false;
        } else {
            nextZone = zone;
        }
        
        // Cancel existing trade session
        if(isTrading()) {
            tradeSession.cancel(this);
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
        changeZone(zone, -1, -1);
    }
    
    public void changeZone(Zone zone, int x, int y) {
        changingZones = true;
        customSpawn = x != -1 && y != -1;
        nextZone = zone;
        enterX = x;
        enterY = y;
        sendMessage(new EventMessage("playerWillChangeZone", null));
        kick("Teleporting...", true);
    }
    
    public void showDialog(Dialog dialog) {
        showDialog(dialog, null);
    }
    
    public void showDialog(Dialog dialog, Consumer<Object[]> handler) {
        sendMessage(new DialogMessage(storeDialogHandler(handler), dialog));
    }

    public void showDialog(Map<String, Object> dialog) {
        showDialog(dialog, null);
    }

    public void showDialog(Map<String, Object> dialog, Consumer<Object[]> handler) {
        sendMessage(new DialogMessage(storeDialogHandler(handler), dialog));
    }

    private int storeDialogHandler(Consumer<Object[]> handler) {
        int id = handler == null ? 0 : ++dialogDiscriminator;
        
        if(id != 0) {
            dialogs.put(id, handler);
        }
        
        return id;
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
            try {
                handler.accept(input);
            } catch(Exception e) {
                logger.error(SERVER_MARKER, "An error occured while handling dialog input", e);
                notify("Oops! There was a problem processing your input.");
            }
        }
    }
    
    public void addTimer(String key, long delay, Runnable action) {
        removeTimer(key);
        timers.add(new Timer<>(key, delay, action));
    }
    
    public void removeTimer(String key) {
        timers.removeIf(timer -> timer.getKey().equals(key));
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
    
    public boolean hasClientVersion(String version) {
        return clientVersion != null && VersionUtils.isGreaterOrEqualTo(clientVersion, version);
    }
    
    public boolean isV3() {
        return hasClientVersion("3.0.0");
    }
    
    /**
     * Rubberbands this player back to its last valid position.
     */
    public void rubberband() {
        sendMessage(new PlayerPositionMessage((int)x, (int)y + 1));
    }
    
    public void respawn() {
        // Check minigame spawnpoint
        if(hasActiveMinigame()) {
            Vector2i spawnPoint = minigame.getSpawnPoint(this);

            if(spawnPoint != null) {
                respawn(spawnPoint.getX(), spawnPoint.getY());
                return;
            }
        }

        // Respawn at default spawn point
        respawn(spawnX, spawnY);
    }

    public void respawn(int x, int y) {
        if(isDead()) {
            setHealth(getMaxHealth());
            breath = 1.0;
            thirst = 0.0;
            cold = 0.0;
        }
        
        sendMessage(new PlayerPositionMessage(x, y));
        sendMessageToPeers(new EntityStatusMessage(this, EntityStatus.REVIVED));
        zone.spawnEffect(x + 0.5F, y - 0.75F, "spawn", 20);
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
        zone.spawnEffect(x, y, "teleport", 20);
    }
    
    public int getTeleportX() {
        return teleportX;
    }
    
    public int getTeleportY() {
        return teleportY;
    }
    
    public void setStealth(boolean stealth) {
        this.stealth = stealth;
        setProperty("xs", stealth ? 1 : 0, true);
    }
    
    public boolean isStealthy() {
        return stealth;
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

    public void notifyProfile(String title, String description) {
        if(isV3()) {
            notify(String.format("<color=#ffd95f>%s</color>\n%s", title, description));
        } else {
            notify(MapHelper.map(String.class, String.class, "title", title, "desc", description), NotificationType.PROFILE);
        }
    }

    public MetaBlock getTransmittableBlock() {
        return transmittableBlock;
    }

    public void setTransmittableBlock(MetaBlock transmittableBlock) {
        this.transmittableBlock = transmittableBlock;
    }

    public void setHeldItem(Item item) {
        heldItem = item;
    }
    
    public Item getHeldItem() {
        return heldItem;
    }
    
    public void tradeItem(Player recipient, Item item) {
        // Cannot trade with self
        if(recipient == this) {
            return;
        }

        if(zone != null && !zone.isMarket()) {
            showDialog(DialogHelper.messageDialog("Trade at the Market!", "Trading is only allowed in Market worlds and private worlds. Ask the player to join you in a Market world."));
            return;
        }

        // Check if item is tradeable
        if(!isGodMode() && item.getTradeability() == Tradeability.FALSE) {
            notify("Sorry, you cannot trade this item.");
            return;
        }

        // Check if player is high enough level to trade this item
        if(!isGodMode() && item.getTradeability() == Tradeability.LEVELED && getLevel() < 20) {
            notify("You must be level 20+ to trade this item.");
            return;
        }

        // Cancel the current trade if the player is initiating a new trade
        if(isTrading() && !tradeSession.isParticipant(recipient)) {
            tradeSession.cancel(this);
        }
        
        // Create a new trade session if it doesn't exist
        if(!isTrading()) {
            tradeSession = new TradeSession(this, recipient);
        }
        
        // Process the offer
        tradeSession.onItemOffered(this, item);
    }
    
    public void setTradeSession(TradeSession tradeSession) {
        this.tradeSession = tradeSession;
    }
    
    public boolean isTrading() {
        return tradeSession != null;
    }
    
    public TradeSession getTradeSession() {
        return tradeSession;
    }
    
    public void trackPlacement(int x, int y, Item item) {
        if(item.getUses().isEmpty() || !zone.areCoordinatesInBounds(x, y)) {
            return;
        }

        if(transmittableBlock != null && item.hasUse(ItemUseType.TRANSMITTED)) {
            transmitBlock(item, transmittableBlock, x, y);
            transmittableBlock = null;
            return;
        }

        boolean linked = false;
        
        if(lastPlacement != null) {
            if(item.hasUse(ItemUseType.SWITCHED) && !item.hasUse(ItemUseType.SWITCH)) {
                linked = tryLinkSwitchedItem(x, y, item);
            } else if(item.hasUse(ItemUseType.TRANSMITTED)) {
                linked = tryLinkTransmittedItem(x, y, item);
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
        
        if(pItem.hasUse(ItemUseType.SWITCH, ItemUseType.TRIGGER)) {
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
    
    private boolean tryLinkTransmittedItem(int x, int y, Item item) {
        int pX = lastPlacement.getX();
        int pY = lastPlacement.getY();
        Item pItem = lastPlacement.getItem();
        
        // Do nothing if the last placed item is not a transmitter
        if(!pItem.hasUse(ItemUseType.TRANSMIT)) {
            return false;
        }
        
        int maxTransmitDistance = getTotalSkillLevel(Skill.ENGINEERING) * 10;
        
        // Notify the player if the distance is beyond the maximum transmit distance
        if(!isGodMode() && !MathUtils.inRange(x, y, pX, pY, maxTransmitDistance)) {
            notify(String.format("You can only transmit %s blocks at your current engineering level.", maxTransmitDistance));
            return false;
        }
        
        MetaBlock metaBlock = zone.getMetaBlock(pX, pY);
        Map<String, Object> metadata = metaBlock == null ? null : metaBlock.getMetadata();
        
        // Do nothing if metadata is null for whatever reason
        if(metadata == null) {
            return false;
        }
        
        // Link transmitter to beacon
        MapHelper.appendList(metadata, ">", Arrays.asList(x, y)); // Make it a list for compatibility reasons
        zone.updateBlock(pX, pY, Layer.FRONT, pItem, 1, null, metadata);
        lastPlacement = null;
        return true;
    }

    public void transmitBlock(Item transmissionTarget, MetaBlock transmittableBlock, int x, int y) {
        if(!isGodMode() && !zone.isOwner(this)) {
            notify("You can't transmit this block because you don't own this zone.");
            inventory.addItem(transmissionTarget);
            return;
        }

        if(!zone.getBlock(transmittableBlock.getX(), transmittableBlock.getY()).getFrontItem().hasUse(ItemUseType.WORLD_MACHINE)) {
            notify("You can't transmit this item at the location you have set it. Maybe it got moved or destroyed since then.");
            inventory.addItem(transmissionTarget);
            return;
        }

        Item baseItem = zone.getBlock(x, y).getBaseItem();

        Item item = transmittableBlock.getItem();
        zone.updateBlock(transmittableBlock.getX(), transmittableBlock.getY(), Layer.FRONT, Item.AIR);

        if(baseItem.hasId("base/pipe")) {
            zone.updateBlock(x, y, Layer.FRONT, Item.AIR);
            notify("The machine has been flushed!");
        } else {
            zone.updateBlock(x, y, Layer.FRONT, item);
        }

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

        double accessoryBonus = getInventory().findAccessoryWithUse(ItemUseType.DOWSING).isAir() ? 1.0 : 2.0;

        return bonus.getChance() * getNormalizedSkill(bonus.getSkill()) * heldItem.getToolBonus() * accessoryBonus;
    }
    
    /**
     * @return The hash to be stored in blocks placed by this player.
     */
    public int getBlockHash() {
        return 1 + ((documentId.hashCode() & 2047) % 2047);
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
    
    protected void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public String getApiToken() {
        return apiToken;
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
    
    public List<String> getRecentZones() {
        return Collections.unmodifiableList(recentZones);
    }
    
    public void addZoneBookmark(Zone zone) {
        addZoneBookmark(zone.getDocumentId());
    }
    
    public void addZoneBookmark(String zone) {
        if(!isZoneBookmarked(zone)) {
            bookmarkedZones.add(0, zone); // Add at top for zone searcher
        }
    }
    
    public void removeZoneBookmark(Zone zone) {
        bookmarkedZones.remove(zone.getDocumentId());
    }
    
    public void removeZoneBookmark(String zone) {
        bookmarkedZones.remove(zone);
    }
    
    public boolean isZoneBookmarked(Zone zone) {
        return isZoneBookmarked(zone.getDocumentId());
    }
    
    public boolean isZoneBookmarked(String zone) {
        return bookmarkedZones.contains(zone);
    }
    
    public int getBookmarkedZoneCount() {
        return bookmarkedZones.size();
    }
    
    public List<String> getBookmarkedZones() {
        return Collections.unmodifiableList(bookmarkedZones);
    }
    
    public void followPlayer(Player player) {
        if(!followees.add(player.getDocumentId())) {
            return; // Do nothing if player is already following
        }

        player.addFollower(this);
        sendMessage(new FollowMessage(player, 0, true));
    }

    public void unfollowPlayer(Player player) {
        if(!followees.remove(player.getDocumentId())) {
            return; // Do nothing if player is not following
        }

        player.removeFollower(this);
        sendMessage(new FollowMessage(player, 0, false));
    }

    public boolean isFollowing(Player player) {
        return isFollowing(player.getDocumentId());
    }

    public boolean isFollowing(String followee) {
        return followees.contains(followee);
    }

    public Set<String> getFollowees() {
        return Collections.unmodifiableSet(followees);
    }

    private void addFollower(Player player) {
        followers.add(player.getDocumentId());

        if(isOnline()) {
            sendMessage(new FollowMessage(player, 1, true));
        }
    }

    private void removeFollower(Player player) {
        followers.remove(player.getDocumentId());

        if(isOnline()) {
            sendMessage(new FollowMessage(player, 1, false));
        }
    }

    public boolean hasFollower(Player player) {
        return hasFollower(player.getDocumentId());
    }

    public boolean hasFollower(String follower) {
        return followers.contains(follower);
    }

    public Set<String> getFollowers() {
        return Collections.unmodifiableSet(followers);
    }

    public void addLootCode(String lootCode) {
        lootCodes.add(lootCode);
    }
    
    public boolean hasLootCode(String lootCode) {
        return lootCodes.contains(lootCode);
    }
    
    public Set<String> getLootCodes() {
        return Collections.unmodifiableSet(lootCodes);
    }
    
    public void trackNameChange(String newName) {
        nameChanges.add(new NameChange(newName, name));
    }
    
    public List<NameChange> getNameChanges() {
        return nameChanges;
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
        kick(String.format("You have been banned: %s", reason), true);
    }
    
    public void unban() {
        unban(null);
    }
    
    public void unban(Player issuer) {
        PlayerRestriction currentBan = getCurrentBan();
        
        if(currentBan != null) {
            currentBan.pardon(issuer);
        }
        
        if(isOnline()) {
            changeZone(null);
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
            double zoneXpMultiplier = getZone() == null ? 1.0 : getZone().getXpMultiplier();
            setExperience((int) Math.round(experience + zoneXpMultiplier * amount), message);
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
            sendDelayedMessage(new StatMessage(PlayerStat.POINTS, skillPoints), 5000);
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
        sendMessage(new StatMessage(PlayerStat.POINTS, skillPoints));
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
        sendMessage(new StatMessage(PlayerStat.CROWNS, crowns));
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
                && achievementType == achievement.getClass()
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
            String message = String.format("%s has earned the %s achievement.", name, title);
            notifyPeers(message, NotificationType.SYSTEM);
            GameServer.getInstance().getPusher().handlePlayerMessage(this, message);

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

    public Map<String, Integer> getOrders() {
        return orders;
    }

    public String getDisplayedOrder() {
        return displayedOrder;
    }

    /** Get value for the entity status "ni" field. Use {@code Player#getIconEmoji} when sending a playerIconDidChange EventMessage */
    public String getIcon() {
        if(getDisplayedOrder() == null
                || !OrderManager.getOrders().containsKey(getDisplayedOrder())
                || orders.getOrDefault(getDisplayedOrder(), 0) == 0) {
            return null;
        }
        return String.format("orders/%s-%d",
                getDisplayedOrder(),
                getOrders().getOrDefault(getDisplayedOrder(), 0)
        );
    }

    /** Icon for sending a playerIconDidChange EventMessage. Use {@code Player#getIcon} for the entity status "ni" field. */
    public String getIconEmoji() {
        String icon = getIcon();
        return icon == null ? null : "emoji/" + icon;
    }

    public void setDisplayedOrder(String displayedOrder) {
        this.displayedOrder = displayedOrder;
    }

    public void randomizeAppearance() {
        appearance.putAll(Appearance.getRandomAppearance(this));
        zone.sendMessage(new EntityChangeMessage(id, getVisibleAppearance()));
    }
    
    public void updateAppearance(Map<String, Object> appearance) {
        this.appearance.putAll(appearance);
        zone.sendMessage(new EntityChangeMessage(id, getVisibleAppearance()));
        QuestEvents.handleAppearance(this, appearance);
    }
    
    public Map<String, Object> getAppearance() {
        return Collections.unmodifiableMap(appearance);
    }

    public Map<String, Object> getVisibleAppearance() {
        return zone.getHolographConfiguration().overrideAppearance(appearance);
    }

    public Map<String, QuestProgress> getQuestProgresses() {
        return questProgresses;
    }

    public ValueWithExpiry<List<Quest>> getDailyQuest() {
        return dailyQuest;
    }

    public Map<String, Quest> getAndroidQuests() {
        return androidQuests;
    }

    public void setDailyQuest(ValueWithExpiry<List<Quest>> dailyQuest) {
        this.dailyQuest = dailyQuest;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public void setSkillLevel(Skill skill, int level) {
        skills.put(skill, level);
        sendMessage(new SkillMessage(skill, level));
    }
    
    public int getTotalSkillLevel(Skill skill) {
        return getSkillLevel(skill) + inventory.getSkillBonus(skill);
    }
    
    public float getNormalizedSkill(Skill skill) {
        return getTotalSkillLevel(skill) / (float)MAX_SKILL_LEVEL;
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
        awardLoot(loot, dialogType, "You received:");
    }

    public void awardLoot(Loot loot, String title) {
        awardLoot(loot, DialogType.LOOT, title);
    }

    public void awardLoot(Loot loot, DialogType dialogType, String title) {
        Dialog dialog = new Dialog();
        DialogSection section = new DialogSection();
        dialog.addSection(section);
        
        loot.getItems().forEach((item, quantity) -> {
            inventory.addItem(item, quantity, true);
            QuestEvents.handleCollectItem(this, item, quantity);
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
            dialog.setTitle(title);
            showDialog(dialog.setType(dialogType));
        } else {
            section.setTitle(title);
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

    public long getLastLandmarkVoteAt() {
        return lastLandmarkVoteAt;
    }

    public void setLastLandmarkVoteAt(long lastLandmarkVoteAt) {
        this.lastLandmarkVoteAt = lastLandmarkVoteAt;
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
        config.put("appearance", getVisibleAppearance());
        config.put("settings", settings);
        config.put("ni", getIcon());
        config.put("api_token", apiToken);
        return config;
    }
}
