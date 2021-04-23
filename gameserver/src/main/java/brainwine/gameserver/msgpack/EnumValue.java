package brainwine.gameserver.msgpack;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Can be assigned to a field in an Enumeration.
 * When packed, the value of the first field with this annotation will be used instead of the ordinal.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumValue {}
