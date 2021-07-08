package brainwine.api.handlers;

import static brainwine.api.util.ContextUtils.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.javalin.http.Context;
import io.javalin.http.ExceptionHandler;

public class SimpleExceptionHandler implements ExceptionHandler<Exception> {
    
    private static final Logger logger = LogManager.getLogger();
    
    @Override
    public void handle(Exception exception, Context ctx) {
        logger.error("Exception caught", exception);
        error(ctx, "%s", exception);
    }
}
