package brainwine.gameserver.entity.npc.behavior.parts;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;

import brainwine.gameserver.entity.FacingDirection;
import brainwine.gameserver.entity.npc.Npc;
import brainwine.gameserver.entity.npc.behavior.Behavior;
import brainwine.gameserver.item.ItemUseType;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.zone.Zone;
import brainwine.gameserver.zone.Block;

public class WalkBehavior extends Behavior {
    
    protected String animation = "walk";
    
    @JsonCreator
    public WalkBehavior(@JacksonInject Npc entity) {
        super(entity);
    }
    
    @Override
    public boolean behave() {
        Zone zone = entity.getZone();

        // regular walk behavior if the zone is not known
        Block block = zone.findBlock(entity.getBlockX(), entity.getBlockY() + 1, Layer.FRONT, i -> i.hasUse(ItemUseType.MOVE));

        if (block != null) {
            // conveyor belt
            int direction = block.getMod(Layer.FRONT) == 0 ? 1 : -1;
            float movingSurfacePower = block.getFrontItem().getPower();
            entity.move(direction, 0, movingSurfacePower, "idle");
        } else {
            // regular walk behavior
            entity.move(entity.getDirection().getId(), 0, "walk");
        }
        
        return true;
    }
    
    @Override
    public boolean canBehave() {
        FacingDirection direction = entity.getDirection();
        return entity.isOnGround(direction.getId()) && !entity.isBlocked(direction.getId(), 0);
    }
    
    @JsonAlias("walk_animation")
    public void setAnimation(String animation) {
        this.animation = animation;
    }
}
