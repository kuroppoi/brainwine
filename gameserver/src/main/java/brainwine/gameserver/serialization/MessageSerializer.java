package brainwine.gameserver.serialization;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import brainwine.gameserver.server.Message;

public class MessageSerializer extends StdSerializer<Message> {
    
    public static final MessageSerializer INSTANCE = new MessageSerializer();
    private static final long serialVersionUID = 2310652788158728087L;

    protected MessageSerializer() {
        super(Message.class);
    }

    @Override
    public void serialize(Message message, JsonGenerator generator, SerializerProvider serializers) throws IOException {
        Field[] fields = message.getClass().getFields();
        
        try {
            if(message.isPrepacked()) {
                if(fields.length != 1 || !Collection.class.isAssignableFrom(fields[0].getType())) {
                    throw new IOException("Invalid prepacked message.");
                }
                
                generator.writeObject(fields[0].get(message));
            } else {
                List<Object> fieldValues = new ArrayList<>();
                
                for(Field field : fields) {
                    fieldValues.add(field.get(message));
                }
                
                if(message.isCollection()) {
                    generator.writeObject(Arrays.asList(fieldValues));
                } else {
                    generator.writeObject(fieldValues);
                }
            }
        } catch(IllegalArgumentException | IllegalAccessException e) {
            throw new IOException(e);
        }
    }
}
