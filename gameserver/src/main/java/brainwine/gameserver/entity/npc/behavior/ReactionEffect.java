package brainwine.gameserver.entity.npc.behavior;

import java.util.List;
import java.util.function.Predicate;

/**Represents an effect which the entity may react to.
 * It infers the type of the effect sent from the client and calls the lambda passed to the correct handler.
 * 
 * This is mainly to handle cases of users clicking/tapping onto an android.
 * 
 * Look at the documentation of methods starting with on- to see what case they handle.
 */
public class ReactionEffect {
    private Type type;
    private Object data;
    private boolean handled = false;

    private ReactionEffect(Type type, Object data) {
        this.type = type;
        this.data = data;
    }

    @SuppressWarnings({ "", "unchecked" })
    private <T> ReactionEffect handle(Type type, Predicate<T> handler) {
        if(this.type == type) {
            handled = handled || handler.test((T)data);
        }

        return this;
    }

    public void clearHandled() {
        handled = false;
    }

    public boolean isHandled() {
        return handled;
    }

    // Add new types of effects below this line.

    private static enum Type {
        INTERACT,
        DROP_ITEM_ONTO,
        UNKNOWN,
    }

    public static ReactionEffect interact(Object data) {
        if(data instanceof List) {
            List<Object> list = (List<Object>)data;

            if(list.size() < 2) {
                return interact(list.get(0));
            }
            
            if("item".equals(list.get(0))) {
                return new ReactionEffect(Type.DROP_ITEM_ONTO, list.get(1));
            }
        } else if(data instanceof Integer) {
            return new ReactionEffect(Type.INTERACT, data);
        }

        return new ReactionEffect(Type.UNKNOWN, data);
    }

    /**
     * When the user taps onto the android
     * 
     * @param fn callback function. Return true iff the interaction is wholly handled.
     * @return itself
     */
    public ReactionEffect onInteract(Predicate<Integer> fn) {
        return handle(Type.INTERACT, fn);
    }

    /**
     * When the user drags an item onto the android
     * 
     * @param fn callback function. Return true iff the interaction is wholly handled.
     * @return itself
     */
    public ReactionEffect onDropItemOnto(Predicate<Integer> fn) {
        return handle(Type.DROP_ITEM_ONTO, fn);
    }

}
