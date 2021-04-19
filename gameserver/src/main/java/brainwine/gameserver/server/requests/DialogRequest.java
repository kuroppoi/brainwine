package brainwine.gameserver.server.requests;

import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.msgpack.models.DialogInputData;
import brainwine.gameserver.server.PlayerRequest;

public class DialogRequest extends PlayerRequest {
    
    public DialogInputData data;
    
    @Override
    public void process(Player player) {
        if(data.isType1()) {
            player.alert("Sorry, this action is not implemented yet.");
        } else if(data.isType2()) {
            player.handleDialogInput(data.getDialogId(), data.getInputData());
        }
    }
}
