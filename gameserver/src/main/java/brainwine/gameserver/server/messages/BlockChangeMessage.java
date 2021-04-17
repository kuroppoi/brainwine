package brainwine.gameserver.server.messages;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.server.Message;
import brainwine.gameserver.server.RegisterMessage;

@RegisterMessage(id = 9, collection = true)
public class BlockChangeMessage extends Message {

    public int x;
    public int y;
    public Layer layer;
    public int entityId;
    public Item item;
    public int mod;
    
    public BlockChangeMessage(int x, int y, Layer layer, Item item, int mod) {
        this.x = x;
        this.y = y;
        this.layer = layer;
        this.item = item;
        this.mod = mod;
    }
}
