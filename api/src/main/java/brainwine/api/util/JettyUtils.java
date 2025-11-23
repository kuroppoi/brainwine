package brainwine.api.util;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;

public class JettyUtils {
    
    public static Server createJettyServerWithSsl(int port, String keyStorePath, String keyStorePassword) {
        Server server = new Server();
        ServerConnector connector = new ServerConnector(server, createSslContextFactory(keyStorePath, keyStorePassword));
        connector.setPort(port);
        server.addConnector(connector);
        return server;
    }
    
    public static SslContextFactory.Server createSslContextFactory(String keyStorePath, String keyStorePassword) {
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath(keyStorePath);
        sslContextFactory.setKeyStorePassword(keyStorePassword);
        return sslContextFactory;
    }
}
