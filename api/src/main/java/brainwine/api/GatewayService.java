package brainwine.api;

import static brainwine.shared.LogMarkers.SERVER_MARKER;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import brainwine.api.handlers.NewsRequestHandler;
import brainwine.api.handlers.PasswordForgotHandler;
import brainwine.api.handlers.PasswordResetHandler;
import brainwine.api.handlers.PlayerLoginHandler;
import brainwine.api.handlers.PlayerRegistrationHandler;
import brainwine.api.handlers.RwcPurchaseHandler;
import brainwine.api.handlers.SimpleExceptionHandler;
import brainwine.shared.JsonHelper;
import io.javalin.Javalin;
import io.javalin.plugin.json.JavalinJackson;

public class GatewayService {
    
    private static final Logger logger = LogManager.getLogger();
    private final Javalin gateway;
    
    public GatewayService(Api api, int port) {
        logger.info(SERVER_MARKER, "Starting GatewayService @ port {} ...", port);
        DataFetcher dataFetcher = api.getDataFetcher();
        String gameServerHost = api.getGameServerHost();
        gateway = Javalin.create(config -> config.jsonMapper(new JavalinJackson(JsonHelper.MAPPER))).start(port);
        gateway.exception(Exception.class, new SimpleExceptionHandler());
        gateway.get("/clients", new NewsRequestHandler(api.getNews()));
        gateway.post("/players", new PlayerRegistrationHandler(dataFetcher, gameServerHost));
        gateway.post("/sessions", new PlayerLoginHandler(dataFetcher, gameServerHost));
        gateway.post("/passwords/request", new PasswordForgotHandler());
        gateway.post("/passwords/reset", new PasswordResetHandler());
        gateway.post("/purchases", new RwcPurchaseHandler());
    }
    
    public void stop() {
        gateway.stop();
    }
}
