package brainwine.gameserver.server;

import brainwine.gameserver.server.pipeline.Connection;

/**
 * Requests are incoming packets from the client.
 */
public abstract class Request {
    
    public abstract void process(Connection connection);
}
