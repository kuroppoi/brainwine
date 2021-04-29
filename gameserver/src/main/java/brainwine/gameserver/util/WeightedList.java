package brainwine.gameserver.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WeightedList<T> {
    
    private final List<T> entries = new ArrayList<>();
    
    public void addEntry(T entry, int weight) {
        for(int i = 0; i < weight; i++) {
            entries.add(entry);
        }
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
