package brainwine.gameserver.server.messages;

import java.util.Arrays;
import java.util.Collection;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.server.Message;
import brainwine.gameserver.server.MessageInfo;

@MessageInfo(id = 39)
public class WardrobeMessage extends Message {
    
    public Collection<Item> wardrobe;
    
    public WardrobeMessage(Collection<Item> wardrobe) {
        this.wardrobe = wardrobe;
    }
    
    public WardrobeMessage(Item... items) {
        this(Arrays.asList(items));
    }
}
