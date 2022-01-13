package brainwine.gameserver.serialization;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import brainwine.gameserver.zone.Block;

public class BlockDeserializer extends StdDeserializer<Block> {
    
    public static final BlockDeserializer INSTANCE = new BlockDeserializer();
    private static final long serialVersionUID = 4595727432327616509L;

    protected BlockDeserializer() {
        super(Block.class);
    }
    
    @Override
    public Block deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
        int base = parser.getIntValue();
        int back = parser.nextIntValue(0);
        int front = parser.nextIntValue(0);
        return new Block(base, back, front);
    }
}
