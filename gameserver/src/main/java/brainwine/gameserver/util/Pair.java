package brainwine.gameserver.util;

import java.beans.ConstructorProperties;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
public class Pair<K, V> {
    
    private final K first;
    private final V last;
    
    @ConstructorProperties({"first", "last"})
    public Pair(K first, V last) {
        this.first = first;
        this.last = last;
    }
    
    public K getFirst() {
        return first;
    }
    
    public V getLast() {
        return last;
    }
}
