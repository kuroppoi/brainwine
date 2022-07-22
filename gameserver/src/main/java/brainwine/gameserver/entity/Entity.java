package brainwine.gameserver.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.server.messages.EntityStatusMessage;
import brainwine.gameserver.util.MathUtils;
import brainwine.gameserver.zone.Zone;

public abstract class Entity {
    
    public static final float POSITION_MODIFIER = 100F;
    public static final int VELOCITY_MODIFIER = (int)POSITION_MODIFIER;
    protected final List<Player> trackers = new ArrayList<>();
    protected int type;
    protected String name;
    protected float health;
    protected int id;
    protected Zone zone;
    protected float x;
    protected float y;
    protected float velocityX;
    protected float velocityY;
    protected int targetX;
    protected int targetY;
    protected FacingDirection direction = FacingDirection.WEST;
    protected int animation;
    
    public Entity(Zone zone) {
        this.zone = zone;
        health = 10; // TODO
    }
    
    public void tick(float deltaTime) {
        // Override
    }
    
    public void die(Player killer) {
        // Override
    }
    
    public void damage(float amount) {
        damage(amount, null);
    }
    
    public void damage(float amount, Player attacker) {
        setHealth(health - amount);
        
        if(health <= 0) {
            die(attacker);
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
    
    public boolean inRange(float x, float y, float range) {
        return MathUtils.inRange(this.x, this.y, x, y, range);
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
    
    public boolean isDead() {
        return health <= 0;
    }
    
    public void setHealth(float health) {
        this.health = health < 0 ? 0 : health;
    }
    
    public float getHealth() {
        return health;
    }
    
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
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
    
    public void setZone(Zone zone) {
        this.zone = zone;
    }
    
    public Zone getZone() {
        return zone;
    }
    
    /**
     * @return A {@link Map} containing all the data necessary for use in {@link EntityStatusMessage}.
     */
    public Map<String, Object> getStatusConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("name", name);
        config.put("h", health);
        return config;
    }
}
