package brainwine.gameserver.util.randomobject;

import java.util.Random;

import io.netty.util.internal.ThreadLocalRandom;

public interface Arbitrary<T> {
    T next(Random random) throws ConcretionFailureException;

    default T next() throws ConcretionFailureException {
        return next(ThreadLocalRandom.current());
    }
}
