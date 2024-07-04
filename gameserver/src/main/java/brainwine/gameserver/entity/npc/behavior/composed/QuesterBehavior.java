package brainwine.gameserver.entity.npc.behavior.composed;

import java.util.Map;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;

import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.entity.npc.behavior.SelectorBehavior;
import brainwine.gameserver.entity.npc.behavior.parts.ChatterBehavior;
import brainwine.gameserver.entity.npc.behavior.parts.DialoguerBehavior;
import brainwine.gameserver.entity.npc.behavior.parts.FallBehavior;
import brainwine.gameserver.entity.npc.behavior.parts.IdleBehavior;

public class QuesterBehavior extends SelectorBehavior {
        
    @JsonCreator
    private QuesterBehavior(@JacksonInject Npc entity, 
            Map<String, Object> config) {
        super(entity, config);
    }
    
    public QuesterBehavior(Npc entity) {
        super(entity);
    }
    
    @Override
    public void addChildren(Map<String, Object> config) {
        addChild(FallBehavior.class, config);
        addChild(DialoguerBehavior.class, config);
        addChild(ChatterBehavior.class, config);
        addChild(IdleBehavior.class, config);
        addChild(WalkerBehavior.class, config);
    }
}
