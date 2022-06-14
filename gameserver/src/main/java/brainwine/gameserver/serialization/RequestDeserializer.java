package brainwine.gameserver.serialization;

import java.io.IOException;
import java.lang.reflect.Field;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import brainwine.gameserver.annotations.OptionalField;
import brainwine.gameserver.server.Request;

public class RequestDeserializer extends StdDeserializer<Request> {
    
    private static final long serialVersionUID = 42527921659694141L;
    
    public RequestDeserializer(Class<? extends Request> type) {
        super(type);
    }
    
    @Override
    public Request deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
        try {
            Request request = (Request)_valueClass.newInstance();
            Field[] fields = request.getClass().getFields();
            
            if(parser.currentToken() != JsonToken.START_ARRAY) {
                throw new IOException("Got invalid token, expected START_ARRAY");
            }
            
            for(Field field : fields) {
                boolean required = field.getAnnotation(OptionalField.class) == null;
                JsonToken token = parser.nextToken();
                
                if(token == JsonToken.VALUE_NULL) {
                    if(required) {
                        throw new IOException("Value is null, but field is required!");
                    }
                    
                    continue;
                } else if(token == JsonToken.END_ARRAY) {
                    if(required) {
                        throw new IOException("Array is end, but field is required!");
                    }
                    
                    break;
                }
                
                Object value = context.findRootValueDeserializer(context.constructType(field.getGenericType())).deserialize(parser, context);
                field.set(request, value);
            }
            
            return request;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IOException(e);
        }
    }
}
