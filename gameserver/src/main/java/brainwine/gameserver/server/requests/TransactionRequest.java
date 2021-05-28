package brainwine.gameserver.server.requests;

import brainwine.gameserver.entity.player.Player;
import brainwine.gameserver.server.PlayerRequest;
import brainwine.gameserver.server.messages.StatMessage;

public class TransactionRequest extends PlayerRequest {
    
    public String key;
    
    @Override
    public void process(Player player) {
        player.alert("Sorry, the crown store has not been implemented yet.");
        player.sendMessage(new StatMessage("crowns", player.getCrowns()));
    }
}
