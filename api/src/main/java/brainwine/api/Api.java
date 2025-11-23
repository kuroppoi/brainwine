package brainwine.api;

import static brainwine.shared.LogMarkers.SERVER_MARKER;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import brainwine.api.config.ApiConfig;
import brainwine.api.config.NewsEntry;
import brainwine.api.config.SslConfig;
import brainwine.shared.JsonHelper;
import io.javalin.core.LoomUtil;

public class Api {
    
    private static final Logger logger = LogManager.getLogger();
    private final ApiConfig config;
    private final List<NewsEntry> news;
    private final DataFetcher dataFetcher;
    private final GatewayService gatewayService;
    private final PortalService portalService;
    
    public Api() {
        this(new DefaultDataFetcher());
    }
    
    public Api(DataFetcher dataFetcher) {
        long startTime = System.currentTimeMillis();
        logger.info(SERVER_MARKER, "Starting API ...");
        this.dataFetcher = dataFetcher;
        logger.info(SERVER_MARKER, "Using data fetcher {}", dataFetcher.getClass().getName());
        logger.info(SERVER_MARKER, "Loading configuration ...");
        config = loadConfig();
        logger.info(SERVER_MARKER, "Is SSL enabled? {}", config.getSslConfig().isSslEnabled() ? "Yes" : "No");
        news = new ArrayList<>(config.getNews()); // Explicit copy
        Collections.reverse(news);
        LoomUtil.useLoomThreadPool = false;
        gatewayService = new GatewayService(this, config.getGatewayPort());
        portalService = new PortalService(this, config.getPortalPort());
        logger.info(SERVER_MARKER, "All done! API startup took {} milliseconds", System.currentTimeMillis() - startTime);
    }
    
    public void onShutdown() {
        logger.info(SERVER_MARKER, "Shutting down API ...");
        gatewayService.stop();
        portalService.stop();
    }
    
    private ApiConfig loadConfig() {
        try {
            File file = new File("api.json");
            
            if(!file.exists()) {
                file.createNewFile();
                JsonHelper.writeValue(file, ApiConfig.DEFAULT_CONFIG);
                return ApiConfig.DEFAULT_CONFIG;
            }
            
            ApiConfig config = JsonHelper.readValue(file, ApiConfig.class);
            JsonHelper.writeValue(file, config);
            return config;
        } catch (Exception e) {
            logger.fatal(SERVER_MARKER, "Failed to load configuration", e);
            System.exit(-1);
        }
        
        return ApiConfig.DEFAULT_CONFIG;
    }
    
    public List<NewsEntry> getNews() {
        return news;
    }
    
    public String getGameServerHost() {
        return config.getGameServerIp() + ":" + config.getGameServerPort();
    }
    
    public SslConfig getSslConfig() {
        return config.getSslConfig();
    }
    
    public DataFetcher getDataFetcher() {
        return dataFetcher;
    }
}
