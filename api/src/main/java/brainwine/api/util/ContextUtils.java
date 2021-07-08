package brainwine.api.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import io.javalin.core.validation.Validator;
import io.javalin.http.Context;

public class ContextUtils {
    
    public static void error(Context ctx, String message, Object... args) {
        Map<String, Object> map = new HashMap<>();
        map.put("error", String.format(message, args));
        ctx.json(map);
    }
    
    public static <T> void handleQueryParam(Context ctx, String key, Class<T> type, Consumer<T> handler) {
        Validator<T> param = ctx.queryParam(key, type);
        T value = param.getOrNull();
        
        if(value != null) {
            handler.accept(value);
        }
    }
}
