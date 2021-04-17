package brainwine.gameserver.server.commands.console;

import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.server.PlayerCommand;

public class TeleportCommand extends PlayerCommand {
    
    public int x;
    public int y;
    
    @Override
    public void process(Player player) {
        if(!player.getZone().areCoordinatesInBounds(x, y)) {
            player.alert("Cannot teleport out of bounds!");
            return;
        }
        
        player.teleport(x, y);
    }
}
