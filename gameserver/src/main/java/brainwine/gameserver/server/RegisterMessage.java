package brainwine.gameserver.server;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RegisterMessage {
	
	int id();
	boolean json() default false;
	boolean compressed() default false;
	boolean collection() default false;
	boolean prepacked() default false;
}
