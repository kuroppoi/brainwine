package brainwine.gameserver.behavior.parts;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;

import brainwine.gameserver.behavior.Behavior;
import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.util.Vector2i;
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
        Vector2i size = entity.getSize();
        Random random = ThreadLocalRandom.current();
        
        for(int i = 0; i < rate; i++) {
            int x = (int)entity.getX() + random.nextInt(size.getX());
            int y = (int)entity.getY() - random.nextInt(size.getY());
            
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
