package brainwine.gameserver.server.messages;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.Layer;
import brainwine.gameserver.server.Message;

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
    
    @Override
    public boolean isCollection() {
        return true;
    }
}
