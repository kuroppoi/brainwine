package brainwine.gameserver.util.randomobject;

import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = RandomListDeserializer.class)
public abstract class RandomList<T> implements Arbitrary<ConstantList<T>> {
    public List<T> toConcrete(Random random) throws ConcretionFailureException {
        ConstantList<T> finalList = next(random);
        return finalList.getList();
    }
}
