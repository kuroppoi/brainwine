package brainwine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

@Plugin(name = "Listenable",
    category = Core.CATEGORY_NAME,
    elementType = Appender.ELEMENT_TYPE, 
    printObject = true)
public class ListenableAppender extends AbstractAppender {
    
    protected static final List<Consumer<LogMessage>> listeners = new ArrayList<>();
    
    protected ListenableAppender(String name, Filter filter,
            Layout<? extends Serializable> layout, boolean ignoreExceptions,
            Property[] properties) {
        super(name, filter, layout, ignoreExceptions, properties);
    }
    
    @PluginFactory
    public static ListenableAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("filter") Filter filter,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginAttribute("ignoreExceptions") boolean ignoreExceptions) {
        return new ListenableAppender(name, filter, layout, ignoreExceptions, null);
    }
    
    public static void addListener(Consumer<LogMessage> listener) {
        synchronized(listeners) {
            listeners.add(listener);
        }
    }
    
    @Override
    public void append(LogEvent event) {
        String message = event.getMessage().getFormattedMessage();
        String formattedMessage = getLayout().toSerializable(event).toString();
        LogMessage logMessage = new LogMessage(event.getLevel(), message, formattedMessage);
        
        synchronized(listeners) {
            for(Consumer<LogMessage> listener : listeners) {
                listener.accept(logMessage);
            }
        }
    }
}
