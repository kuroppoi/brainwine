package brainwine.gameserver.zone;

import static brainwine.shared.LogMarkers.SERVER_MARKER;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
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
import brainwine.gameserver.entity.npc.NpcData;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.item.ModType;
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
    private final Map<Integer, Entity> entities = new ConcurrentHashMap<>(); // TODO is there a better solution?
    private final Map<Integer, Npc> npcs = new HashMap<>();
    private final Map<Integer, Npc> mountedNpcs = new HashMap<>();
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
        logger.info(SERVER_MARKER, "Loading entity spawns ...");
        File file = new File("spawning.json");
        ResourceUtils.copyDefaults("spawning.json");
        
        if(file.isFile()) {
            try {
                spawns.putAll(JsonHelper.readValue(file, new TypeReference<Map<Biome, List<EntitySpawn>>>(){}));
            } catch (IOException e) {
                logger.error(SERVER_MARKER, "Failed to load entity spawns", e);
            }
        }
    }
    
    private static List<EntitySpawn> getEligibleEntitySpawns(Biome biome, String locale, double depth, Item baseItem) {
        return spawns.entrySet().stream()
                .filter(entry -> entry.getKey() == biome)
                .map(Entry::getValue)
                .flatMap(Collection::stream)
                .filter(spawn -> locale.equalsIgnoreCase(spawn.getLocale())
                        && depth >= spawn.getMinDepth() && depth <= spawn.getMaxDepth()
                        && ((!baseItem.hasId("base/maw") && !baseItem.hasId("base/pipe")) || spawn.getOrifice() == baseItem))
                .collect(Collectors.toList());
    }
    
    private static EntitySpawn getRandomEligibleEntitySpawn(Biome biome, String locale, double depth, Item baseItem) {
        return new WeightedMap<>(getEligibleEntitySpawns(biome, locale, depth, baseItem), EntitySpawn::getFrequency).next();
    }
    
    public void tick(float deltaTime) {
        if(!npcs.isEmpty()) {
            clearEntities();
        }
        
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
                    Item baseItem = block.getBaseItem();
                    
                    if((immediate && baseItem.hasId("base/maw") || baseItem.hasId("base/pipe")) || 
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
                EntitySpawn spawn = getRandomEligibleEntitySpawn(
                        zone.getBiome(), locale, y / (double)zone.getHeight(), block.getBaseItem());
                
                if(immediate) {
                    if(tryBustOrifice(x, y, Layer.BACK) || tryBustOrifice(x, y, Layer.FRONT)) {
                        return;
                    }
                }
                
                if(spawn != null) {
                    EntityConfig config = spawn.getEntity();
                    
                    if(config != null) {
                        spawnEntity(new Npc(zone, config), x, y);
                    }
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
        npcs.values().stream()
            .filter(npc -> npc.isDead() || (!npc.isPersistent() && (!zone.isChunkLoaded(npc.getBlockX(), npc.getBlockY()) ||
                    (npc.isTransient() && System.currentTimeMillis() > npc.getLastTrackedAt() + ENTITY_CLEAR_TIME))))
            .collect(Collectors.toList())
            .forEach(this::removeEntity);
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
        int index = zone.getBlockIndex(x, y);
        
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
        
        // Remove existing mounted entity at this position
        // Ideally this should be done on block update but this works just fine
        Npc existingMountedNpc;
        
        if((existingMountedNpc = mountedNpcs.remove(index)) != null) {
            removeEntity(existingMountedNpc);
        }
        
        // Check for mounted entity (turrets & geysers)
        if(item.isEntity()) {
            EntityConfig config = EntityRegistry.getEntityConfig(item.getId());
            
            if(config != null) {
                Npc entity = new Npc(zone, config);
                MetaBlock metaBlock = zone.getMetaBlock(x, y);
                
                // Set owner entity if it has one
                if(metaBlock != null && metaBlock.hasOwner()) {
                    entity.setOwner(metaBlock.getOwner());
                }
                
                entity.setMountBlock(x, y);
                spawnEntity(entity, x, y);
                mountedNpcs.put(index, entity);
            }
        }
    }
    
    public void spawnPersistentNpcs(Collection<NpcData> data) {
        for(NpcData entry : data) {
            if(entry.getType() == null) {
                continue;
            }
            
            Npc npc = new Npc(zone, entry.getType());
            npc.setName(entry.getName());
            spawnEntity(npc, entry.getX(), entry.getY());
        }
    }
    
    public Npc spawnEntity(String type, int x, int y) {
        return spawnEntity(type, x, y, false);
    }
    
    public Npc spawnEntity(String type, int x, int y, boolean effect) {
        EntityConfig config = EntityRegistry.getEntityConfig(type);
        
        if(config == null) {
            return null;
        }
        
        Npc entity = new Npc(zone, config);
        spawnEntity(entity, x, y, effect);
        return entity;
    }
    
    public void spawnEntity(Entity entity, int x, int y) {
        spawnEntity(entity, x, y, false);
    }
    
    public void spawnEntity(Entity entity, int x, int y, boolean effect) {
        addEntity(entity);
        entity.setPosition(x, y);
        
        if(effect && zone.isChunkLoaded(x, y)) {
            zone.spawnEffect(x + 0.5F, y + 0.5F, "bomb-teleport", 4);
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
            
            // Remove entity from parent's children if it has one
            Npc npc = (Npc)entity;
            Entity owner = npc.getOwner();
            
            if(owner instanceof Npc) {
                ((Npc)owner).removeChild(npc);
            }
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
        return (int)npcs.values().stream().filter(Npc::isTransient).count();
    }
    
    public Collection<Npc> getNpcs() {
        return Collections.unmodifiableCollection(npcs.values());
    }
    
    public List<Npc> getPersistentNpcs() {
        return npcs.values().stream().filter(Npc::isPersistent).collect(Collectors.toList());
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
