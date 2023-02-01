package brainwine.gameserver.behavior;

import static brainwine.shared.LogMarkers.SERVER_MARKER;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;

import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.util.MapHelper;
import brainwine.shared.JsonHelper;

public class SequenceBehavior extends CompositeBehavior {
    
    private static final Logger logger = LogManager.getLogger();
    private static final List<String> loggedInvalidTypes = new ArrayList<>();
    
    public SequenceBehavior(Npc entity, Map<String, Object> config) {
        super(entity, config);
    }
    
    public SequenceBehavior(Npc entity) {
        super(entity);
    }
    
    public static SequenceBehavior createBehaviorTree(Npc npc, List<Map<String, Object>> behavior) {
        SequenceBehavior root = new SequenceBehavior(npc);
        
        for(Map<String, Object> config : behavior) {
            try {
                root.addChild(JsonHelper.readValue(config, Behavior.class, new InjectableValues.Std().addValue(Npc.class, npc)));
            } catch(InvalidTypeIdException e) {
                String type = e.getTypeId();
                
                // TODO get rid of this once we add the remaining behaviors
                if(!loggedInvalidTypes.contains(type)) {
                    logger.warn(SERVER_MARKER, "No implementation exists for behavior type '{}'", type);
                    loggedInvalidTypes.add(type);
                }
            } catch(IOException e) {
                logger.error(SERVER_MARKER, "Could not add behavior type '{}' to behavior tree for entity with type '{}'", 
                        MapHelper.getString(config, "type", "unknown"), npc.getType(), e);
            }
        }
        
        return root;
    }
    
    @Override
    public boolean behave() {
        for(Behavior child : children) {
            if(!child.canBehave() || !child.behave()) {
                return false;
            }
        }
        
        return true;
    }
}
