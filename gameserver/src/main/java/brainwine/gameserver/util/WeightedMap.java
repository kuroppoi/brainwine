package brainwine.gameserver.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import io.netty.util.internal.ThreadLocalRandom;

public class WeightedMap<T> {
    
    @JsonValue
    private final Map<T, Double> map = new HashMap<>();
    private double totalWeight;
    
    public WeightedMap() {}
    
    public WeightedMap(Collection<T> entries, Function<T, Number> weightFunction) {
        for(T entry : entries) {
            addEntry(entry, weightFunction.apply(entry).doubleValue());
        }
    }
    
    @JsonCreator
    public WeightedMap(Collection<T> entries) {
        for(T entry : entries) {
            addEntry(entry);
        }
    }
    
    @JsonCreator
    public WeightedMap(Map<T, Double> map) {
        map.forEach((entry, weight) -> {
            addEntry(entry, weight);
        });
    }
    
    public WeightedMap<T> addEntry(T entry) {
        return addEntry(entry, 1);
    }
    
    public WeightedMap<T> addEntry(T entry, double weight) {
        if(weight > 0 && entry != null) {
            map.put(entry, weight);
            totalWeight += weight;
        }
        
        return this;
    }
    
    public boolean isEmpty() {
        return map.isEmpty();
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
        if(!map.isEmpty()) {
            double rolled = random.nextDouble() * totalWeight;
            
            for(Entry<T, Double> entry : map.entrySet()) {
                double weight = entry.getValue();
                
                if(rolled < weight) {
                    return entry.getKey();
                }
                
                rolled -= weight;
            }
        }
        
        return def;
    }
}
