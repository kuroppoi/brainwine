package brainwine.gameserver.msgpack.templates;

import java.io.IOException;

import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.unpacker.Unpacker;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemRegistry;

public class ItemTemplate extends AbstractTemplate<Item> {

    @Override
    public void write(Packer packer, Item item, boolean required) throws IOException {
        if(item == null) {
            if(required) {
                throw new MessageTypeException("Attempted to write null");
            }
            
            packer.writeNil();
            return;
        }
        
        packer.write(item.getId());
    }

    @Override
    public Item read(Unpacker unpacker, Item to, boolean required) throws IOException {
        if(!required && unpacker.trySkipNil()) {
            return null;
        }
        
        return ItemRegistry.getItem(unpacker.readInt());
    }
}
