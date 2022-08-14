package brainwine.shared;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class JsonHelper {
    
    public static final ObjectMapper MAPPER = JsonMapper.builder()
            .findAndAddModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
            .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, MapperFeature.USE_BASE_TYPE_AS_DEFAULT_IMPL)
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .build();
    private static final ObjectWriter writer = MAPPER.writer(CustomPrettyPrinter.INSTANCE);
    
    public static <T> T readValue(String string, Class<T> type) throws JsonMappingException, JsonProcessingException {
        return MAPPER.readValue(string, type);
    }
    
    public static <T> T readValue(File file, Class<T> type) throws JsonParseException, JsonMappingException, IOException {
        return MAPPER.readValue(file, type);
    }
    
    public static <T> T readValue(Object object, Class<T> type) throws JsonProcessingException {
        return readValue(writeValueAsString(object), type);
    }
    
    public static <T> T readValue(String string, TypeReference<T> type) throws JsonMappingException, JsonProcessingException {
        return MAPPER.readValue(string, type);
    }
    
    public static <T> T readValue(File file, TypeReference<T> type) throws IOException {
        return MAPPER.readValue(file, type);
    }
    
    public static <T> T readValue(Object object, TypeReference<T> type) throws JsonProcessingException {
        return readValue(writeValueAsString(object), type);
    }
    
    public static <T> T readValue(String string, Class<T> type, InjectableValues injectableValues) throws JsonMappingException, JsonProcessingException {
        return MAPPER.readerFor(type).with(injectableValues).readValue(string);
    }
    
    public static <T> T readValue(File file, Class<T> type, InjectableValues injectableValues) throws JsonParseException, JsonMappingException, IOException {
        return MAPPER.readerFor(type).with(injectableValues).readValue(file);
    }
    
    public static <T> T readValue(Object object, Class<T> type, InjectableValues injectableValues) throws JsonProcessingException {
        return readValue(writeValueAsString(object), type, injectableValues);
    }
    
    public static <T> List<T> readList(File file, Class<T> type) throws IOException {
        return MAPPER.readerForListOf(type).readValue(file);
    }
    
    public static void writeValue(File file, Object value) throws JsonGenerationException, JsonMappingException, IOException {
        writer.writeValue(file, value);
    }
    
    public static String writeValueAsString(Object value) throws JsonProcessingException {
        return writer.writeValueAsString(value);
    }
    
    public static byte[] writeValueAsBytes(Object value) throws JsonProcessingException {
        return writer.writeValueAsBytes(value);
    }
}
