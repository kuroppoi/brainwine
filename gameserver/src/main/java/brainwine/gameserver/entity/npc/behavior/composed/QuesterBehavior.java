package brainwine.gameserver.entity.npc.behavior.composed;

import java.util.Map;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;

import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.entity.npc.behavior.SelectorBehavior;
import brainwine.gameserver.entity.npc.behavior.parts.ChatterBehavior;
import brainwine.gameserver.entity.npc.behavior.parts.DialoguerBehavior;
import brainwine.gameserver.entity.npc.behavior.parts.IdleBehavior;
import brainwine.gameserver.util.MapHelper;

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
    protected void addChildren(Map<String, Object> config) {
        addChild(ChatterBehavior.class, config);
        addChild(DialoguerBehavior.class, config);
        addChild(WalkerBehavior.class, config);

        if(config.containsKey("idle")) {
            addChild(IdleBehavior.class, MapHelper.getMap(config, "idle"));
        }
        
    }
}
