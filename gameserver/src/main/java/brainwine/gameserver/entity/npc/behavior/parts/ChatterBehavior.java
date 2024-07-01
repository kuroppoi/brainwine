package brainwine.gameserver.entity.npc.behavior.parts;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;

import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.entity.npc.behavior.Behavior;
import brainwine.gameserver.Fake;
import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.entity.FacingDirection;

public class ChatterBehavior extends Behavior {
    private static final long MESSAGE_COOLDOWN_MS = 1_000;
    private static final long NEXT_MESSAGE_OVERALL_MS = 6_000;
    private static final long NEXT_MESSAGE_PER_PERSON_MS = 60_000;

    private final long startedAt = System.currentTimeMillis() - 24 * 60 * 60 * 1000;
    private long mostRecentChatAt = startedAt;
    private String nextMessage = null;
    private long nextMessageAt = System.currentTimeMillis() + MESSAGE_COOLDOWN_MS;

    private Map<Entity, Long> lastChattedAt = new HashMap<>();

    @JsonCreator
    public ChatterBehavior(@JacksonInject Npc entity) {
        super(entity);
    }

    @Override
    public boolean behave() {
        if(nextMessage != null && System.currentTimeMillis() > nextMessageAt) {
            entity.emote(nextMessage);
            nextMessage = null;
        }

        if(System.currentTimeMillis() < mostRecentChatAt + NEXT_MESSAGE_OVERALL_MS) {
            // cooldown after chatting in case the other party wants to dialogue
            return true;
        } else {
            Entity target = entity.getZone().getRandomPlayerInRange(entity.getX(), entity.getY(), 3);

            if(target == null) {
                // no one to talk to
                return false;
            }

            // chat if some time has passed
            if(System.currentTimeMillis() > lastChattedAt.getOrDefault(target, startedAt) + NEXT_MESSAGE_PER_PERSON_MS) {
                chatAt(target);
                return true;
            }

            // couldn't chat with anyone, do something else
            return false;
        }
    }

    public void chatAt(Entity other) {
        final long currentTime = System.currentTimeMillis();

        nextMessage = Fake.get(Fake.Type.SALUTATION);
        nextMessageAt = currentTime + MESSAGE_COOLDOWN_MS;

        entity.setDirection(other.getX() > entity.getX() ? FacingDirection.EAST : FacingDirection.WEST);

        lastChattedAt.put(other, currentTime);
        mostRecentChatAt = currentTime;
    }

}
