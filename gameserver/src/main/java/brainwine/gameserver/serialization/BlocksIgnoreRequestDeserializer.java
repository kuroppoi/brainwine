package brainwine.gameserver.serialization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import brainwine.gameserver.server.requests.BlocksIgnoreRequest;

public class BlocksIgnoreRequestDeserializer extends StdDeserializer<BlocksIgnoreRequest> {
    
    public static final BlocksIgnoreRequestDeserializer INSTANCE = new BlocksIgnoreRequestDeserializer();
    private static final long serialVersionUID = -3423549008026709552L;
    
    protected BlocksIgnoreRequestDeserializer() {
        super(BlocksIgnoreRequest.class);
    }
    
    @Override
    public BlocksIgnoreRequest deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
        BlocksIgnoreRequest request = new BlocksIgnoreRequest();
        
        if(parser.currentToken() != JsonToken.START_ARRAY) {
            throw new IOException("Got invalid token, expected START_ARRAY");
        }
        
        if(parser.nextToken() == JsonToken.VALUE_NUMBER_INT) {
            List<Integer> chunkIndices = new ArrayList<>();
            
            while(parser.currentToken() != JsonToken.END_ARRAY) {
                chunkIndices.add(parser.getIntValue());
                parser.nextToken();
            }
            
            request.chunkIndices = toArray(chunkIndices);
        } else {
            request.chunkIndices = parser.readValueAs(int[].class);
        }
        
        return request;
    }
    
    private int[] toArray(List<Integer> list) {
        int[] array = new int[list.size()];
        
        for(int i = 0; i < array.length; i++) {
            array[i] = list.get(i);
        }
        
        return array;
    }
}
