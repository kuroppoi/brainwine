package brainwine.shared;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class OffsetDateTimeSerializer extends StdSerializer<OffsetDateTime> {
    
    public static final OffsetDateTimeSerializer INSTANCE = new OffsetDateTimeSerializer();
    private static final long serialVersionUID = 7309329981624380784L;
    private static final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd")
            .appendLiteral('T')
            .appendPattern("HH:mm:ss.SSS")
            .appendOffsetId()
            .toFormatter();
    
    protected OffsetDateTimeSerializer() {
        super(OffsetDateTime.class);
    }
    
    @Override
    public void serialize(OffsetDateTime dateTime, JsonGenerator generator, SerializerProvider provider) throws IOException {
        generator.writeString(formatter.format(dateTime));
    }
}
