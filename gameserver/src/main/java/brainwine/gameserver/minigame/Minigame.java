package brainwine.gameserver.minigame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.entity.EntityAttack;
import brainwine.gameserver.item.ItemUseType;
import brainwine.gameserver.player.NotificationType;
import brainwine.gameserver.player.Player;
import brainwine.gameserver.util.MathUtils;
import brainwine.gameserver.util.Vector2i;
import brainwine.gameserver.zone.Zone;

/**
 * Base class for minigame sessions.
 */
public abstract class Minigame {
    
    public static final long LEADERBOARD_UPDATE_INTERVAL = 2000;
    protected final Map<Player, Participant> participants = new HashMap<>();
    protected final List<Participant> leaderboard = new ArrayList<>();
    protected final Zone zone;
    protected final Player initiator;
    protected final int x;
    protected final int y;
    protected boolean active;
    protected Participant currentLeader;
    protected long startedAt;
    private long lastLeaderboardUpdateAt;
    
    public Minigame(Zone zone, Player initiator, int x, int y) {
        this.zone = zone;
        this.initiator = initiator;
        this.x = x;
        this.y = y;
    }
    
    public abstract void onInteract(Player player);
    protected abstract void onStart();
    protected abstract void onFinish();
    
    public void tick(float deltaTime) {
        long now = System.currentTimeMillis();
        
        // Update leaderboard
        if(now >= lastLeaderboardUpdateAt + LEADERBOARD_UPDATE_INTERVAL) {
            updateLeaderboard();
            lastLeaderboardUpdateAt = now;
        }
    }
    
    /**
     * Preferably, call {@link Zone#startMinigame(Minigame)}.
     */
    public final void start() {
        if(active) {
            return; // Do nothing if already started
        }
        
        active = true;
        startedAt = System.currentTimeMillis();
        addParticipant(initiator);
        addParticipantsInRange();
        onStart();
    }
    
    public final void finish() {
        if(!active) {
            return; // Do nothing if already inactive
        }
        
        updateLeaderboard();
        
        // Clean up participants
        for(Participant participant : participants.values()) {
            Player player = participant.getPlayer();
            
            if(player.getMinigame() == this) {
                player.setMinigame(null);
            }
            
            participant.showInfo("");
        }
        
        onFinish();
        active = false;
    }
    
    public void entityKilled(Entity entity, EntityAttack cause) {
        // Override
    }
    
    public void entityAttacked(Entity entity, EntityAttack attack, float damage) {
        // Override
    }
    
    public void updateLeaderboard() {
        leaderboard.clear();
        participants.values().stream().sorted((a, b) -> Double.compare(b.getScore(), a.getScore())).forEach(leaderboard::add);
        Participant leader = leaderboard.get(0);
        
        if(leader.getScore() > 0.0) {
            if(leader != currentLeader) {
                notifyParticipants(String.format("%s took the lead with %s!", leader.getPlayer().getName(), describeScore(leader.getScore())), NotificationType.PEER_ACCOMPLISHMENT);
            }
            
            currentLeader = leader;
            leaderboard.forEach(x -> x.showInfo(String.format("You are in %s place with %s", ordinalizeNumber(getLeaderboardPosition(x)), describeScore(x.getScore()))));
        }
    }
    
    public void notifyCreator(Object message) {
        notifyCreator(message, NotificationType.POPUP);
    }
    
    public void notifyCreator(Object message, NotificationType type) {
        if(initiator != null) {
            initiator.notify(message, type);
        }
    }
    
    public void notifyParticipants(Object message) {
        notifyParticipants(message, NotificationType.POPUP);
    }
    
    public void notifyParticipants(Object message, NotificationType type) {
        for(Participant participant : participants.values()) {
            if(participant.isParticipating()) {
                participant.getPlayer().notify(message, type);
            }
        }
    }
    
    public void addParticipantsInRange() {
        for(Player player : zone.getPlayersInRange(x, y, getRange())) {
            if(!player.hasActiveMinigame()) {
                addParticipant(player);
            }
        }
    }
    
    public Participant addParticipant(Player player) {
        player.setMinigame(this);
        return participants.computeIfAbsent(player, x -> new Participant(this, x));
    }
    
    public boolean hasParticipant(Player player) {
        return participants.containsKey(player);
    }
    
    public Participant getParticipant(Player player) {
        return participants.get(player);
    }
    
    public int getLeaderboardPosition(Participant participant) {
        return leaderboard.indexOf(participant) + 1;
    }
    
    public String describeScore(double score) {
        return String.format(Locale.US, "%,.2f points", score); // Override
    }
    
    public double getRange() {
        return Math.max(zone.getWidth(), zone.getHeight()); // Override
    }
    
    public Vector2i getSpawnPoint(Player player) {
        List<Vector2i> spawnPoints = zone.getMetaBlocks().stream()
                .filter(block -> block.getItem().hasUse(ItemUseType.TELEPORT) && MathUtils.inRange(x, y, block.getX(), block.getY(), getRange()))
                .map(block -> new Vector2i(block.getX(), block.getY()))
                .collect(Collectors.toCollection(ArrayList::new));
        spawnPoints.add(new Vector2i(x, y));
        return spawnPoints.get((int)(Math.random() * spawnPoints.size())); // Override
    }
    
    public String ordinalizeNumber(int number) {
        String str = String.valueOf(number);
        String ordinal = str.endsWith("11") || str.endsWith("12") || str.endsWith("13") ? "th" : str.endsWith("1") ? "st" : str.endsWith("2") ? "nd" : str.endsWith("3") ? "rd" : "th";
        return String.format("%s%s", number, ordinal);
    }
    
    public Zone getZone() {
        return zone;
    }
    
    public Player getInitiator() {
        return initiator;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public boolean isActive() {
        return active;
    }
}
