package brainwine.gameserver.entity.npc.behavior.parts;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;

import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.entity.npc.behavior.Behavior;
import brainwine.gameserver.item.ItemUseType;
import brainwine.gameserver.zone.Block;

public class ConveyorBehavior extends Behavior {
    
    protected String animation = "idle";
    protected Block conveyorBlock;

    @JsonCreator
    public ConveyorBehavior(@JacksonInject Npc entity) {
        super(entity);
    }

    @Override
    public boolean behave() {
        int direction = conveyorBlock.getFrontMod() == 0 ? 1 : -1;
        float movingSurfacePower = conveyorBlock.getFrontItem().getPower();
        
        // Randomly change direction to match the conveyor belt's
        if(Math.random() < 0.333) {
            entity.setDirection(direction);
        }
        
        // Fail if entity is blocked to allow for other behavior like crawling to work
        if(entity.isBlocked(direction, 0)) {
            return false;
        }
        
        entity.move(direction, 0, movingSurfacePower, animation, false);
        return true;
    }
    
    @Override
    public boolean canBehave() {
        // Check if entity is standing on a conveyor belt
        conveyorBlock = entity.getZone().findBlock(entity.getBlockX(), entity.getBlockY() + 1, block -> block.getFrontItem().hasUse(ItemUseType.MOVE));
        return conveyorBlock != null;
    }
    
    public void setAnimation(String animation) {
        this.animation = animation;
    }
}
