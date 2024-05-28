package brainwine.gameserver.entity.npc.behavior.parts;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;

import brainwine.gameserver.entity.FacingDirection;
import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.entity.npc.behavior.Behavior;
import brainwine.gameserver.item.ItemUseType;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.zone.Block;

public class ConveyorBeltBehavior extends Behavior {
    protected String animation = "idle";

    @JsonCreator
    public ConveyorBeltBehavior(@JacksonInject Npc entity) {
        super(entity);
    }

    @Override
    public boolean behave() {
        // Find moving surface the entity is standing on
        Block block = entity.getZone().findBlock(entity.getBlockX(), entity.getBlockY() + 1,
            b -> b.getFrontItem().hasUse(ItemUseType.MOVE));

        if(block == null) return false;
        
        int direction = block.getMod(Layer.FRONT) == 0 ? 1 : -1;
        float movingSurfacePower = block.getFrontItem().getPower();

        if(entity.isBlocked(direction, 0)) {
            entity.setDirection(direction == -1 ? FacingDirection.EAST : FacingDirection.WEST);
            entity.setAnimation("walk");
        } else {
            entity.move(direction, 0, movingSurfacePower, animation);
        }
        
        return true;
    }
    
    @Override
    public boolean canBehave() {
        if(!entity.isOnGround()) return false;

        Block block = entity.getZone().findBlock(entity.getBlockX(), entity.getBlockY() + 1,
            b -> b.getFrontItem().hasUse(ItemUseType.MOVE));

        return block != null;
    }
    
    @JsonAlias("conveyor_belt_animation")
    public void setAnimation(String animation) {
        this.animation = animation;
    }
}
