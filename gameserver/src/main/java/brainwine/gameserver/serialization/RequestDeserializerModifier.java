package brainwine.gameserver.serialization;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;

import brainwine.gameserver.server.Request;

public class RequestDeserializerModifier extends BeanDeserializerModifier {
    
    public static final RequestDeserializerModifier INSTANCE = new RequestDeserializerModifier();
    
    private RequestDeserializerModifier() {}
    
    @Override
    @SuppressWarnings("unchecked")
    public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
        Class<?> clazz = beanDesc.getBeanClass();
        
        if(Request.class.isAssignableFrom(clazz)) {
            return new RequestDeserializer((Class<? extends Request>)clazz);
        }
        
        return deserializer;
    }
}
