package brainwine.gameserver.entity;

import java.util.HashMap;
import java.util.Map;

import brainwine.gameserver.server.messages.EntityStatusMessage;
import brainwine.gameserver.zone.Zone;

public abstract class Entity {
    
    public static final float POSITION_MODIFIER = 100F;
    public static final int VELOCITY_MODIFIER = (int)POSITION_MODIFIER;
    private static int discriminator;
    protected String name;
    protected float health;
    protected float steam;
    protected final int id;
    protected Zone zone;
    protected float x;
    protected float y;
    protected int velocityX;
    protected int velocityY;
    protected int targetX;
    protected int targetY;
    protected FacingDirection direction = FacingDirection.WEST;
    protected int animation;
    protected Boolean admin;
    
    public Entity(Zone zone) {
        this.id = ++discriminator;
        this.zone = zone;
        health = 10; // TODO
    }
    
    public abstract EntityType getType();
    
    public void tick() {
        
    }
    
    public int getId() {
        return id;
    }
    
    public void setAdmin(Boolean admin) {
    	this.admin = admin;
    }
    
    public Boolean getAdmin() {
    	return this.admin;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    public boolean isDead() {
        return this.health <= 0;
    }
    
    public void setHealth(float health) {
        this.health = health;
    }
    
    public float getHealth() {
        return health;
    }
    
    public void setSteam(float steam) {
    	this.steam = steam;
    }
    
    public float getSteam() {
    	return steam;
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
    
    public void setVelocity(int velocityX, int velocityY) {
        this.velocityX = velocityX;
        this.velocityY = velocityY;
    }
    
    public int getVelocityX() {
        return velocityX;
    }
    
    public int getVelocityY() {
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
