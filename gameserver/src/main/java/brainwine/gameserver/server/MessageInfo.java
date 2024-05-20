package brainwine.gameserver.server;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MessageInfo {
    
    public int id();
    
    /**
     * Defines whether or not this message should be serialized as JSON.
     * If false, it will be serialized using MessagePack.
     * 
     * The default value is false.
     */
    public boolean json() default false;
    
    /**
     * Defines whether or not this message should be compressed after serialization.
     * 
     * The default value is false.
     */
    public boolean compressed() default false;
    
    /**
     * Defines whether or not the values of this message should be part of a collection,
     * allowing for multiple messages to be sent in a single packet.
     * This feature is not supported and this option only exists for client compatibility,
     * but you can use the {@code prepacked} option to achieve the same result.
     * 
     * The default value is false.
     */
    public boolean collection() default false;
    
    /**
     * Defines whether or not this message is prepacked i.e. all the values are already
     * part of a {@code Collection} within the message class.
     * 
     * The default value is false.
     */
    public boolean prepacked() default false;
}
