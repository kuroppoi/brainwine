package brainwine.gameserver.util.randomobject;

import brainwine.shared.JsonHelper;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class RandomListDeserializer<T> extends StdDeserializer<RandomList<T>> implements ContextualDeserializer {
    private Class<T> itemType;
    private ObjectMapper mapper;

    public RandomListDeserializer() {
        this(null, null);
    }

    public RandomListDeserializer(Class<T> itemType, ObjectMapper mapper) {
        super(RandomList.class);
        this.itemType = itemType;
        this.mapper = mapper;
    }

    @Override
    public RandomList<T> deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException
    {
        JsonNode node = parser.readValueAsTree();

        return deserialize(node, context);
    }

    private RandomList<T> deserialize(JsonNode node, DeserializationContext context) throws IOException {
        if(node.isObject()) {
            if(node.hasNonNull("choices")) {
                RandomList<T> choices = deserialize(node.get("choices"), context);
                if(node.has("pick")) {
                    RandomInteger pick = mapper.readValue(mapper.treeAsTokens(node.get("pick")), RandomInteger.class);
                    return new RandomPickList<>(choices, pick);
                } else {
                    return new RandomPickList<>(choices);
                }

            }

            if(node.hasNonNull("concat")) {
                List<RandomList<T>> concatList = new ArrayList<>();
                JsonNode concatNode = node.get("concat");
                for(int i = 0; i < concatNode.size(); i++) {
                    JsonNode current = concatNode.get(i);
                    concatList.add(deserialize(current, context));
                }
                return new ConcatList<>(concatList);
            }
        }

        if(node.isArray()) {
            List<T> arr = new ArrayList<>();
            for(int i = 0; i < node.size(); i++) {
                JsonNode current = node.get(i);
                arr.add(mapper.readValue(mapper.treeAsTokens(current), itemType));
            }
            return new ConstantList<>(arr);
        }

        return null;
    }

    /** Standard procedure to resolve some annotation. */
    private <T extends Annotation> T getAnnotation(BeanProperty property, Class<T> clazz) throws Exception {
        T value = property.getMember().getAnnotation(clazz);
        if(value == null) value = property.getMember().getDeclaringClass().getAnnotation(clazz);
        if(value == null) throw new Exception(clazz.getName() + " is missing!");

        return value;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext context, BeanProperty property) {
        try {
            if(property == null) {
                throw new Exception("BeanProperty is missing!");
            }

            RandomListItemType itemType = getAnnotation(property, RandomListItemType.class);

            return new RandomListDeserializer<>(itemType.value(), JsonHelper.MAPPER);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
