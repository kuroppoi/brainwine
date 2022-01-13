package brainwine.gameserver.serialization;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import brainwine.gameserver.zone.Chunk;

public class NetworkChunkSerializer extends StdSerializer<Chunk> {

    public static final NetworkChunkSerializer INSTANCE = new NetworkChunkSerializer();
    private static final long serialVersionUID = -1573014029866696503L;

    protected NetworkChunkSerializer() {
        super(Chunk.class);
    }
    
    @Override
    public void serialize(Chunk chunk, JsonGenerator generator, SerializerProvider provider) throws IOException {
        generator.writeStartArray();
        generator.writeNumber(chunk.getX());
        generator.writeNumber(chunk.getY());
        generator.writeNumber(chunk.getWidth());
        generator.writeNumber(chunk.getHeight());
        generator.writeObject(chunk.getBlocks());
        generator.writeEndArray();
    }
}
