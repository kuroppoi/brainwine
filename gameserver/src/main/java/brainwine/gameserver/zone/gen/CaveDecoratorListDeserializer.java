package brainwine.gameserver.zone.gen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import brainwine.gameserver.zone.gen.models.CaveType;
import brainwine.shared.JsonHelper;

/**
 * It's a bit hack-ish, but it works.
 */
public class CaveDecoratorListDeserializer extends JsonDeserializer<List<CaveDecorator>> {

    @Override
    public List<CaveDecorator> deserialize(JsonParser parser, DeserializationContext ctx) throws IOException, JsonProcessingException {
        List<CaveDecorator> list = new ArrayList<>();
        JsonNode node = parser.readValueAsTree();
        Iterator<Entry<String, JsonNode>> it = node.fields();
        
        while(it.hasNext()) {
            Entry<String, JsonNode> entry = it.next();
            CaveType type = JsonHelper.readValue(String.format("\"%s\"", entry.getKey()), CaveType.class);
            CaveDecorator decorator = JsonHelper.readValue(entry.getValue().toString(), type.getDecoratorType());
            list.add(decorator);
        }
        
        return list;
    }

}
