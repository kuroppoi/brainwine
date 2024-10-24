package brainwine.gameserver.util.randomobject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import brainwine.shared.JsonHelper;

public abstract class RandomList<T> implements Arbitrary<ConstantList<T>> {
    @JsonCreator
    public static <T> RandomList<T> create(List<Object> list) throws JsonProcessingException {
        ConstantList<T> result = new ConstantList<T>();

        for (Object o : list) {
            result.getList().add(result.createItem(o));
        }

        return result;
    }

    @JsonCreator
    public static <T> RandomList<T> create(Map<String, Object> map) throws JsonProcessingException {
        if(map.containsKey("pick") && map.containsKey("choices")) {
            return RandomPickList.create(map);
        }

        if(map.containsKey("concat")) {
            return ConcatList.create(map);
        }

        throw new JsonMappingException("Could not infer the kind of random list.");
    }

    public static <T> RandomList<T> create(Object o) throws JsonProcessingException {
        if (o instanceof List) return create((List<Object>) o);
        if (o instanceof Map) return create((Map<String, Object>) o);

        else throw new JsonMappingException("Value is not a random list!");
    }

    protected T createItem(Object obj) throws JsonProcessingException {
        return JsonHelper.readValue(obj, new TypeReference<T>() {});
    }

    public List<T> toConcrete(Random random) throws ConcretionFailureException {
        ConstantList<T> finalList = next(random);

        List<T> result = new ArrayList<>(finalList.size());
        for (Object o : finalList.getList()) {
            result.add((T) o);
        }

        return result;
    }
}
