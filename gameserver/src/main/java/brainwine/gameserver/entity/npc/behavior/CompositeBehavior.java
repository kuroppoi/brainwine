package brainwine.gameserver.entity.npc.behavior;

import static brainwine.shared.LogMarkers.SERVER_MARKER;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.InjectableValues;

import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.player.Player;
import brainwine.shared.JsonHelper;

public abstract class CompositeBehavior extends Behavior {
    
    private static final Logger logger = LogManager.getLogger();
    protected final List<Behavior> children = new ArrayList<>();
    
    public CompositeBehavior(Npc entity, Map<String, Object> config) {
        super(entity);
        addChildren(config);
    }
    
    public CompositeBehavior(Npc entity) {
        this(entity, Collections.emptyMap());
    }
    
    @Override
    public void react(BehaviorMessage message, Player player, Object... data) {
        // Child behavior might add new behavior to its parent so we loop like this to avoid ConcurrentModificationException
        for(int i = 0; i < children.size(); i++) {
            children.get(i).react(message, player, data);
        }
    }
    
    protected void addChildren(Map<String, Object> config) {
        // Override
    }
    
    public void addChild(Class<? extends Behavior> type, Map<String, Object> config) {
        try {
            addChild(JsonHelper.readValue(config, type, new InjectableValues.Std().addValue(Npc.class, entity)));
        } catch(IOException e) {
            logger.error(SERVER_MARKER, "Could not add child behavior of type {}.", type.getName(), e);
        }
    }
    
    public void addChild(Behavior child) {
        if(children.contains(child)) {
            logger.warn(SERVER_MARKER, "Duplicate child instance {} for behavior {}", child, this);
            return;
        }
        
        child.setParent(this);
        children.add(child);
    }
    
    public void removeChild(Behavior child) {
        if(children.remove(child)) {
            child.setParent(null);
        }
    }
    
    public Collection<Behavior> getChildren() {
        return Collections.unmodifiableCollection(children);
    }
}
