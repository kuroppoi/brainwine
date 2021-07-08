package brainwine.api.handlers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import brainwine.api.config.NewsEntry;
import io.javalin.http.Context;
import io.javalin.http.Handler;

public class NewsRequestHandler implements Handler {
    
    private final Map<String, Object> news = new HashMap<>();
    
    public NewsRequestHandler(Collection<NewsEntry> posts) {
        news.put("posts", posts);
    }
    
    @Override
    public void handle(Context ctx) throws Exception {
        ctx.json(news);
    }
}
