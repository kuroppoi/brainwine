package brainwine.api.handlers;

import static brainwine.api.util.ContextUtils.*;

import io.javalin.http.Context;
import io.javalin.http.Handler;

public class PasswordResetHandler implements Handler {
    
    @Override
    public void handle(Context ctx) throws Exception {
        error(ctx, "Sorry, it is currently not possible to reset your password.");
    }
}
