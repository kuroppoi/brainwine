package brainwine.gameserver.minigame;

import static brainwine.shared.LogMarkers.SERVER_MARKER;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;

import brainwine.gameserver.GameServer;
import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.entity.EntityAttack;
import brainwine.gameserver.entity.EntityRegistry;
import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.item.DamageType;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.loot.Loot;
import brainwine.gameserver.player.NotificationType;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.resource.ResourceFinder;
import brainwine.gameserver.util.Vector2i;
import brainwine.gameserver.zone.Zone;
import brainwine.shared.JsonHelper;

public class Pandora extends Minigame {
    
    public static final String PANDORA_OPEN_ID = "containers/pandora-open";
    public static final String[] REWARD_LOOT_CATEGORIES = { "armaments", "armaments+", "treasure", "treasure+" }; // TODO make configurable
    public static final long GRACE_PERIOD = 60000; // 1 minute
    public static final long MAX_ROUND_DURATION = 480000; // 8 minutes
    public static final double MINIGAME_RANGE = 50.0;
    public static final double MAX_ENEMY_DISTANCE = 50.0; // Maximum distance wave enemies can wander before they're teleported back
    public static final double MAX_RESPAWN_DISTANCE = 30.0; // Maximum distance at which players respawn near the minigame on death
    public static final int MIN_ROUNDS = 10;
    public static final int MAX_ROUNDS = 20;
    private static final Logger logger = LogManager.getLogger();
    private static final Map<Integer, List<Map<String, Integer>>> config = new HashMap<>();
    private final Map<String, Integer> roundSpawns = new HashMap<>();
    private final Set<String> potencyBumps = new HashSet<>();
    private final List<Npc> spawns = new ArrayList<>();
    private final Random random = new Random();
    private int currentRound;
    private long roundStartedAt;
    private long nextActionAt;
    
    public static void loadConfig() {
        logger.info(SERVER_MARKER, "Loading Pandora configuration ...");
        
        try {
            config.clear();
            config.putAll(JsonHelper.readValue(ResourceFinder.getResourceUrl("pandora.json"), new TypeReference<Map<Integer, List<Map<String, Integer>>>>(){}));
            
            // Make sure there are spawns for round 1
            if(!config.containsKey(1)) {
                throw new IllegalArgumentException("No round 1 spawns configured");
            }
            
            // Perform extensive error checking on load so we don't have to do it later
            config.forEach((round, spawns) -> {
                // Check for empty spawns
                if(spawns.stream().anyMatch(spawn -> spawn.values().stream().reduce(Integer::sum).orElse(0) <= 0)) {
                    throw new IllegalArgumentException(String.format("Round %s+ config has one or more empty spawns", round));
                }
                
                // Check for invalid entity types
                String invalidType = spawns.stream()
                        .flatMap(spawn -> spawn.keySet().stream())
                        .filter(type -> EntityRegistry.getEntityConfig(type) == null)
                        .findFirst().orElse(null);
                
                if(invalidType != null) {
                    throw new IllegalArgumentException(String.format("Invalid entity type: %s", invalidType));
                }
            });   
        } catch(Exception e) {
            logger.error(SERVER_MARKER, "Failed to load Pandora config", e);
            config.clear();
            return;
        }
    }
    
    public Pandora(Zone zone, Player initiator, int x, int y) {
        super(zone, initiator, x, y);
    }
    
    @Override
    public void tick(float deltaTime) {
        super.tick(deltaTime);
        long now = System.currentTimeMillis();
        
        // End minigame if block state is invalid
        if(!zone.isChunkLoaded(x, y) || !zone.getBlock(x, y).getFrontItem().hasId(PANDORA_OPEN_ID)) {
            finish();
            return;
        }
        
        // Wait for grace period to end
        if(currentRound == 0) {
            if(now >= startedAt + GRACE_PERIOD) {
                zone.notifyPlayers("Pandora's Box is coming ALIVE!");
                zone.updateBlock(x, y, Layer.FRONT, PANDORA_OPEN_ID, 2);
                zone.spawnEffect(x, y, "karma sound", 1);
                nextRound();
            }
            
            return;
        }
        
        // Cancel minigame if the maximum round duration has been reached
        if(now >= roundStartedAt + MAX_ROUND_DURATION) {
            cancel("Pandora could not be contained. Better luck next time.");
            return;
        }
        
        // Perform an action if it is time
        if(now >= nextActionAt) {
            nextAction();
            nextActionAt = (long)(now + (0.5 + currentRound * 0.05) * 1000); // Spawns become less frequent as the difficulty increases
        }

        // Randomly explode
        if(Math.random() < deltaTime * 0.123) {
            explode();
        }
    }
    
    @Override
    public void onInteract(Player player) {
        addParticipant(player);
        
        // Increase potency if the minigame hasn't started yet
        if(currentRound == 0 && potencyBumps.add(player.getDocumentId())) {
            zone.notifyPlayers(String.format("%s increased Pandora's chaos level to %s!", player.getName(), potencyBumps.size()), NotificationType.PEER_ACCOMPLISHMENT);
        }
    }
    
    @Override
    protected void onStart() {
        // Reject if config failed to load
        if(config.isEmpty()) {
            notifyCreator(String.format("Pandora has not been configured correctly.\nPlease %s.", initiator.isAdmin() ? "check the server log for more info" : "contact a server administrator"));
            finish();
            return;
        }
        
        zone.spawnEffect(x, y, "match start", 1);
        zone.updateBlock(x, y, Layer.FRONT, PANDORA_OPEN_ID, 1);
        potencyBumps.add(initiator.getDocumentId());
        
        // Notify all players in the zone
        for(Player player : zone.getPlayers()) {
            player.notifyProfile(String.format("%s opened Pandora's box at %s", initiator.getName(), zone.getReadableCoordinates(x, y)), "Tap on it in the next 60 seconds to build chaos!");
        }
    }
    
    @Override
    protected void onFinish() {
        // Kill all spawned entities
        for(Npc entity : spawns) {
            entity.setMinigame(null);
            entity.setHealth(0.0F);
        }
        
        // Destroy block if it is still there
        if(zone.getBlock(x, y).getFrontItem().hasId(PANDORA_OPEN_ID)) {
            explode(8.0F);
            zone.updateBlock(x, y, Layer.FRONT, 0);
            zone.spawnEffect(x, y, "match end", 1);
        }
    }
    
    @Override
    public void entityKilled(Entity entity, EntityAttack cause) {
        spawns.remove(entity);
    }
    
    @Override
    public void entityAttacked(Entity entity, EntityAttack attack, float damage) {
        // Do nothing if entity is not a wave enemy
        if(!spawns.contains(entity)) {
            return;
        }
        
        Entity attacker = attack.getAttacker();
        
        // Check if attacker is present
        if(attacker == null || !attacker.isPlayer()) {
            return;
        };
        
        Player player = (Player)attacker;
        Participant participant = addParticipant(player);
        participant.incrementScore(damage);
    }
    
    @Override
    public String describeScore(double score) {
        return String.format(Locale.US, "%,.2f damage dealt", score);
    }
    
    @Override
    public double getRange() {
        return MINIGAME_RANGE;
    }
    
    @Override
    public Vector2i getSpawnPoint(Player player) {
        return player.inRange(x, y, MAX_RESPAWN_DISTANCE) ? super.getSpawnPoint(player) : null;
    }
    
    private void nextAction() {
        // Move on to the next round if all enemies have been killed
        if(spawns.isEmpty() && roundSpawns.isEmpty()) {
            nextRound();
            return;
        }
        
        addParticipantsInRange();
        
        // Spawn a random enemy if there are any remaining
        if(!roundSpawns.isEmpty()) {
            String entity = roundSpawns.keySet().stream().skip(random.nextInt(roundSpawns.size())).findAny().get();
            roundSpawns.compute(entity, (key, value) -> value <= 1 ? null : value - 1);
            Npc npc = zone.spawnEntity(entity, x + random.nextInt(2), y - random.nextInt(3), true);
            npc.setMinigame(this);
            spawns.add(npc);
        }
        
        // Teleport all out-of-range enemies back to the box
        spawns.stream().filter(entity -> !entity.inRange(x, y, MAX_ENEMY_DISTANCE)).forEach(entity -> {
            entity.spawnEffect("bomb-teleport", 4);
            entity.setPosition(x + random.nextInt(2), y - random.nextInt(3));
            entity.spawnEffect("bomb-teleport", 4);
        });
    }
    
    private void nextRound() {
        addParticipantsInRange();
        int totalRounds = getTotalRounds();
        
        // Finish if the final round has been cleared
        if(currentRound >= totalRounds) {
            complete();
            return;
        }
        
        // Increment round and fetch spawn data
        currentRound++;
        int key = config.keySet().stream().filter(x -> currentRound >= x).max(Integer::compareTo).orElse(1);
        List<Map<String, Integer>> configs = config.getOrDefault(key, Collections.emptyList());
        roundSpawns.clear();
        roundSpawns.putAll(configs.get(random.nextInt(configs.size()))); // Select a random wave of enemies
        
        // Randomly increase the number of spawns this round depending on the chaos level
        int spawnBumps = potencyBumps.size() / (currentRound < 10 ? 2 : 3);
        
        for(int i = 0; i < spawnBumps; i++) {
            Entry<String, Integer> spawn = roundSpawns.entrySet().stream().skip(random.nextInt(roundSpawns.size())).findFirst().get();
            roundSpawns.put(spawn.getKey(), spawn.getValue() + 1);
        }
        
        // Notify all players in the zone that the next round is starting
        zone.notifyPlayers(String.format("Pandora wave %s of %s is beginning!", currentRound, totalRounds));
        roundStartedAt = System.currentTimeMillis();
    }
    
    private void complete() {
        finish();
        double luckMultiplier = Math.min(10.0, potencyBumps.size());
        int baseLuck = Math.min(12, participants.size() * 4);
        int position = 0;
        
        // Give out rewards
        for(Participant participant : leaderboard) {
            if(participant.isParticipating()) {
                int luck = (int)(Math.max(1, baseLuck - position * 4) * luckMultiplier);
                Player player = participant.getPlayer();
                Loot loot = GameServer.getInstance().getLootManager().getRandomLoot(luck, zone.getBiome(), player.getInventory().getWardrobe(), REWARD_LOOT_CATEGORIES);
                player.awardLoot(loot, String.format("You won %s place!", ordinalizeNumber(position + 1)));
            }
            
            position++;
        }
        
        // Broadcast leader's score
        zone.notifyPlayers(String.format("Pandora has been contained! %s showed mastery with %s!", currentLeader.getPlayer().getName(), describeScore(currentLeader.getScore())));
    }
    
    private void cancel(String message) {
        finish();
        zone.notifyPlayers(message);
    }
    
    private void explode() {
        explode(4.0F + (float)Math.random());
    }
    
    private void explode(float radius) {
        int x = this.x - 1 + (int)(Math.random() * 3);
        int y = this.y - 1 + (int)(Math.random() * 3);
        zone.explode(x, y, radius, null, false, 0.0F, DamageType.ENERGY, "bomb-electric"); // TODO explosion should do damage, but only to players!
    }
    
    public int getTotalRounds() {
        return Math.min(MAX_ROUNDS, MIN_ROUNDS + potencyBumps.size());
    }
}
