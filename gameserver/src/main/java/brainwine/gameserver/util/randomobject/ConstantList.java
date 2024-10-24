package brainwine.gameserver.util.randomobject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ConstantList<T> extends RandomList<T> {
    List<T> list;

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
}
