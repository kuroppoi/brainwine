package brainwine.api;

import java.io.File;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import brainwine.api.config.ApiConfig;
import brainwine.api.config.NewsEntry;
import brainwine.shared.JsonHelper;

public class Api {
    
    private static final Logger logger = LogManager.getLogger();
    private final ApiConfig config;
    private final DataFetcher dataFetcher;
    private final GatewayService gatewayService;
    private final PortalService portalService;
    
    public Api() {
        this(new DefaultDataFetcher());
    }
    
    public Api(DataFetcher dataFetcher) {
        long startTime = System.currentTimeMillis();
        logger.info("Starting API ...");
        this.dataFetcher = dataFetcher;
        logger.info("Using data fetcher {}", dataFetcher.getClass().getName());
        logger.info("Loading configuration ...");
        config = loadConfig();
        gatewayService = new GatewayService(this, config.getGatewayPort());
        portalService = new PortalService(this, config.getPortalPort());
        logger.info("All done! API startup took {} milliseconds", System.currentTimeMillis() - startTime);
    }
    
    public void onShutdown() {
        logger.info("Shutting down API ...");
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
            
            return JsonHelper.readValue(file, ApiConfig.class);
        } catch (Exception e) {
            logger.fatal("Failed to load configuration", e);
            System.exit(-1);
        }
        
        return ApiConfig.DEFAULT_CONFIG;
    }
    
    public List<NewsEntry> getNews() {
        return config.getNews();
    }
    
    public String getGameServerHost() {
        return config.getGameServerIp() + ":" + config.getGameServerPort();
    }
    
    public DataFetcher getDataFetcher() {
        return dataFetcher;
    }
}
