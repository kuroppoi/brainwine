package brainwine.api.handlers;

import static brainwine.api.util.ContextUtils.error;

import brainwine.api.DataFetcher;
import brainwine.api.models.PlayersRequest;
import brainwine.api.models.ServerConnectInfo;
import io.javalin.http.Context;
import io.javalin.http.Handler;

public class PlayerRegistrationHandler implements Handler {
    
    private final DataFetcher dataFetcher;
    private final String gameServerHost;
    
    public PlayerRegistrationHandler(DataFetcher dataFetcher, String gameServerHost) {
        this.dataFetcher = dataFetcher;
        this.gameServerHost = gameServerHost;
    }

    @Override
    public void handle(Context ctx) throws Exception {
        PlayersRequest request = ctx.bodyValidator(PlayersRequest.class).get();
        String name = request.getName();
        
        if(!name.matches("^[a-zA-Z0-9_.-]{4,20}$")) {
            error(ctx, "Please enter a valid username.");
            return;
        }
        
        if(dataFetcher.isPlayerNameTaken(name)) {
            error(ctx, "Sorry, this username has already been taken.");
            return;
        }
        
        String token = dataFetcher.registerPlayer(name);
        ctx.json(new ServerConnectInfo(gameServerHost, name, token));
    }
}
