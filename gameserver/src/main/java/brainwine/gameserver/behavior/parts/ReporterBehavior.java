package brainwine.gameserver.behavior.parts;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;

import brainwine.gameserver.behavior.Behavior;
import brainwine.gameserver.entity.npc.Npc;

public class ReporterBehavior extends Behavior {
    
    @JsonCreator
    public ReporterBehavior(@JacksonInject Npc entity) {
        super(entity);
    }

    @Override
    public boolean behave() {
        entity.setProperty("h", (int)(entity.getHealth() * 100), true);
        return true;
    }
}
