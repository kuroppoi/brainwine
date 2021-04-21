package brainwine.gameserver.server.messages;

import java.util.Collection;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.server.Message;

public class WardrobeMessage extends Message {
    
    public Collection<Item> wardrobe;
    
    public WardrobeMessage(Collection<Item> wardrobe) {
        this.wardrobe = wardrobe;
    }
}
