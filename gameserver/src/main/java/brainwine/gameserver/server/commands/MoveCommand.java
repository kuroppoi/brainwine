package brainwine.gameserver.server.commands;

import brainwine.gameserver.entity.Entity;
import brainwine.gameserver.entity.FacingDirection;
import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.server.PlayerCommand;
import brainwine.gameserver.server.RegisterCommand;
import brainwine.gameserver.server.messages.EntityPositionMessage;
import brainwine.gameserver.zone.Zone;

@RegisterCommand(id = 5)
public class MoveCommand extends PlayerCommand {
    
    public int x;
    public int y;
    public int velocityX;
    public int velocityY;
    public FacingDirection direction;
    public int targetX;
    public int targetY;
    public int animation;
    
    @Override
    public void process(Player player) {
        Zone zone = player.getZone();
        float fX = x / Entity.POSITION_MODIFIER;
        float fY = y / Entity.POSITION_MODIFIER;
        velocityX /= Entity.VELOCITY_MODIFIER;
        velocityY /= Entity.VELOCITY_MODIFIER;
        targetX /= Entity.VELOCITY_MODIFIER;
        targetY /= Entity.VELOCITY_MODIFIER;
        
        // If out of bounds
        if(fX < 0 || fY < 0 || fX > zone.getWidth() || fY > zone.getHeight()) {
            fail(player);
            return;
        }
        
        // If velocity is too high
        if(Math.abs(velocityX) > Player.MAX_SPEED_X || Math.abs(velocityY) > Player.MAX_SPEED_Y) {
            fail(player);
            return;
        }
        
        player.setPosition(fX, fY);
        player.setVelocity(velocityX, velocityY);
        player.setDirection(direction);
        player.setTarget(targetX, targetY);
        player.setAnimation(animation);
        player.sendMessageToPeers(new EntityPositionMessage(player));
        zone.exploreArea((int)fX, (int)fY); // TODO xp reward
    }
    
    private void fail(Player player) {
        player.rubberband();
    }
}
