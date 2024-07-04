package brainwine.gameserver.entity.npc.behavior.parts;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;

import brainwine.gameserver.entity.FacingDirection;
import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.entity.npc.behavior.Behavior;
import brainwine.gameserver.player.Player;

public class ChatterBehavior extends Behavior {
    
    public static final long BEHAVIOR_COOLDOWN = 6000;
    public static final long CHAT_COOLDOWN = 60000;
    private final Map<Player, Long> recentChats = new HashMap<>();
    private String nextMessage;
    private long nextMessageAt;
    private long lastChattedAt;
    
    @JsonCreator
    public ChatterBehavior(@JacksonInject Npc entity) {
        super(entity);
    }

    @Override
    public boolean behave() {
        long now = System.currentTimeMillis();
        
        // Emote the next message if it is ready
        if(nextMessage != null && now >= nextMessageAt) {
            entity.emote(nextMessage);
            nextMessage = null;
        }
        
        // Stop for a little while if the entity has chatted recently
        if(now < lastChattedAt + BEHAVIOR_COOLDOWN) {
            return true;
        }
        
        recentChats.values().removeIf(x -> now > x + CHAT_COOLDOWN); // Clear expired entries
        Player player = entity.getZone().getRandomPlayerInRange(entity.getX(), entity.getY(), 3);
        
        // Fail if no player is nearby or entity has chatted with target player recently
        if(player == null || recentChats.containsKey(player)) {
            return false;
        }
                
        // TODO store messages in a server configuration along with entity & zone names
        String[] messages = {
            "Hello.",
            "Salutations.",
            "Acknowledgements, stranger.",
            "Ah, a human!",
            "Hello, survivor person.",
            "Good day.",
            "Hello human."
        };
        nextMessage = messages[(int)(Math.random() * messages.length)];
        nextMessageAt = now + 1000; // Give the entity some time to stop moving
        lastChattedAt = now;
        recentChats.put(player, lastChattedAt);
        entity.setDirection(entity.getX() > player.getX() ? FacingDirection.WEST : FacingDirection.EAST); // Face entity towards the player
        entity.setAnimation(0);
        return true;
    }
}
