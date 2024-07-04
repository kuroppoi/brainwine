package brainwine.gameserver.entity.npc.behavior.parts;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;

import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.entity.npc.behavior.Behavior;
import brainwine.gameserver.entity.npc.behavior.BehaviorMessage;
import brainwine.gameserver.player.Player;

public class PetBehavior extends Behavior {
    
    public static final long PET_COOLDOWN = 2000;
    private long lastPettedAt;
    
    @JsonCreator
    public PetBehavior(@JacksonInject Npc entity) {
        super(entity);
    }

    @Override
    public boolean behave() {
        return true;
    }
    
    @Override
    public void react(BehaviorMessage message, Player player, Object... data) {
        switch(message) {
        case INTERACT:
            long now = System.currentTimeMillis();
            
            // Do nothing if entity was petted recently
            if(now < lastPettedAt + PET_COOLDOWN) {
                return;
            }
            
            entity.emote("*terrapus noises*");
            entity.spawnEffect("terrapus purr");
            lastPettedAt = now;
            break;
        default:
            break;
        }
    }
}
