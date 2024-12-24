package brainwine.gameserver.util.randomobject;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface ObjectMapperProvider {
    ObjectMapper get();
}
