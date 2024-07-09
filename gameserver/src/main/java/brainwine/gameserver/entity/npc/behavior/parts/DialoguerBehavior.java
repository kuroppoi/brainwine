package brainwine.gameserver.entity.npc.behavior.parts;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;

import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.entity.npc.behavior.Behavior;
import brainwine.gameserver.entity.npc.behavior.BehaviorMessage;
import brainwine.gameserver.player.Player;

// TODO implement this fully
public class DialoguerBehavior extends Behavior {
    
    @JsonCreator
    public DialoguerBehavior(@JacksonInject Npc entity) {
        super(entity);
    }

    @Override
    public boolean behave() {
        return false;
    }
    
    @Override
    public void react(BehaviorMessage message, Player player, Object... data) {
        switch(message) {
        case INTERACT:
            // Do nothing if player is null
            if(player == null) {
                break;
            }
            
            String[] responses = {
                "Error: Job module not found.",
                "I am not quite ready for that yet.",
                "Sorry, please try again later.",
                "Query returned error 404.",
                "Critical error.",
                "Does not compute."
            };
            
            // Respond with a random bogus message for now
            String response = responses[(int)(Math.random() * responses.length)];
            entity.emote(response);
            break;
        default:
            break;
        }
    }
}
