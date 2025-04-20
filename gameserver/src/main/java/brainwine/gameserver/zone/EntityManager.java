package brainwine.gameserver.zone;

import static brainwine.shared.LogMarkers.SERVER_MARKER;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import brainwine.gameserver.player.NotificationType;
import brainwine.gameserver.server.messages.EventMessage;
import brainwine.gameserver.server.messages.NotificationMessage;
import brainwine.gameserver.util.MathUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;

import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.entity.EntityConfig;
import brainwine.gameserver.entity.EntityRegistry;
import brainwine.gameserver.entity.EntityStatus;
import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.entity.npc.NpcData;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.item.ModType;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.resource.ResourceFinder;
import brainwine.gameserver.server.messages.EntityPositionMessage;
import brainwine.gameserver.server.messages.EntityStatusMessage;
import brainwine.gameserver.util.MapHelper;
import brainwine.gameserver.util.Vector2i;
import brainwine.gameserver.util.WeightedMap;
import brainwine.shared.JsonHelper;

public class EntityManager {
    
    public static final long ENTITY_CLEAR_TIME = 10000;
    public static final long SPAWN_INTERVAL = 200;
    private static final Logger logger = LogManager.getLogger();
    private static final ThreadLocalRandom random = ThreadLocalRandom.current();
    private static final Map<Biome, List<EntitySpawn>> globalSpawns = new HashMap<>();
    private final List<EntitySpawn> spawns = new ArrayList<>();
    private final Map<Integer, Entity> entities = new ConcurrentHashMap<>(); // TODO is there a better solution?
    private final Map<Integer, Npc> npcs = new HashMap<>();
    private final Map<Integer, Npc> mountedNpcs = new HashMap<>();
    private final Map<Integer, Player> players = new HashMap<>();
    private final Map<String, Player> playersByName = new HashMap<>();
    private final Zone zone;
    private int entityDiscriminator;
    private long lastSpawnAt = System.currentTimeMillis();
    private long lastInvasionAt;
    private long lastInvasionWaveAt = System.currentTimeMillis();
    private long timeUntilNextInvasionWave;
    private long timeUntilNextInvasion = 10000;
    private Player currentInvasionTarget;
    private int currentInvasionWave = 4;
    private final List<Integer> invaders = new ArrayList<>();
    private long lastInhibitionTime = System.currentTimeMillis();
    
    public EntityManager(Zone zone) {
        this.zone = zone;
    }
    
    public static void loadEntitySpawns() {
        globalSpawns.clear();
        logger.info(SERVER_MARKER, "Loading entity spawns ...");
        
        try {
            URL url = ResourceFinder.getResourceUrl("spawning.json", true);
            Map<Biome, List<EntitySpawn>> loaded = JsonHelper.readValue(url, new TypeReference<Map<Biome, List<EntitySpawn>>>(){});

            // Validate all entity spawns.
            for(Map.Entry<Biome, List<EntitySpawn>> entry : loaded.entrySet()) {
                List<EntitySpawn> validSpawns = new ArrayList<>(entry.getValue().size());
                for(EntitySpawn spawn : entry.getValue()) {
                    if(spawn.getEntityConfig() != null) {
                        validSpawns.add(spawn);
                    } else {
                        logger.warn("Entity " + spawn.getEntity() + " not found for " + entry.getKey() + " spawns.");
                    }
                }
                globalSpawns.put(entry.getKey(), validSpawns);
            }

        } catch (IOException e) {
            logger.error(SERVER_MARKER, "Failed to load entity spawns", e);
        }
    }

    public void updateSpawnRates() {
        if(spawns.isEmpty()) {
            try {
                spawns.addAll(
                        JsonHelper.readValue(
                                JsonHelper.writeValueAsString(globalSpawns.get(zone.getBiome())),
                                new TypeReference<List<EntitySpawn>>() {}
                        )
                );
            } catch(Exception e) {
                throw new RuntimeException("Cannot initialize individual zone entity spawns.", e);
            }
        }

        int difficulty = zone.getMassSpawnerConfiguration() != null
                ? zone.getMassSpawnerConfiguration().getDifficulty()
                : 3;

        for(EntitySpawn spawn : spawns) {
            spawn.resetFrequency();

            boolean isFriendly = spawn.getEntityConfig().isFriendly();
            boolean isHostile = !isFriendly;

            if(difficulty == 1 && isHostile) spawn.setFrequency(0.0);
            if(difficulty == 2 && isFriendly) spawn.setFrequency(2.0 * spawn.getFrequency());
            if(difficulty == 4 && isHostile) spawn.setFrequency(2.0 * spawn.getFrequency());
            if(difficulty == 5 && isHostile) spawn.setFrequency(3.0 * spawn.getFrequency());
        }
    }

    private List<EntitySpawn> getEligibleEntitySpawns(Biome biome, String locale, double depth, double acidity, Item baseItem, ZoneRules rules) {
        return spawns.stream()
                .filter(spawn -> locale.equalsIgnoreCase(spawn.getLocale())
                        && depth >= spawn.getMinDepth() && depth <= spawn.getMaxDepth()
                        && (
                                (rules.isHostileEntitySpawnsEnabled() || acidity >= spawn.getMinAcidity()) &&
                                (rules.isPeacefulEntitySpawnsEnabled() || acidity <= spawn.getMaxAcidity())
                        )
                        && ((!baseItem.hasId("base/maw") && !baseItem.hasId("base/pipe")) || spawn.getOrifice() == baseItem))
                .collect(Collectors.toList());
    }
    
    private EntitySpawn getRandomEligibleEntitySpawn(Biome biome, String locale, double depth, double acidity, Item baseItem, ZoneRules rules) {
        return new WeightedMap<>(getEligibleEntitySpawns(biome, locale, depth, acidity, baseItem, rules), EntitySpawn::getFrequency).next();
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

        // Blow up powered inhibitors if there are players in the world
        if(System.currentTimeMillis() > lastInhibitionTime + 1000) {
            if(!players.isEmpty()) processInhibitors();
            lastInhibitionTime = System.currentTimeMillis();
        }

        // Process active evokers if there are players in the world
        if(System.currentTimeMillis() > lastInvasionAt + timeUntilNextInvasion) {
            if(currentInvasionWave >= 4 && !zone.getPlayers().isEmpty()) {
                tryEvoking();
            }
            timeUntilNextInvasion = Math.max(10000 / Math.max(zone.getPlayers().size(), 1), 20000);
            lastInvasionAt = System.currentTimeMillis();
        }

        tickInvasion();
    }
    
    private void spawnRandomEntity() {
        boolean immediate = random.nextDouble() < 0.75;
        List<Chunk> visibleChunks = zone.getVisibleChunks();
        List<Chunk> chunks = immediate ? visibleChunks : zone.getLoadedChunks().stream()
                .filter(chunk -> !visibleChunks.contains(chunk)).collect(Collectors.toList());

        boolean isNotConfigured = zone.getMassSpawnerConfiguration() == null;
        boolean doMaws = isNotConfigured || zone.getMassSpawnerConfiguration().isMawSpawningEnabled();
        boolean doAreas = isNotConfigured || zone.getMassSpawnerConfiguration().isAreaSpawningEnabled();

        if(!chunks.isEmpty()) {
            List<Vector2i> eligiblePositions = new ArrayList<>();
            Chunk chunk = chunks.get(random.nextInt(chunks.size()));

            for(int x = chunk.getX(); x < chunk.getX() + chunk.getWidth(); x++) {
                for(int y = chunk.getY(); y < chunk.getY() + chunk.getHeight(); y++) {
                    Block block = chunk.getBlock(x, y);
                    Item baseItem = block.getBaseItem();
                    
                    if((immediate && doMaws && (baseItem.hasId("base/maw") || baseItem.hasId("base/pipe"))) ||
                            (!immediate && doAreas && block.getBackItem().isAir() && block.getFrontItem().isAir())) {
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
                        zone.getBiome(), locale, y / (double)zone.getHeight(), zone.getAcidity(), block.getBaseItem(), zone.getRules());
                
                if(immediate) {
                    if(tryBustOrifice(x, y, Layer.BACK) || tryBustOrifice(x, y, Layer.FRONT)) {
                        return;
                    }
                }
                
                if(spawn != null) {
                    EntityConfig config = spawn.getEntityConfig();
                    
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
                    Npc entity = spawnEntity(guardian, x, y);
                    
                    if(entity != null) {
                        entity.setGuardBlock(x, y);
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
            Npc entity = spawnEntity(item.getId(), x, y);
            
            if(entity != null) {
                MetaBlock metaBlock = zone.getMetaBlock(x, y);
                
                // Set owner entity if it has one
                if(metaBlock != null && metaBlock.hasOwner()) {
                    entity.setOwner(metaBlock.getOwner());
                }
                
                entity.setMountBlock(x, y);
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
            npc.setJob(entry.getJob());
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
            player.onZoneEntered();
            players.put(entityId, player);
            playersByName.put(player.getName().toLowerCase(), player);
            player.sendMessageToPeers(new EntityStatusMessage(player, EntityStatus.ENTERING));
            player.sendMessageToPeers(new EntityPositionMessage(player));
            player.sendMessage(new EventMessage("playerIconDidChange", player.getIconEmoji()));
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
            playersByName.remove(entity.getName().toLowerCase());
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

    public void tryEvoking() {
        Set<Player> candidateSet = new HashSet<>();

        for(MetaBlock evoker : zone.getMetaBlocksWithItem("mechanical/spawner-brain")) {
            candidateSet.addAll(zone.getPlayersInRange(evoker.getX(), evoker.getY(), 30));
        }

        List<Player> candidates = candidateSet.stream()
                .filter(p -> !p.isGodMode())
                .collect(Collectors.toList());

        if(!candidates.isEmpty()) {
            startInvasion(candidates.get((int) (Math.random() * candidates.size())));
        }
    }

    public void processInhibitors() {
        Map<Player, Integer> evokersInhibited = new HashMap<>();
        List<Player> players = new ArrayList<>(zone.getPlayers());
        boolean inhibited = false;
        for(MetaBlock evoker : zone.getMetaBlocksWithItem("mechanical/spawner-brain")) {
            if(zone.getBlock(evoker.getX(), evoker.getY()).getFrontMod() != 0) {
                inhibited = true;
                zone.spawnEffect(evoker.getX(), evoker.getY(), "bomb-electric", 5);
                zone.updateBlock(evoker.getX(), evoker.getY(), Layer.FRONT, Item.AIR);

                int minIndex = -1;
                double minDistance = Double.POSITIVE_INFINITY;
                for(int i = 0; i < players.size(); i++) {
                    double playerDistance = MathUtils.distance(players.get(i).getX(), players.get(i).getY(), evoker.getX(), evoker.getY());
                    if(minDistance > playerDistance) {
                        minIndex = i;
                        minDistance = playerDistance;
                    }
                }
                if(minIndex != -1) {
                    Player player = players.get(minIndex);
                    evokersInhibited.merge(player, 1, Integer::sum);
                }
            }
        }

        for(Map.Entry<Player, Integer> score : evokersInhibited.entrySet()) {
            Player player = score.getKey();
            int count = score.getValue();
            player.getStatistics().trackEvokersInhibited(count);
            String suffix = count == 1 ? " inhibited an evoker!" : " inhibited " + count + " evokers!";
            player.notify("You" + suffix, NotificationType.SYSTEM);
            player.notifyPeers(player.getName() + suffix, NotificationType.SYSTEM);
            player.addExperience(500 * count);
        }

        if(inhibited && !checkEvokers()) {
            zone.sendMessage(new NotificationMessage("All evokers have been inhibited!", NotificationType.SYSTEM));
        }
    }

    public boolean checkEvokers() {
        return !zone.getMetaBlocksWithItem("mechanical/spawner-brain").isEmpty();
    }

    public synchronized void startInvasion(Player target) {
        for(int entityId : invaders) {
            Entity e = getEntity(entityId);
            if(e != null) {
                zone.spawnEffect(e.getX(), e.getY(), "bomb-teleport", 4);
                e.setHealth(0.0f);
            }
        }
        invaders.clear();

        currentInvasionTarget = target;
        currentInvasionWave = 0;
        lastInvasionAt = System.currentTimeMillis();
        lastInvasionWaveAt = 0;
        timeUntilNextInvasionWave = 0;
    }

    private void tickInvasion() {
        if(currentInvasionWave >= 4) return;
        currentInvasionWave = Math.max(0, currentInvasionWave);

        if(currentInvasionTarget == null
                || !currentInvasionTarget.isOnline()
                || currentInvasionTarget.getZone() != zone
        ) {
            currentInvasionWave = 4;
            return;
        }

        if(lastInvasionWaveAt + timeUntilNextInvasionWave < System.currentTimeMillis()) {
            WeightedMap<String> invaders = zone.getBiome() == Biome.BRAIN
                    ? new WeightedMap<>(MapHelper.map(
                        String.class, Double.class,
                    "brains/small",  15.0,
                        "brains/medium", 2.0,
                        "brains/medium-dire", 1.0
                    ))
                    : new WeightedMap<>(MapHelper.map(
                        String.class, Double.class,
                        "revenant", 15.0,
                        "dire-revenant", 2.0,
                        "revenant-lord", 1.0
                    ));

            int numInvaders = 1;
            if(currentInvasionWave == 3 && Math.random() < 0.5) {
                numInvaders = 2;
            }

            List<Vector2i> eligiblePositions = new ArrayList<>(8);
            for(int x = -1; x <= 1; x++) {
                for(int y = -1; y <= 1; y++) {
                    if(x == 0 && y == 0) continue;
                    int blockX = currentInvasionTarget.getBlockX() + x;
                    int blockY = currentInvasionTarget.getBlockY() + y;
                    if(zone.areCoordinatesInBounds(blockX, blockY) && !zone.isBlockOccupied(blockX, blockY, Layer.FRONT)) {
                        eligiblePositions.add(new Vector2i(blockX, blockY));
                    }
                }
            }

            if(eligiblePositions.isEmpty()) {
                eligiblePositions.add(new Vector2i(currentInvasionTarget.getBlockX(), currentInvasionTarget.getBlockY()));
            }

            for(int i = 0; i < numInvaders; i++) {
                Vector2i pos = eligiblePositions.get((int)(Math.random() * eligiblePositions.size()));
                Npc npc = spawnEntity(invaders.next(), pos.getX(), pos.getY());
                this.invaders.add(npc.getId());
            }

            // Determine interval until next wave
            double minInterval = new double[] {3000, 1000, 500, 0}[currentInvasionWave];
            double maxInterval = new double[] {4000, 2000, 1500, 1000}[currentInvasionWave];
            timeUntilNextInvasionWave = (long)MathUtils.lerp(minInterval, maxInterval, Math.random());

            // Skip last wave randomly
            if(currentInvasionWave == 2 && Math.random() < 0.5) {
                currentInvasionWave = 4;
            }

            currentInvasionWave++;
            lastInvasionWaveAt = System.currentTimeMillis();
        }
    }

    public long getLastInvasionAt() {
        return lastInvasionAt;
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
        return playersByName.get(name.toLowerCase());
    }
    
    public int getPlayerCount() {
        return players.size();
    }
    
    public Collection<Player> getPlayers() {
        return Collections.unmodifiableCollection(players.values());
    }
}
