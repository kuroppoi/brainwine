package brainwine.gameserver.serialization;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import brainwine.gameserver.zone.Block;

public class BlockSerializer extends StdSerializer<Block> {

    public static final BlockSerializer INSTANCE = new BlockSerializer();
    private static final long serialVersionUID = 4060486562629926309L;

    protected BlockSerializer() {
        super(Block.class);
    }

    @Override
    public void serialize(Block block, JsonGenerator generator, SerializerProvider provider) throws IOException {
        generator.writeNumber(block.getBase());
        generator.writeNumber(block.getBack());
        generator.writeNumber(block.getFront());
    }
}
