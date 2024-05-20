package brainwine.gameserver.serialization;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import brainwine.gameserver.achievement.Achievement;

/**
 * Seriously, Jackson has the stupidest problems sometimes.
 */
public class AchievementSerializer extends StdSerializer<Achievement> {
    
    public static final AchievementSerializer INSTANCE = new AchievementSerializer();
    private static final long serialVersionUID = 7660825036762291561L;
    
    protected AchievementSerializer() {
        super(Achievement.class);
    }
    
    @Override
    public void serialize(Achievement achievement, JsonGenerator generator, SerializerProvider provider) throws IOException {
        generator.writeString(achievement.getTitle());
    }
    
    @Override
    public void serializeWithType(Achievement achievement, JsonGenerator generator, SerializerProvider provider,
            TypeSerializer typeSerializer) throws IOException {
        serialize(achievement, generator, provider);
    }
}
