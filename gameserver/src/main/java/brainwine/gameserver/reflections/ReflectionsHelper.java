package brainwine.gameserver.reflections;

import java.lang.annotation.Annotation;
import java.util.Set;

import org.reflections.Reflections;

/**
 * Simple helper class for the Reflections library.
 */
public class ReflectionsHelper {
    
    private static final Reflections reflections = new Reflections("brainwine.gameserver");
    
    public static Set<Class<?>> getTypesAnnotatedWith(Class<? extends Annotation> annotation) {
        return reflections.getTypesAnnotatedWith(annotation);
    }
}
