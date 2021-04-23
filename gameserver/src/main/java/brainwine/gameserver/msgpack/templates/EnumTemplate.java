package brainwine.gameserver.msgpack.templates;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.type.ValueType;
import org.msgpack.unpacker.Unpacker;

import brainwine.gameserver.msgpack.EnumValue;

public class EnumTemplate<T> extends AbstractTemplate<T> {
    
    private final Map<T, Object> ids = new HashMap<>();
    private final Map<Object, T> values = new HashMap<>();
    
    public EnumTemplate(Class<T> type) {
        T[] entries = type.getEnumConstants();
        
        for(Field field : type.getFields()) {
            if(field.isAnnotationPresent(EnumValue.class)) {
                try {
                    for(T entry : entries) {
                        Object id = field.get(entry);
                        ids.put(entry, id);
                        values.put(id, entry);
                    }
                    
                    return;
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new MessageTypeException(e);
                }
            }
        }
        
        for(Method method : type.getMethods()) {
            if(method.isAnnotationPresent(EnumValue.class)) {
                try {
                    for(T entry : entries) {
                        Object id = method.invoke(entry);
                        ids.put(entry, id);
                        values.put(id, entry);
                    }
                    
                    return;
                } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                    throw new MessageTypeException(e);
                }
            }
        }
        
        for(int i = 0; i < entries.length; i++) {
            ids.put(entries[i], i);
            values.put(i, entries[i]);
        }
    }
    
    @Override
    public void write(Packer packer, T target, boolean required) throws IOException {
        if(target == null) {
            if(required) {
                throw new MessageTypeException("Attempted to write null");
            }
            
            packer.writeNil();
            return;
        }
        
        // Dangerous, might throw an NPE
        packer.write(ids.get(target));
    }
    
    @Override
    public T read(Unpacker unpacker, T to, boolean required) throws IOException {
        if(!required && unpacker.trySkipNil()) {
            return null;
        }
        
        ValueType next = unpacker.getNextType();
        
        if(next == ValueType.INTEGER) {
            return values.get(unpacker.readInt());
        } else if(next == ValueType.RAW) {
            return values.get(unpacker.readString());
        }
        
        throw new MessageTypeException("Unsupported enum id type");
    }
}
