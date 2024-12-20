package brainwine.gameserver.server.requests;

import brainwine.gameserver.player.Player;
import brainwine.gameserver.server.PlayerRequest;
import brainwine.gameserver.server.RequestInfo;
import brainwine.gameserver.shop.ShopManager;

@RequestInfo(id = 41)
public class TransactionRequest extends PlayerRequest {
    
    public String key;
    
    @Override
    public void process(Player player) {
        ShopManager.purchaseProduct(player, key);
    }
}
