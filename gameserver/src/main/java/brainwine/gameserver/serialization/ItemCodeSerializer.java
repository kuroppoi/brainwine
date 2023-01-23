package brainwine.gameserver.serialization;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import brainwine.gameserver.item.Item;

public class ItemCodeSerializer extends StdSerializer<Item> {
    
    public static final ItemCodeSerializer INSTANCE = new ItemCodeSerializer();
    private static final long serialVersionUID = 8938614385421916365L;
    
    protected ItemCodeSerializer() {
        super(Item.class);
    }
    
    @Override
    public void serialize(Item item, JsonGenerator generator, SerializerProvider provider) throws IOException {
        generator.writeNumber(item.getCode());
    }
}
