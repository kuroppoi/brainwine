package brainwine.gameserver.util.randomobject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ConstantList<T> extends RandomList<T> {
    private List<T> list;

    public ConstantList() {
        this.list = new ArrayList<>();
    }

    public ConstantList(List<T> list) {
        this.list = list;
    }
    
    @Override
    public ConstantList<T> next(Random random) {
        return this;
    }

    public int size() {
        return list.size();
    }

    public List<T> getList() {
        return list;
    }

    @Override
    public String toString() {
        return "ConstantList(" + list.stream().map(r -> r.toString()).collect(Collectors.joining(", ")) + ")";
    }
}
