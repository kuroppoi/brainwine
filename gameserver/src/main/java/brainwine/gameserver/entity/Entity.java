package brainwine.gameserver.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import brainwine.gameserver.item.DamageType;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemUseType;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.server.Message;
import brainwine.gameserver.server.messages.EffectMessage;
import brainwine.gameserver.server.messages.EntityChangeMessage;
import brainwine.gameserver.server.messages.EntityStatusMessage;
import brainwine.gameserver.util.MapHelper;
import brainwine.gameserver.util.MathUtils;
import brainwine.gameserver.zone.Block;
import brainwine.gameserver.zone.MetaBlock;
import brainwine.gameserver.zone.Zone;

public abstract class Entity {
    
    public static final float DEFAULT_HEALTH = 5;
    public static final float POSITION_MODIFIER = 100F;
    public static final int VELOCITY_MODIFIER = (int)POSITION_MODIFIER;
    public static final int ATTACK_RETENTION_TIME = 2000;
    public static final int ATTACK_INVINCIBLE_TIME = 333;
    protected final Map<String, Object> properties = new HashMap<>();
    protected final List<Player> trackers = new ArrayList<>();
    protected final List<EntityAttack> recentAttacks = new ArrayList<>();
    protected int type;
    protected String name;
    protected float health = DEFAULT_HEALTH;
    protected int id;
    protected Zone zone;
    protected float x;
    protected float y;
    protected float velocityX;
    protected float velocityY;
    protected int blockX;
    protected int blockY;
    protected int lastBlockX;
    protected int lastBlockY;
    protected int targetX;
    protected int targetY;
    protected int sizeX = 1;
    protected int sizeY = 1;
    protected FacingDirection direction = FacingDirection.WEST;
    protected int animation;
    protected boolean invulnerable;
    protected EntityAttack lastAttack; // Used for tracking in entity deaths -- do not use this for anything else!
    protected long lastDamagedAt;
    
    public Entity(Zone zone) {
        this.zone = zone;
    }
    
    public void tick(float deltaTime) {
        long now = System.currentTimeMillis();
        
        // Update block position
        updateBlockPosition();
        
        // Clear expired recent attacks
        recentAttacks.removeIf(attack -> now >= attack.getTime() + ATTACK_RETENTION_TIME);
    }
    
    public void die(EntityAttack cause) {
        // Override
    }
    
    public void heal(float amount) {
        if(health > 0) {
            setHealth(health + amount);
        }
    }
    
    public void attack(Entity attacker, Item weapon, float baseDamage, DamageType damageType) {
        attack(attacker, weapon, baseDamage, damageType, false);
    }
    
    public void attack(Entity attacker, Item weapon, float baseDamage, DamageType damageType, boolean trueDamage) {
        // Ignore attack if entity is dead or invulnerable
        if(isDead() || isInvulnerable()) {
            return;
        }
        
        // Ignore attack if there is no damage to deal
        if(baseDamage <= 0 || damageType == null || damageType == DamageType.NONE) {
            return;
        }
        
        EntityAttack attack = new EntityAttack(attacker, weapon, baseDamage, damageType);
        recentAttacks.add(attack);
        lastAttack = attack;
        lastDamagedAt = System.currentTimeMillis();
        
        // Kill entity if attacker is a player in god mode
        if(attacker != null && attacker.isPlayer() && ((Player)attacker).isGodMode()) {
            setHealth(0.0F);
            return;
        }
        
        // Ignore multipliers if true damage should be dealt
        if(trueDamage) {
            setHealth(health - baseDamage);
            return;
        }
        
        float attackMultiplier = attacker != null ? Math.max(0.0F, attacker.getAttackMultiplier(attack)) : 1.0F;
        float defense = Math.max(0.0F, 1.0F - getDefense(attack));
        float damage = baseDamage * attackMultiplier * defense;
        setHealth(health - damage);
    }
    
    public float getAttackMultiplier(EntityAttack attack) {
        return 1.0F; // Override
    }
    
    public float getDefense(EntityAttack attack) {
        return 1.0F; // Override
    }
    
    public void spawnEffect(String type) {
        spawnEffect(type, 1);
    }
    
    public void spawnEffect(String type, Object data) {
        float effectX = x + sizeX / 2.0F;
        float effectY = y + sizeY / 2.0F;
        sendMessageToTrackers(new EffectMessage(effectX, effectY, type, data));
    }
    
    public void emote(String message) {
        float effectX = x + sizeX / 2.0F;
        float effectY = y - sizeY + 1;
        sendMessageToTrackers(new EffectMessage(effectX, effectY, "emote", message));
    }
    
    public void updateBlockPosition() {
        lastBlockX = blockX;
        lastBlockY = blockY;
        blockX = (int)x;
        blockY = (int)y;
        
        // Check if block position has changed
        if(lastBlockX != blockX || lastBlockY != blockY) {
            blockPositionChanged();
        }
    }
    
    public void blockPositionChanged() {
        // Check for touchplates
        if(zone != null && zone.isChunkLoaded(blockX, blockY)) {
            MetaBlock metaBlock = zone.getMetaBlock(blockX, blockY);
            Block block = zone.getBlock(blockX, blockY);
            Item item = block.getFrontItem();
            int mod = block.getFrontMod();
            
            // Trigger a switch interaction if the entity stepped on a touchplate
            if(item.hasUse(ItemUseType.TRIGGER)) {
                ItemUseType.SWITCH.getInteraction().interact(zone, this, blockX, blockY, Layer.FRONT, item, mod, metaBlock, null, null);
            }
        }
    }
    
    public boolean canSee(Entity other) {
        return canSee((int)other.getX(), (int)other.getY());
    }
    
    public boolean canSee(int x, int y) {
        return zone.isPointVisibleFrom((int)this.x, (int)this.y, x, y) ||
                (y > 0 && zone.isPointVisibleFrom((int)this.x, (int)this.y, x, y - 1));
    }
    
    public boolean inRange(Entity other, float range) {
        return inRange(other.getX(), other.getY(), range);
    }
    
    public boolean inRange(float x, float y, double range) {
        return MathUtils.inRange(this.x, this.y, x, y, range);
    }
    
    public void setProperty(String key, Object value) {
        setProperty(key, value, false);
    }
    
    public void setProperty(String key, Object value, boolean sendMessage) {
        setProperties(MapHelper.map(key, value), sendMessage);
    }
    
    public void setProperties(Map<String, Object> properties) {
        setProperties(properties, false);
    }
    
    public void setProperties(Map<String, Object> properties, boolean sendMessage) {
        properties.forEach((key, value) -> {
            if(value == null) {
                this.properties.remove(key);
            } else {
                this.properties.put(key, value);
            }
        });
        
        if(sendMessage) {
            sendMessageToTrackers(new EntityChangeMessage(id, properties));
        }
    }
    
    public void sendMessageToTrackers(Message message) {
        for(Player tracker : trackers) {
            tracker.sendMessage(message);
        }
    }
    
    public void addTracker(Player tracker) {
        trackers.add(tracker);
    }
    
    public void removeTracker(Player tracker) {
        trackers.remove(tracker);
    }
    
    public List<Player> getTrackers() {
        return trackers;
    }
    
    public boolean wasAttackedRecently(Entity entity, int delay) {
        return recentAttacks.stream().filter(attack -> attack.getAttacker() == entity && System.currentTimeMillis() < attack.getTime() + delay).findFirst().isPresent();
    }
    
    public EntityAttack getMostRecentAttack() {
        return recentAttacks.isEmpty() ? null : recentAttacks.get(recentAttacks.size() - 1);
    }
    
    public List<EntityAttack> getRecentAttacks() {
        return Collections.unmodifiableList(recentAttacks);
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getId() {
        return id;
    }
    
    public int getType() {
        return type;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public float getMaxHealth() {
        return DEFAULT_HEALTH;
    }
    
    public boolean isDead() {
        return health <= 0;
    }
    
    public void setHealth(float health) {
        float maxHealth = getMaxHealth();
        this.health = health < 0 ? 0 : health > maxHealth ? maxHealth : health;
        
        if(this.health <= 0.0F) {
            die(lastAttack);
        }
        
        lastAttack = null;
    }
    
    public float getHealth() {
        return health;
    }
    
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        updateBlockPosition();
    }
    
    public float getX() {
        return x;
    }
    
    public float getY() {
        return y;
    }
    
    public void setVelocity(float velocityX, float velocityY) {
        this.velocityX = velocityX;
        this.velocityY = velocityY;
    }
    
    public float getVelocityX() {
        return velocityX;
    }
    
    public float getVelocityY() {
        return velocityY;
    }
    
    public void setTarget(int targetX, int targetY) {
        this.targetX = targetX;
        this.targetY = targetY;
    }
    
    public int getTargetX() {
        return targetX;
    }
    
    public int getTargetY() {
        return targetY;
    }
    
    public int getBlockX() {
        return blockX;
    }
    
    public int getBlockY() {
        return blockY;
    }
    
    public int getSizeX() {
        return sizeX;
    }
    
    public int getSizeY() {
        return sizeY;
    }
    
    public void setDirection(int direction) {
        setDirection(direction > 0 ? FacingDirection.EAST : direction < 0 ? FacingDirection.WEST : this.direction);
    }
    
    public void setDirection(FacingDirection direction) {
        this.direction = direction;
    }
    
    public FacingDirection getDirection() {
        return direction;
    }
    
    public void setAnimation(int animation) {
        this.animation = animation;
    }
    
    public int getAnimation() {
        return animation;
    }
    
    public void setInvulnerable(boolean invulnerable) {
        this.invulnerable = invulnerable;
    }
    
    public boolean isInvulnerable() {
        return invulnerable;
    }
    
    public void setZone(Zone zone) {
        this.zone = zone;
    }
    
    public Zone getZone() {
        return zone;
    }
    
    public final boolean isPlayer() {
        return this instanceof Player; // Not very OOP
    }
    
    /**
     * @return A {@link Map} containing all the data necessary for use in {@link EntityStatusMessage}.
     */
    public Map<String, Object> getStatusConfig() {
        Map<String, Object> config = new HashMap<>();
        config.putAll(properties);
        return config;
    }
}
