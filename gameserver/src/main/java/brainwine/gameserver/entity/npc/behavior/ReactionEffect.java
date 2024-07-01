package brainwine.gameserver.entity.npc.behavior;

import java.util.List;

import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Consumer;

public class ReactionEffect {
    private Type type;
    private Object data;

    private ReactionEffect(Type type, Object data) {
        this.type = type;
        this.data = data;
    }

    private <T> ReactionEffect handle(Type type, Consumer<T> handler) {
        if(this.type == type) {
            try {
                handler.accept((T)data);
            }
            catch(ClassCastException e) {}
        }

        return this;
    }

    // Add new types of effects below this line.

    private static enum Type {
        INTERACT,
    }

    public static ReactionEffect interact(Object data) {
        List<Object> myData;
        if (data instanceof List) {
            myData = (List<Object>)data;
        } else {
            myData = List.of(data);
        }
        return new ReactionEffect(Type.INTERACT, myData);
    }

    public ReactionEffect onInteract(Consumer<List<Integer>> fn) {
        return handle(Type.INTERACT, fn);
    }

}
