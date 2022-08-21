package brainwine.gameserver.entity.npc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.behavior.SequenceBehavior;
import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.entity.EntityConfig;
import brainwine.gameserver.entity.EntityLoot;
import brainwine.gameserver.entity.EntityRegistry;
import brainwine.gameserver.entity.FacingDirection;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.item.DamageType;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.server.messages.EntityChangeMessage;
import brainwine.gameserver.util.MapHelper;
import brainwine.gameserver.util.Pair;
import brainwine.gameserver.util.Vector2i;
import brainwine.gameserver.util.WeightedMap;
import brainwine.gameserver.zone.MetaBlock;
import brainwine.gameserver.zone.Zone;

public class Npc extends Entity {
    
    public static final int ATTACK_RETENTION_TIME = 2000;
    public static final int ATTACK_INVINCIBLE_TIME = 333;
    private final EntityConfig config;
    private final String typeName;
    private final float maxHealth;
    private final float baseSpeed;
    private final Vector2i size;
    private final WeightedMap<EntityLoot> loot;
    private final WeightedMap<EntityLoot> placedLoot;
    private final Map<Item, WeightedMap<EntityLoot>> lootByWeapon;
    private final Map<DamageType, Float> resistances;
    private final Map<DamageType, Float> weaknesses;
    private final List<String> animations;
    private final SequenceBehavior behaviorTree;
    private final Map<String, Object> properties = new HashMap<>();
    private final Map<DamageType, Float> activeDefenses = new HashMap<>();
    private final Map<Player, Pair<Item, Long>> recentAttacks = new HashMap<>();
    private float speed;
    private int moveX;
    private int moveY;
    private Vector2i guardBlock;
    private Vector2i mountBlock;
    private Entity target;
    private long lastBehavedAt = System.currentTimeMillis();
    private long lastTrackedAt = System.currentTimeMillis();
    
    public Npc(Zone zone, EntityConfig config) {
        super(zone);
        
        // Add components & merge relevant configurations if applicable
        List<Map<String, Object>> behavior = new ArrayList<>(config.getBehavior());
        Map<String, String> attachments = new HashMap<>(config.getAttachments());
        Map<String, String[]> components = config.getComponents();
        
        // TODO to what extent should we merge the configurations with components?
        // Merging everything is kind of a pain and seems to be overdoing it anyway...
        
        if(!components.isEmpty()) {
            List<Integer> selectedComponents = new ArrayList<>();
            
            for(Entry<String, String[]> entry : components.entrySet()) {
                String[] pool = entry.getValue();
                EntityConfig componentConfig = pool.length > 0 ? 
                        EntityRegistry.getEntityConfig(pool[ThreadLocalRandom.current().nextInt(pool.length)]) : null;
                
                if(componentConfig != null) {
                    behavior.addAll(componentConfig.getBehavior());
                    attachments.putAll(componentConfig.getAttachments());
                    selectedComponents.add(componentConfig.getType());
                }
            }
            
            properties.put("C", selectedComponents);
        }
        
        // Set attachments if applicable
        if(!attachments.isEmpty()) {
            Map<Integer, Object> slots = new HashMap<>();
            
            for(Entry<String, String> entry : attachments.entrySet()) {
                String slot = entry.getKey();
                String attachment = entry.getValue();
                
                if(attachment != null) {
                    slots.put(config.getSlots().indexOf(slot), config.getAttachmentTypes().indexOf(attachment));
                }
            }
            
            properties.put("sl", slots);
        }
        
        this.config = config;
        this.typeName = config.getName();
        this.type = config.getType();
        this.maxHealth = config.getMaxHealth();
        this.baseSpeed = config.getBaseSpeed();
        this.size = config.getSize();
        this.loot = config.getLoot();
        this.placedLoot = config.getPlacedLoot();
        this.lootByWeapon = config.getLootByWeapon();
        this.resistances = config.getResistances();
        this.weaknesses = config.getWeaknesses();
        this.animations = config.getAnimations();
        this.behaviorTree = SequenceBehavior.createBehaviorTree(this, behavior);
        this.direction = Math.random() < 0.5 ? FacingDirection.WEST : FacingDirection.EAST;
        health = maxHealth;
        speed = baseSpeed * (float)(0.9 + Math.random() * 0.2);
    }
    
    @Override
    public void tick(float deltaTime) {
        long now = System.currentTimeMillis();
        
        // Clear expired recent attacks
        recentAttacks.values().removeIf(attack -> now >= attack.getLast() + ATTACK_RETENTION_TIME);
        
        // Tick behavior when it is ready
        if(now >= lastBehavedAt + (int)(1000 / speed)) {
            lastBehavedAt = now;
            behaviorTree.behave();
            
            if(moveX != 0 || moveY != 0) {
                setPosition(x + moveX, y + moveY);
                setVelocity(moveX * speed, moveY * speed);
                moveX = 0;
                moveY = 0;
            }
            
            setPosition(Math.round(x), Math.round(y));
        }
        
        if(!trackers.isEmpty()) {
            lastTrackedAt = System.currentTimeMillis();
        }
    }
    
    @Override
    public void die(Player killer) {
        // Grant loot & track kill
        if(killer != null) {
            if(!isPlayerPlaced()) {
                // Track assists
                for(Player attacker : recentAttacks.keySet()) {
                    if(attacker != killer) {
                        attacker.getStatistics().trackAssist(config);
                    }
                }
                
                killer.getStatistics().trackKill(config);
            }
            
            EntityLoot loot = getRandomLoot(killer);
            
            if(loot != null) {
                Item item = loot.getItem();
                
                if(!item.isAir()) {
                    killer.getInventory().addItem(item, loot.getQuantity(), true);
                }
            }
        }
        
        // Remove itself from the guard block metadata if it was guarding one
        if(isGuard()) {
            MetaBlock metaBlock = zone.getMetaBlock(guardBlock.getX(), guardBlock.getY());
            
            if(metaBlock != null) {
                MapHelper.getList(metaBlock.getMetadata(), "!", Collections.emptyList()).remove(typeName);
            }
        }
        
        // Destroy mount block if it has one
        if(isMounted()) {
            zone.updateBlock(mountBlock.getX(), mountBlock.getY(), Layer.FRONT, 0);
        }
    }
    
    @Override
    public float getMaxHealth() {
        return maxHealth;
    }
    
    @Override
    public Map<String, Object> getStatusConfig() {
        Map<String, Object> config = super.getStatusConfig();
        config.putAll(properties);
        
        if(isDead()) {
            config.put("!", "v");
        }
        
        return config;
    }
    
    public void move(int x, int y) {
        move(x, y, baseSpeed);
    }
    
    public void move(int x, int y, float speed) {
        move(x, y, speed, null);
    }
    
    public void move(int x, int y, String animation) {
        move(x, y, baseSpeed, animation);
    }
    
    public void move(int x, int y, float speed, String animation) {
        this.speed = speed;
        direction = x > 0 ? FacingDirection.EAST : x < 0 ? FacingDirection.WEST : direction;
        moveX = x;
        moveY = y;
        
        if(animation != null) {
            setAnimation(animation);
        }
    }
    
    public EntityConfig getConfig() {
        return config;
    }
    
    public void attack(Player attacker, Item weapon) {
        // Prevent damage if this entity is mounted and its mount is protected
        if(isMounted() && zone.isBlockProtected(mountBlock.getX(), mountBlock.getY(), attacker)) {
            return;
        }
        
        Pair<Item, Long> recentAttack = recentAttacks.get(attacker);
        long now = System.currentTimeMillis();
        
        // Reject the attack if the player already attacked this entity recently
        if(recentAttack != null && now < recentAttack.getLast() + ATTACK_INVINCIBLE_TIME) {
            return;
        }
        
        damage(calculateDamage(weapon.getDamage() / 4, weapon.getDamageType()), attacker); // TODO change weapon damage in config
        recentAttacks.put(attacker, new Pair<>(weapon, now));
    }
    
    public float calculateDamage(float baseDamage, DamageType type) {
        return baseDamage * (1 - getDefense(type));
    }
    
    public Collection<Pair<Item, Long>> getRecentAttacks() {
        return Collections.unmodifiableCollection(recentAttacks.values());
    }
    
    public void setDefense(DamageType type, float amount) {
        if(amount == 0) {
            activeDefenses.remove(type);
        } else {
            activeDefenses.put(type, amount);
        }
    }
    
    public float getDefense(DamageType type) {
        return getDefense(type, true);
    }
    
    public float getDefense(DamageType type, boolean includeBaseDefense) {
        return (includeBaseDefense ? getBaseDefense(type) : 0) + activeDefenses.getOrDefault(type, 0F);
    }
    
    public boolean isTransient() {
        return !isGuard() && !isMounted();
    }
    
    public void setProperty(String key, Object value) {
        if(value == null) {
            properties.remove(key);
        } else {
            properties.put(key, value);
        }
        
        for(Player tracker : trackers) {
            tracker.sendMessage(new EntityChangeMessage(id, MapHelper.map(key, value)));
        }
    }
    
    public EntityLoot getRandomLoot(Player awardee) {
        Item weapon = awardee.getHeldItem();
        
        if(isOwnedBy(awardee)) {
            return placedLoot.next();
        } else if(lootByWeapon.containsKey(weapon)) {
            return lootByWeapon.get(weapon).next();
        } else {
            return loot.next();
        }
    }
    
    public float getBaseDefense(DamageType type) {
        return resistances.getOrDefault(type, 0F) - weaknesses.getOrDefault(type, 0F);
    }
    
    public void setGuardBlock(int x, int y) {
        setGuardBlock(new Vector2i(x, y));
    }
    
    public void setGuardBlock(Vector2i guardBlock) {
        this.guardBlock = guardBlock;
    }
    
    public boolean isGuard() {
        return guardBlock != null;
    }
    
    public Vector2i getGuardBlock() {
        return guardBlock;
    }
    
    public void setMountBlock(int x, int y) {
        setMountBlock(new Vector2i(x, y));
    }
    
    public void setMountBlock(Vector2i mountBlock) {
        this.mountBlock = mountBlock;
    }
    
    public boolean isMounted() {
        return mountBlock != null;
    }
    
    public Vector2i getMountBlock() {
        return mountBlock;
    }
    
    public boolean isPlayerPlaced() {
        return getOwner() != null;
    }
    
    public boolean isOwnedBy(Player player) {
        return player == getOwner();
    }
    
    public Player getOwner() {
        return GameServer.getInstance().getPlayerManager().getPlayerById(getOwnerId());
    }
    
    private String getOwnerId() {
        MetaBlock metaBlock = isMounted() ? zone.getMetaBlock(mountBlock.getX(), mountBlock.getY()) : null;
        
        if(metaBlock != null) {
            return metaBlock.getOwner();
        }
        
        return null;
    }
    
    public void setTarget(Entity target) {
        this.target = target;
    }
    
    public boolean hasTarget() {
        return target != null;
    }
    
    public Entity getTarget() {
        return target;
    }
    
    public void setSpeed(float speed) {
        this.speed = speed;
    }
    
    public float getSpeed() {
        return speed;
    }
    
    public float getBaseSpeed() {
        return baseSpeed;
    }
    
    public int getMoveX() {
        return moveX;
    }
    
    public int getMoveY() {
        return moveY;
    }
    
    public long getLastTrackedAt() {
        return lastTrackedAt;
    }
    
    public boolean isBlocked(int oX, int oY) {
        int x = (int)this.x;
        int y = (int)this.y;
        int tX = x + oX;
        int tY = y + oY;
        boolean blocked = zone.isBlockSolid(tX, tY) || (oX != 0 && zone.isBlockSolid(tX, y)) || (oY != 0 && zone.isBlockSolid(x, tY));
        
        if(size.getX() > 1) {
            int additionalWidth = size.getX() - 1;
            blocked = blocked || zone.isBlockSolid(tX + additionalWidth, tY) 
                    || (oX != 0 && zone.isBlockSolid(tX + additionalWidth, y)) 
                    || (oY != 0 && zone.isBlockSolid(x + additionalWidth, tY));
        }
        
        if(size.getY() > 1) {
            int additionalHeight = size.getY() - 1;
            blocked = blocked || zone.isBlockSolid(tX, tY - additionalHeight) 
                    || (oX != 0 && zone.isBlockSolid(tX, y - additionalHeight)) 
                    || (oY != 0 && zone.isBlockSolid(x, tY - additionalHeight));
        }
        
        return blocked;
    }
    
    public boolean isOnGround() {
        return isOnGround(0);
    }
    
    public boolean isOnGround(int diagonal) {
        return isBlocked(0, 1) || (diagonal != 0 && isBlocked(diagonal, 1));
    }
    
    public void setAnimation(String name) {
        int index = animations.indexOf(name);
        setAnimation(index == -1 ? 0 : index);
    }
}
