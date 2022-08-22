package brainwine.gameserver.behavior.composed;

import java.util.Map;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;

import brainwine.gameserver.behavior.SelectorBehavior;
import brainwine.gameserver.behavior.parts.DigBehavior;
import brainwine.gameserver.behavior.parts.FallBehavior;
import brainwine.gameserver.behavior.parts.IdleBehavior;
import brainwine.gameserver.behavior.parts.TurnBehavior;
import brainwine.gameserver.behavior.parts.WalkBehavior;
import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.util.MapHelper;

public class DiggerBehavior extends SelectorBehavior {

    @JsonCreator
    private DiggerBehavior(@JacksonInject Npc entity, 
            Map<String, Object> config) {
        super(entity, config);
    }
    
    public DiggerBehavior(Npc entity) {
        super(entity);
    }
    
    @Override
    public void addChildren(Map<String, Object> config) {
        if(config.containsKey("idle")) {
            addChild(IdleBehavior.class, MapHelper.getMap(config, "idle"));
        }
        
        addChild(DigBehavior.class, config);
        addChild(WalkBehavior.class, config);
        addChild(FallBehavior.class, config);
        addChild(TurnBehavior.class, config);
    }
}
