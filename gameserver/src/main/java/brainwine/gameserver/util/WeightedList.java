package brainwine.gameserver.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonCreator;

import io.netty.util.internal.ThreadLocalRandom;

public class WeightedList<T> {
    
    private final List<T> entries = new ArrayList<>();
    
    @JsonCreator
    public WeightedList(Map<T, Integer> map) {
        map.forEach((k, v) -> {
            addEntry(k, v);
        });
    }
    
    @JsonCreator
    public WeightedList(List<T> list) {
        for(T t : list) {
            addEntry(t, 1);
        }
    }
    
    public WeightedList() {}
    
    public WeightedList<T> addEntry(T entry, int weight) {
        for(int i = 0; i < weight; i++) {
            entries.add(entry);
        }
        
        return this;
    }
    
    public T next() {
        return next((T)null);
    }
    
    public T next(T def) {
        return next(ThreadLocalRandom.current(), def);
    }
    
    public T next(Random random) {
        return next(random, null);
    }
    
    public T next(Random random, T def) {
        if(entries.isEmpty()) {
            return def;
        }
        
        return entries.get(random.nextInt(entries.size()));
    }
    
    public boolean isEmpty() {
        return entries.isEmpty();
    }
}
