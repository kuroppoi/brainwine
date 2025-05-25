package brainwine.api;

import static brainwine.api.util.ContextUtils.error;
import static brainwine.shared.LogMarkers.SERVER_MARKER;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import brainwine.api.models.PlayersRequest;
import brainwine.api.models.ServerConnectInfo;
import brainwine.api.models.SessionsRequest;
import brainwine.shared.JsonHelper;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.plugin.json.JavalinJackson;

public class GatewayService {
    
    private static final Pattern namePattern = Pattern.compile("^[a-zA-Z0-9_.-]{4,20}$");
    private static final Logger logger = LogManager.getLogger();
    private final Api api;
    private final DataFetcher dataFetcher;
    private final Javalin gateway;
    
    public GatewayService(Api api, int port) {
        this.api = api;
        this.dataFetcher = api.getDataFetcher();
        logger.info(SERVER_MARKER, "Starting GatewayService @ port {} ...", port);
        gateway = Javalin.create(config -> config.jsonMapper(new JavalinJackson(JsonHelper.MAPPER)))
            .exception(Exception.class, this::handleException)
            .get("/clients", this::handleNewsRequest)
            .post("/players", this::handlePlayerRegistration)
            .post("/sessions", this::handlePlayerLogin)
            .post("/passwords/request", this::handlePasswordForget)
            .post("/passwords/reset", this::handlePasswordReset)
            .post("/purchases", this::handleInAppPurchase)
            .start(port);
    }
    
    /**
     * Exception handler function.
     */
    private void handleException(Exception exception, Context ctx) {
        logger.error(SERVER_MARKER, "Exception caught", exception);
        error(ctx, "%s", exception);
    }
    
    /**
     * Handler function for news requests. (main menu)
     */
    private void handleNewsRequest(Context ctx) {
        Map<String, Object> news = new HashMap<>();
        news.put("posts", api.getNews());
        ctx.json(news);
    }
    
    /**
     * Handler function for registering a new account.
     */
    private void handlePlayerRegistration(Context ctx) {
        PlayersRequest request = ctx.bodyValidator(PlayersRequest.class).get();
        String name = request.getName();
        
        // Check if name is too short, too long or contains illegal characters
        if(!namePattern.matcher(name).matches()) {
            error(ctx, "Please enter a valid username.");
            return;
        }
        
        // Check if a player with this name already exists
        if(dataFetcher.isPlayerNameTaken(name)) {
            error(ctx, "Sorry, this username has already been taken.");
            return;
        }
        
        String token = dataFetcher.registerPlayer(name);
        ctx.json(new ServerConnectInfo(api.getGameServerHost(), name, token));
    }
    
    /**
     * Handler function for logging into an existing account with username & password/auth token.
     * If the user logs in using a password, a new auth token is generated.
     */
    private void handlePlayerLogin(Context ctx) {
        SessionsRequest request = ctx.bodyValidator(SessionsRequest.class).get();
        String name = request.getName();
        String password = request.getPassword();
        String token = request.getToken();
        
        // If a password is present, try to log in and generate an auth token.
        // Null auth token = incorrect username/password combination.
        // Otherwise, if an auth token is present, try to verify that instead.
        if(password != null) {
            token = dataFetcher.login(name, password);
            
            if(token == null) {
                error(ctx, "Username or password is incorrect. Please check your credentials.");
                return;
            }
        } else if(token != null) {
            if(!dataFetcher.verifyAuthToken(name, token)) {
                error(ctx, "The provided session token is invalid or has expired. Please try relogging.");
                return;
            }
        } else {
            error(ctx, "No credentials provided.");
            return;
        }
        
        ctx.json(new ServerConnectInfo(api.getGameServerHost(), dataFetcher.fetchPlayerName(name), token));
    }
    
    /**
     * Handler function for initiating password resets.
     * TODO wip
     */
    private void handlePasswordForget(Context ctx) {
        error(ctx, "Sorry, it is currently not possible to reset your password.");
    }
    
    /**
     * Handler function for processing password resets.
     * TODO wip
     */
    private void handlePasswordReset(Context ctx) {
        error(ctx, "Sorry, it is currently not possible to reset your password.");
    }
    
    /**
     * Handler function for in-app purchases.
     * Permanently doomed to err, as it will never be implemented.
     */
    private void handleInAppPurchase(Context ctx) {
        error(ctx, "Sorry, in-app purchases are not supported.");
    }
    
    /**
     * Stops the gateway service.
     * @see Javalin#stop()
     */
    public void stop() {
        gateway.stop();
    }
}
