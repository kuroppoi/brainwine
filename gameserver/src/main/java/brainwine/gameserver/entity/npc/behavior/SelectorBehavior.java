package brainwine.gameserver.entity.npc.behavior;

import java.util.Map;

import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.player.Player;

public class SelectorBehavior extends CompositeBehavior {
    
    public SelectorBehavior(Npc entity, Map<String, Object> config) {
        super(entity, config);
    }
    
    public SelectorBehavior(Npc entity) {
        super(entity);
    }
    
    @Override
    public boolean behave() {
        for(Behavior child : children) {
            if(child.canBehave() && child.behave()) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public void react(BehaviorMessage message, Player player, Object... data) {
        for(Behavior child : children) {
            child.react(message, player, data);
        }
    }

}
