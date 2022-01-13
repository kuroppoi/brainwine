package brainwine.gameserver.server.requests;

import java.util.Map;

import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.server.PlayerRequest;

public class DialogRequest extends PlayerRequest {
    
    public Object id;
    public Object[] input;
    
    @Override
    public void process(Player player) {
        if(input.length == 1 && input[0] instanceof Map) {
            input = ((Map<?, ?>)input[0]).values().toArray();
        }
        
        if(id instanceof String) {
            player.alert("Sorry, this action is not implemented yet.");
            return;
        } else if(id instanceof Integer) {
            if((int)id > 0) {
                player.handleDialogInput((int)id, input);
            }
        }
    }
}
