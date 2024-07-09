package brainwine.gameserver.entity.npc.behavior.composed;

import java.util.Map;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;

import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.entity.npc.behavior.SelectorBehavior;
import brainwine.gameserver.entity.npc.behavior.parts.ConveyorBehavior;
import brainwine.gameserver.entity.npc.behavior.parts.DigBehavior;
import brainwine.gameserver.entity.npc.behavior.parts.FallBehavior;
import brainwine.gameserver.entity.npc.behavior.parts.IdleBehavior;
import brainwine.gameserver.entity.npc.behavior.parts.TurnBehavior;
import brainwine.gameserver.entity.npc.behavior.parts.WalkBehavior;
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
        addChild(ConveyorBehavior.class, config);
        
        if(config.containsKey("idle")) {
            addChild(IdleBehavior.class, MapHelper.getMap(config, "idle"));
        }
        
        addChild(DigBehavior.class, config);
        addChild(WalkBehavior.class, config);
        addChild(FallBehavior.class, config);
        addChild(TurnBehavior.class, config);
    }
}
