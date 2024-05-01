package brainwine.gameserver.entity.npc.behavior.parts;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;

import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.entity.npc.behavior.Behavior;
import brainwine.gameserver.zone.Zone;

public class DigBehavior extends Behavior {
    
    @JsonCreator
    public DigBehavior(@JacksonInject Npc entity) {
        super(entity);
    }
    
    @Override
    public boolean behave() {
        Zone zone = entity.getZone();
        int x = (int)entity.getX() + entity.getDirection().getId();
        int y = (int)entity.getY();
        
        if(zone.isChunkLoaded(x, y) && zone.getBlock(x, y).getFrontItem().isDiggable()) {
            zone.digBlock(x, y);
        }
        
        return false;
    }
}
