package brainwine.gameserver.behavior.composed;

import java.util.Map;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;

import brainwine.gameserver.behavior.SelectorBehavior;
import brainwine.gameserver.behavior.parts.FallBehavior;
import brainwine.gameserver.behavior.parts.IdleBehavior;
import brainwine.gameserver.behavior.parts.TurnBehavior;
import brainwine.gameserver.behavior.parts.WalkBehavior;
import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.util.MapHelper;

public class WalkerBehavior extends SelectorBehavior {
    
    @JsonCreator
    private WalkerBehavior(@JacksonInject Npc entity, 
            Map<String, Object> config) {
        super(entity, config);
    }
    
    public WalkerBehavior(Npc entity) {
        super(entity);
    }

    @Override
    protected void addChildren(Map<String, Object> config) {
        if(config.containsKey("idle")) {
            addChild(IdleBehavior.class, MapHelper.getMap(config, "idle"));
        }
        
        addChild(new WalkBehavior(entity));
        addChild(new FallBehavior(entity));
        addChild(new TurnBehavior(entity));
    }
}
