package brainwine.gameserver.zone;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;

import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.entity.EntityConfig;
import brainwine.gameserver.entity.EntityRegistry;
import brainwine.gameserver.entity.EntityStatus;
import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.item.ModType;
import brainwine.gameserver.server.messages.EffectMessage;
import brainwine.gameserver.server.messages.EntityPositionMessage;
import brainwine.gameserver.server.messages.EntityStatusMessage;
import brainwine.gameserver.util.MapHelper;
import brainwine.gameserver.util.ResourceUtils;
import brainwine.gameserver.util.Vector2i;
import brainwine.gameserver.util.WeightedMap;
import brainwine.shared.JsonHelper;

public class EntityManager {
    
    public static final long ENTITY_CLEAR_TIME = 10000;
    public static final long SPAWN_INTERVAL = 200;
    private static final Logger logger = LogManager.getLogger();
    private static final ThreadLocalRandom random = ThreadLocalRandom.current();
    private static final Map<Biome, List<EntitySpawn>> spawns = new HashMap<>();
    private final Map<Integer, Entity> entities = new HashMap<>();
    private final Map<Integer, Npc> npcs = new HashMap<>();
    private final Map<Integer, Player> players = new HashMap<>();
    private final Map<String, Player> playersByName = new HashMap<>();
    private final Zone zone;
    private int entityDiscriminator;
    private long lastSpawnAt = System.currentTimeMillis();
    
    public EntityManager(Zone zone) {
        this.zone = zone;
    }
    
    public static void loadEntitySpawns() {
        spawns.clear();
        logger.info("Loading entity spawns ...");
        File file = new File("spawning.json");
        ResourceUtils.copyDefaults("spawning.json");
        
        if(file.isFile()) {
            try {
                Map<Biome, List<EntitySpawn>> loot = JsonHelper.readValue(file, new TypeReference<Map<Biome, List<EntitySpawn>>>(){});
                spawns.putAll(loot);
            } catch (IOException e) {
                logger.error("Failed to load entity spawns", e);
            }
        }
    }
    
    private static EntityConfig getRandomEligibleEntity(Biome biome, String locale, double depth, Item baseItem) {
        WeightedMap<EntityConfig> eligibleEntities = new WeightedMap<>();
        
        if(spawns.containsKey(biome)) {
            spawns.get(biome).stream().filter(spawn -> locale.equalsIgnoreCase(spawn.getLocale())
                    && depth >= spawn.getMinDepth() && depth <= spawn.getMaxDepth()
                    && ((baseItem.getId() != 5 && baseItem.getId() != 6) || spawn.getOrifice() == baseItem))
                .forEach(spawn -> eligibleEntities.addEntry(spawn.getEntity(), spawn.getFrequency()));
        }
        
        return eligibleEntities.next();
    }
    
    public void tick(float deltaTime) {
        clearEntities();
        
        for(Entity entity : getEntities()) {
            entity.tick(deltaTime);
        }
        
        long now = System.currentTimeMillis();
        
        if(now > lastSpawnAt + SPAWN_INTERVAL &&
                !players.isEmpty() && getTransientNpcCount() < Math.min(64, players.size() * 8)) {
            spawnRandomEntity();
            lastSpawnAt = now;
        }
    }
    
    private void spawnRandomEntity() {
        boolean immediate = random.nextDouble() < 0.75;
        List<Chunk> visibleChunks = zone.getVisibleChunks();
        List<Chunk> chunks = immediate ? visibleChunks : zone.getLoadedChunks().stream()
                .filter(chunk -> !visibleChunks.contains(chunk)).collect(Collectors.toList());
        
        if(!chunks.isEmpty()) {
            List<Vector2i> eligiblePositions = new ArrayList<>();
            Chunk chunk = chunks.get(random.nextInt(chunks.size()));
            
            for(int x = chunk.getX(); x < chunk.getX() +  chunk.getWidth(); x++) {
                for(int y = chunk.getY(); y < chunk.getY() + chunk.getHeight(); y++) {
                    Block block = chunk.getBlock(x, y);
                    int base = block.getBaseItem().getId();
                    
                    if((immediate && base == 5 || base == 6) || 
                            (!immediate && block.getBackItem().isAir() && block.getFrontItem().isAir())) {
                        eligiblePositions.add(new Vector2i(x, y));
                    }
                }
            }
            
            if(!eligiblePositions.isEmpty()) {
                Vector2i position = eligiblePositions.get(random.nextInt(eligiblePositions.size()));
                int x = position.getX();
                int y = position.getY();
                Block block = chunk.getBlock(x, y);
                String locale = block.getBaseItem().isAir() ? "sky" : "cave";
                EntityConfig entity = getRandomEligibleEntity(zone.getBiome(), locale, y / (double)zone.getHeight(), block.getBaseItem());
                
                if(immediate) {
                    if(tryBustOrifice(x, y, Layer.BACK) || tryBustOrifice(x, y, Layer.FRONT)) {
                        return;
                    }
                }
                
                if(entity != null) {
                    spawnEntity(new Npc(zone, entity), x, y);
                }
            }
        }
    }
    
    private boolean tryBustOrifice(int x, int y, Layer layer) {
        Block block = zone.getBlock(x, y);
        Item item = block.getItem(layer);
        int mod = block.getMod(layer);
        
        if(!item.isAir()) {
            if(!zone.isBlockProtected(x, y, null) && random.nextBoolean()) {
                item = item.getMod() == ModType.DECAY && mod < 5 ? item : Item.AIR;
                mod = item.isAir() ? 0 : Math.min(5, mod + random.nextInt(1, 3));
                zone.updateBlock(x, y, layer, item, mod);
            }
            
            return true;
        }
        
        return false;
    }
    
    private void clearEntities() {
        List<Npc> clearableEntities = new ArrayList<>();
        
        for(Npc npc : npcs.values()) {
            if(npc.isDead() || !zone.isChunkLoaded((int)npc.getX(), (int)npc.getY()) || 
                    (npc.isTransient() && System.currentTimeMillis() > npc.getLastTrackedAt() + ENTITY_CLEAR_TIME)) {
                clearableEntities.add(npc);
            }
        }
        
        for(Npc npc : clearableEntities) {
            removeEntity(npc);
        }
    }
    
    public List<Entity> getEntitiesInRange(float x, float y, float range) {
        return getEntities().stream().filter(entity -> entity.inRange(x, y, range)).collect(Collectors.toList());
    }
    
    public Player getRandomPlayerInRange(float x, float y, float range) {
        List<Player> players = getPlayersInRange(x, y, range);
        return players.isEmpty() ? null : players.get(random.nextInt(players.size()));
    }
    
    public List<Player> getPlayersInRange(float x, float y, float range) {
        return getPlayers().stream().filter(player -> player.inRange(x, y, range)).collect(Collectors.toList());
    }
    
    public void trySpawnBlockEntity(int x, int y) {
        if(!zone.areCoordinatesInBounds(x, y)) {
            return;
        }
        
        Item item = zone.getBlock(x, y).getFrontItem();
        
        // Check for guardian entity
        if(item.getGuardLevel() > 0) {
            MetaBlock metaBlock = zone.getMetaBlock(x, y);
            
            if(metaBlock != null) {
                List<String> guardians = MapHelper.getList(metaBlock.getMetadata(), "!", Collections.emptyList());
                
                for(String guardian : guardians) {
                    EntityConfig config = EntityRegistry.getEntityConfig(guardian);
                    
                    if(config != null) {
                        Npc entity = new Npc(zone, config);
                        entity.setGuardBlock(x, y);
                        spawnEntity(entity, x, y);
                    }
                }
            }
        }
        
        // Check for mounted entity (turrets & geysers)
        if(item.isEntity()) {
            EntityConfig config = EntityRegistry.getEntityConfig(item.getName());
            
            if(config != null) {
                Npc entity = new Npc(zone, config);
                entity.setMountBlock(x, y);
                spawnEntity(entity, x, y);
            }
        }
    }
    
    public void spawnEntity(Entity entity, int x, int y) {
        spawnEntity(entity, x, y, false);
    }
    
    public void spawnEntity(Entity entity, int x, int y, boolean effect) {
        if(zone.isChunkLoaded(x, y)) {
            addEntity(entity);
            entity.setPosition(x, y);
            
            if(effect) {
                zone.sendMessageToChunk(new EffectMessage(x + 0.5F, y + 0.5F, "bomb-teleport", 4), zone.getChunk(x, y));
            }
        }
    }
    
    public void addEntity(Entity entity) {
        if(entities.containsValue(entity)) {
            removeEntity(entity);
        }
        
        int entityId = ++entityDiscriminator;
        entity.setZone(zone);
        entity.setId(entityId);
        
        if(entity instanceof Player) {
            Player player = (Player)entity;
            player.onZoneChanged();
            players.put(entityId, player);
            playersByName.put(player.getName(), player);
            player.sendMessageToPeers(new EntityStatusMessage(player, EntityStatus.ENTERING));
            player.sendMessageToPeers(new EntityPositionMessage(player));
        } else if(entity instanceof Npc) {
            npcs.put(entityId, (Npc)entity);
        }
        
        entities.put(entityId, entity);
    }
    
    public void removeEntity(Entity entity) {
        int entityId = entity.getId();
        
        if(!entities.remove(entityId, entity)) {
            return;
        }
        
        if(entity instanceof Player) {
            players.remove(entityId);
            playersByName.remove(entity.getName());
            zone.sendMessage(new EntityStatusMessage(entity, EntityStatus.EXITING));
        } else {
            npcs.remove(entityId);
        }
    }
    
    public Entity getEntity(int entityId) {
        return entities.get(entityId);
    }
    
    public int getEntityCount() {
        return entities.size();
    }
    
    public Collection<Entity> getEntities() {
        return Collections.unmodifiableCollection(entities.values());
    }
    
    public Npc getNpc(int entityId) {
        return npcs.get(entityId);
    }
    
    public int getNpcCount() {
        return npcs.size();
    }
    
    public int getTransientNpcCount() {
        return (int)npcs.values().stream().filter(npc -> npc.isTransient()).count();
    }
    
    public Collection<Npc> getNpcs() {
        return Collections.unmodifiableCollection(npcs.values());
    }
    
    public Player getPlayer(int entityId) {
        return players.get(entityId);
    }
    
    public Player getPlayer(String name) {
        return playersByName.get(name);
    }
    
    public int getPlayerCount() {
        return players.size();
    }
    
    public Collection<Player> getPlayers() {
        return Collections.unmodifiableCollection(players.values());
    }
}
