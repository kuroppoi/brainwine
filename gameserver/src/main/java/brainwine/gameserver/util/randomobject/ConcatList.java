package brainwine.gameserver.util.randomobject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ConcatList<T> extends RandomList<T> {
    private List<RandomList<T>> concat;

    public ConcatList(List<RandomList<T>> concat) {
        this.concat = concat;
    }

    @Override
    public ConstantList<T> next(Random random) throws ConcretionFailureException {
        if(concat == null) return new ConstantList<>();

        List<T> result = new ArrayList<T>();
        for(RandomList<T> l : concat) {
            result.addAll(l.next(random).getList());
        }

        return new ConstantList<T>(result);
    }

    @Override
    public String toString() {
        return "ConcatList(" + concat.stream().map(r -> r.toString()).collect(Collectors.joining(", ")) + ")";
    }
    
}
