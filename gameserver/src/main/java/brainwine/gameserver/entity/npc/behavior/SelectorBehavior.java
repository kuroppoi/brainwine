package brainwine.gameserver.entity.npc.behavior;

import java.util.Map;

import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.entity.npc.Npc;

public class SelectorBehavior extends CompositeBehavior implements Reactor {
    
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
    public boolean react(Entity other, ReactionEffect message, Object params) {
        for(Behavior child : children) {
            if(child.isReactor()) {
                if(((Reactor) child).react(other, message, params)) {
                    return true;
                }
            }
        }

        return false;
    }
}
