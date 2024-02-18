package brainwine.gameserver.entity.npc.behavior.parts;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;

import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.entity.npc.behavior.Behavior;
import brainwine.gameserver.zone.Zone;

public class UnblockBehavior extends Behavior {
    
    protected int rate = 1;
    
    @JsonCreator
    public UnblockBehavior(@JacksonInject Npc entity) {
        super(entity);
    }
    
    @Override
    public boolean behave() {
        Zone zone = entity.getZone();
        Random random = ThreadLocalRandom.current();
        
        for(int i = 0; i < rate; i++) {
            int x = (int)entity.getX() + random.nextInt(entity.getSizeX());
            int y = (int)entity.getY() - random.nextInt(entity.getSizeY());
            
            if(zone.isChunkLoaded(x, y) && zone.getBlock(x, y).getFrontItem().isDiggable()) {
                zone.digBlock(x, y);
            }
        }
        
        return true;
    }
    
    public void setRate(int rate) {
        this.rate = rate;
    }
}
