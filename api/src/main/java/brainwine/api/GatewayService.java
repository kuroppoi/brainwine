package brainwine.api;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import brainwine.api.models.PlayersRequest;
import brainwine.api.models.ServerConnectInfo;
import brainwine.api.models.SessionsRequest;
import brainwine.api.util.ContextUtils;
import brainwine.gameserver.GameServer;
import brainwine.gameserver.entity.player.PlayerManager;
import io.javalin.Javalin;

public class GatewayService {
    
    private static final Logger logger = LogManager.getLogger();
    private final Javalin gateway;
    
    public GatewayService(Api api, int port) {
        logger.info("Starting GatewayService @ port {} ...", port);
        PlayerManager playerManager = GameServer.getInstance().getPlayerManager();
        gateway = Javalin.create().start(port);
        gateway.exception(Exception.class, (e, ctx) -> {
            ContextUtils.error(ctx, "%s", e);
            logger.error("Exception caught", e);
        });
        
        // News
        gateway.get("/clients", ctx ->{
            Map<String, Object> json = new HashMap<>();
            json.put("posts", api.getNews());
            ctx.json(json);
        });
        
        // Registration
        gateway.post("/players", ctx -> {
            PlayersRequest request = ctx.bodyValidator(PlayersRequest.class).get();
            String name = request.getName();
            
            if(playerManager.getPlayer(name) != null) {
                ContextUtils.error(ctx, "Sorry, this username has already been taken.");
                return;
            }
            
            playerManager.registerPlayer(name);
            String token = playerManager.refreshAuthToken(name);
            ctx.json(new ServerConnectInfo(api.getGameServerHost(), name, token));
        });
        
        // Login
        gateway.post("/sessions", ctx -> {
            SessionsRequest request = ctx.bodyValidator(SessionsRequest.class).get();
            String name = request.getName();
            String password = request.getPassword();
            String token = request.getToken();
            
            if(password != null) {
                if(!playerManager.verifyPassword(name, password)) {
                    ContextUtils.error(ctx, "Username or password is incorrect. Please check your credentials.");
                    return;
                }
            } else if(token != null) {
                if(!playerManager.verifyAuthToken(name, token)) {
                    ContextUtils.error(ctx, "The provided session token is invalid or has expired. Please try relogging.");
                    return;
                }
            } else {
                ContextUtils.error(ctx, "No credentials provided.");
                return;
            }
            
            String newToken = playerManager.refreshAuthToken(name);
            ctx.json(new ServerConnectInfo(api.getGameServerHost(), name, newToken));
        });
        
        // Password reset request
        gateway.post("/passwords/request", ctx -> {
            ContextUtils.error(ctx, "Sorry, this feature is not implemented yet.");
        });
        
        // Password reset token entry
        gateway.post("/passwords/reset", ctx -> {
            ContextUtils.error(ctx, "Sorry, this feature is not implemented yet.");
        });
        
        // RWC purchases
        gateway.post("/purchases", ctx -> {
            ContextUtils.error(ctx, "Sorry, purchases with RWC are disabled.");
        });
    }
    
    public void stop() {
        gateway.stop();
    }
}
