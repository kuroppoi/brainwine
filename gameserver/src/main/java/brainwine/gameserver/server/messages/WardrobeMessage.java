package brainwine.gameserver.server.messages;

import java.util.Arrays;
import java.util.Collection;

import brainwine.gameserver.annotations.MessageInfo;
import brainwine.gameserver.item.Item;
import brainwine.gameserver.server.Message;

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
