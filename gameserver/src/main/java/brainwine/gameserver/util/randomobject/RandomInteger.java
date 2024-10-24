package brainwine.gameserver.util.randomobject;

import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonMappingException;

public class RandomInteger implements Arbitrary<Integer> {
    private boolean isConcrete = false;
    private Integer value = null;
    private List<Integer> randomBetween = null;

    public RandomInteger() {}

    @JsonCreator
    public RandomInteger(Integer value) {
        this.value = value;
        this.isConcrete = true;
    }

    @JsonCreator
    public RandomInteger(@JsonProperty("random_between") List<Integer> randomBetween ) throws JsonMappingException {
        if (!(randomBetween.size() == 2 || randomBetween.size() == 3)) {
            throw new JsonMappingException("Invalid input for random_between: provide either 2 or 3 constant numbers");
        }

        if (randomBetween.get(0) > randomBetween.get(1)) {
            throw new JsonMappingException("Invalid input for random_between: range min is greater than range max");
        }

        if (randomBetween.size() == 3 && randomBetween.get(2) <= 0) {
            throw new JsonMappingException("Invalid input for random_between: only positive step values are allowed");
        }
        this.randomBetween = randomBetween;
    }

    @Override
    public Integer next(Random random) {
        if (this.isConcrete) {
            return value;
        } else {
            int step = 1;
            if(randomBetween.size() == 3) {
                step = randomBetween.get(2);
            }
            return step * random.nextInt(randomBetween.get(0) / step, randomBetween.get(1) / step + 1);
        }
    }
    
}
