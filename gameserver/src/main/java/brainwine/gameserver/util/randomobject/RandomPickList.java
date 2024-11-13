package brainwine.gameserver.util.randomobject;

import java.util.List;
import java.util.Random;

import brainwine.gameserver.util.PickRandom;

public class RandomPickList<T> extends RandomList<T> {
    RandomList<T> choices;
    RandomInteger pick;

    public RandomPickList(RandomList<T> choices) {
        this.choices = choices;
        this.pick = new RandomInteger(1);
    }

    public RandomPickList(RandomList<T> choices, RandomInteger pick) {
        this.choices = choices;
        this.pick = pick;
    }

    @Override
    public List<T> next(Random random) throws ConcretionFailureException {
        List<T> list = choices.next(random);
        int count = pick.next(random);
        List<T> chosen = PickRandom.<T>sampleWithoutReplacement(random, list, count);

        return chosen;
    }

    @Override
    public String toString() {
        return "RandomPickList(" + pick + " of " + choices + ")";
    }
    
}
