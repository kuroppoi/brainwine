package brainwine.gameserver.item;

import brainwine.gameserver.util.LazyGetter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;

@JsonSerialize(using = LazyItemGetter.Serializer.class)
public class LazyItemGetter extends LazyGetter<String, Item> {

    public LazyItemGetter(String in) {
        super(in);
    }
    
    @Override
    public Item load() {
        return ItemRegistry.getItem(in);
    }

    public static class Serializer extends JsonSerializer {
        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(((LazyItemGetter)value).get().getId());
        }
    }
}
