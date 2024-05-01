package brainwine.gameserver.entity.npc.behavior;

import java.util.Map;

import brainwine.gameserver.entity.npc.Npc;

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
}
