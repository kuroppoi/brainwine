package brainwine.api.util;

import java.util.HashMap;
import java.util.Map;

import io.javalin.http.Context;

public class ContextUtils {
    
    public static void error(Context ctx, String message, Object... args) {
        Map<String, Object> map = new HashMap<>();
        map.put("error", String.format(message, args));
        ctx.json(map);
    }
}
