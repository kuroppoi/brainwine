package brainwine.gameserver.item;

import brainwine.gameserver.util.LazyGetter;

public class LazyItemGetter extends LazyGetter<String, Item> {

    public LazyItemGetter(String in) {
        super(in);
    }
    
    @Override
    public Item load() {
        return ItemRegistry.getItem(in);
    }
}
