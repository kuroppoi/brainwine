package brainwine.gameserver.entity.npc.behavior.composed;

import java.util.Map;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;

import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.entity.npc.behavior.SelectorBehavior;
import brainwine.gameserver.entity.npc.behavior.parts.ClimbBehavior;
import brainwine.gameserver.entity.npc.behavior.parts.ConveyorBehavior;
import brainwine.gameserver.entity.npc.behavior.parts.FallBehavior;
import brainwine.gameserver.entity.npc.behavior.parts.IdleBehavior;
import brainwine.gameserver.entity.npc.behavior.parts.TurnBehavior;
import brainwine.gameserver.entity.npc.behavior.parts.WalkBehavior;
import brainwine.gameserver.util.MapHelper;

public class CrawlerBehavior extends SelectorBehavior {
        
    @JsonCreator
    private CrawlerBehavior(@JacksonInject Npc entity, 
            Map<String, Object> config) {
        super(entity, config);
    }
    
    public CrawlerBehavior(Npc entity) {
        super(entity);
    }
    
    @Override
    public void addChildren(Map<String, Object> config) {
        addChild(ConveyorBehavior.class, config);
        
        if(config.containsKey("idle")) {
            addChild(IdleBehavior.class, MapHelper.getMap(config, "idle"));
        }
        
        addChild(WalkBehavior.class, config);
        addChild(ClimbBehavior.class, config);
        addChild(TurnBehavior.class, config);
        addChild(ClimbBehavior.class, config);
        addChild(FallBehavior.class, config);
    }
}
