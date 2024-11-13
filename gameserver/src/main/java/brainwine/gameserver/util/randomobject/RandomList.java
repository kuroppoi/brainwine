package brainwine.gameserver.util.randomobject;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = RandomListDeserializer.class)
public abstract class RandomList<T> implements Arbitrary<List<T>> {}
