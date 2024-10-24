package brainwine.gameserver.util.randomobject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class ConcatList<T> extends RandomList<T> {
    List<RandomList<T>> lists;

    public ConcatList() {}
    public ConcatList(List<RandomList<T>> lists) {
        this.lists = lists;
    }

    public static <V> ConcatList<V> create(Map<String, Object> inp) throws JsonProcessingException {
        Object inpList = inp.get("concat");

        if (inpList instanceof List) {
            List<RandomList<V>> concat = new ArrayList<>();

            for (Object o : (List<Object>)inpList) {
                concat.add(RandomList.create(o));
            }

            return new ConcatList<>(concat);
        }

        throw new JsonMappingException("Concat list doesn't have a list of lists.");
    }

    @Override
    public ConstantList<T> next(Random random) throws ConcretionFailureException {
        if (lists == null) return null;

        List<T> result = new ArrayList<T>();
        for (RandomList<T> l : lists) {
            result.addAll(l.next(random).getList());
        }

        return new ConstantList<T>(result);
    }
    
}
