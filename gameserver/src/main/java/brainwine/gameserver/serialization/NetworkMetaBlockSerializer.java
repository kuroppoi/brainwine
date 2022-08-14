package brainwine.gameserver.serialization;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import brainwine.gameserver.zone.MetaBlock;

public class NetworkMetaBlockSerializer extends StdSerializer<MetaBlock> {
    
    public static final NetworkMetaBlockSerializer INSTANCE = new NetworkMetaBlockSerializer();
    private static final long serialVersionUID = -3597246970639428645L;
    
    protected NetworkMetaBlockSerializer() {
        super(MetaBlock.class);
    }
    
    @Override
    public void serialize(MetaBlock metaBlock, JsonGenerator generator, SerializerProvider provider) throws IOException {
        generator.writeStartArray();
        generator.writeNumber(metaBlock.getX());
        generator.writeNumber(metaBlock.getY());
        generator.writeObject(metaBlock.getClientMetadata());
        generator.writeEndArray();
    }
}
