package brainwine.gameserver.util.randomobject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import brainwine.gameserver.util.MapHelper;
import brainwine.gameserver.util.PickRandom;
import brainwine.shared.JsonHelper;

public class RandomPickList<T> extends RandomList<T> {

    RandomList<T> options;
    RandomInteger pick;

    public static <V> RandomPickList<V> create(Map<String, Object> inp) throws JsonProcessingException {
        RandomList<V> options;

        Object inpChoices = inp.get("choices");
        if(inpChoices instanceof List) options = RandomList.<V>create((List<Object>) inpChoices);
        else if(inpChoices instanceof Map) options = RandomList.<V>create((Map<String, Object>) inpChoices);

        else throw new JsonMappingException("Random pick list does not have a list of choices.");

        RandomInteger pick = JsonHelper.readValue(inp.get("pick"), RandomInteger.class);
        return new RandomPickList<V>(options, pick);
    }

    public RandomPickList(RandomList<T> options, RandomInteger pick) {
        this.options = options;
        this.pick = pick;
    }

    @Override
    public ConstantList<T> next(Random random) throws ConcretionFailureException {
        ConstantList<T> list = options.next(random);
        int count = pick.next(random);
        List<T> chosen = PickRandom.<T>sampleWithoutReplacement(random, list.getList(), count);

        return new ConstantList<>(chosen);
    }
    
}
