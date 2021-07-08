package brainwine.api.handlers;

import static brainwine.api.util.ContextUtils.*;

import brainwine.api.DataFetcher;
import brainwine.api.models.ServerConnectInfo;
import brainwine.api.models.SessionsRequest;
import io.javalin.http.Context;
import io.javalin.http.Handler;

public class PlayerLoginHandler implements Handler {
    
    private final DataFetcher dataFetcher;
    private final String gameServerHost;
    
    public PlayerLoginHandler(DataFetcher dataFetcher, String gameServerHost) {
        this.dataFetcher = dataFetcher;
        this.gameServerHost = gameServerHost;
    }
    
    @Override
    public void handle(Context ctx) throws Exception {
        SessionsRequest request = ctx.bodyValidator(SessionsRequest.class).get();
        String name = request.getName();
        String password = request.getPassword();
        String token = request.getToken();
        
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
        
        ctx.json(new ServerConnectInfo(gameServerHost, name, token));
    }
}
