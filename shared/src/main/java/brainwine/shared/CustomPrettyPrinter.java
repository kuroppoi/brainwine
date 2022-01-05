package brainwine.shared;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter.Indenter;
import com.fasterxml.jackson.core.util.Instantiatable;

public class CustomPrettyPrinter implements PrettyPrinter, Instantiatable<CustomPrettyPrinter> {
    
    public static final CustomPrettyPrinter INSTANCE = new CustomPrettyPrinter();
    private static final Indenter indenter = DefaultIndenter.SYSTEM_LINEFEED_INSTANCE;
    private int nesting;
    
    private CustomPrettyPrinter() {}
    
    @Override
    public CustomPrettyPrinter createInstance() {
        return new CustomPrettyPrinter();
    }
    
    @Override
    public void writeRootValueSeparator(JsonGenerator gen) throws IOException {
        gen.writeRaw(DefaultPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR);
    }
    
    @Override
    public void writeStartObject(JsonGenerator gen) throws IOException {
        gen.writeRaw('{');
        nesting++;
    }
    
    @Override
    public void writeEndObject(JsonGenerator gen, int nrOfEntries) throws IOException {
        nesting--;
        
        if(nrOfEntries > 0) {
            indenter.writeIndentation(gen, nesting);
        }
        
        gen.writeRaw('}');
    }
    
    @Override
    public void writeObjectEntrySeparator(JsonGenerator gen) throws IOException {
        gen.writeRaw(',');
        indenter.writeIndentation(gen, nesting);
    }
    
    @Override
    public void writeObjectFieldValueSeparator(JsonGenerator gen) throws IOException {
        gen.writeRaw(": ");
    }
    
    @Override
    public void writeStartArray(JsonGenerator gen) throws IOException {
        gen.writeRaw('[');
        nesting++;
    }
    
    @Override
    public void writeEndArray(JsonGenerator gen, int nrOfValues) throws IOException {
        nesting--;
        
        if(nrOfValues > 0) {
            indenter.writeIndentation(gen, nesting);
        }
        
        gen.writeRaw(']');
    }
    
    @Override
    public void writeArrayValueSeparator(JsonGenerator gen) throws IOException {
        gen.writeRaw(',');
        indenter.writeIndentation(gen, nesting);
    }
    
    @Override
    public void beforeArrayValues(JsonGenerator gen) throws IOException {
        indenter.writeIndentation(gen, nesting);
    }
    
    @Override
    public void beforeObjectEntries(JsonGenerator gen) throws IOException {
        indenter.writeIndentation(gen, nesting);
    }
}
