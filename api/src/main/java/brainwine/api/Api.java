package brainwine.api;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import brainwine.api.models.NewsEntry;

public class Api {
    
    private static final Logger logger = LogManager.getLogger();
    private final List<NewsEntry> news = new ArrayList<>();
    private final PropertyFile properties;
    private final int gatewayPort;
    private final int portalPort;
    private final String hostAddress;
    private final int hostPort;
    private final GatewayService gatewayService;
    private final PortalService portalService;
    
    public Api() {
        long startTime = System.currentTimeMillis();
        logger.info("Starting API ...");
        loadNews();
        logger.info("Loading properties ...");
        properties = new PropertyFile(new File("api.properties"));
        gatewayPort = properties.getInt("gateway_port", 5001);
        portalPort = properties.getInt("portal_port", 5003);
        hostAddress = properties.getString("gameserver_address", "127.0.0.1");
        hostPort = properties.getInt("gameserver_port", 5002);
        gatewayService = new GatewayService(this, gatewayPort);
        portalService = new PortalService(portalPort);
        logger.info("All done! API startup took {} milliseconds", System.currentTimeMillis() - startTime);
    }
    
    public void onShutdown() {
        logger.info("Shutting down API ...");
        gatewayService.stop();
        portalService.stop();
    }
    
    private void loadNews() {
        logger.info("Loading news data ...");
        news.clear();
        File newsFile = new File("news.json");
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            
            if(!newsFile.exists()) {
                logger.info("Generating default news file ...");
                NewsEntry defaultNews = new NewsEntry("Default News", "This news entry was automatically generated.\nEdit 'news.json' to make your own!", "A more civilised age...");
                mapper.writerWithDefaultPrettyPrinter().writeValue(newsFile, Arrays.asList(defaultNews));
            }
            
            news.addAll(mapper.readerForListOf(NewsEntry.class).readValue(newsFile));
            Collections.reverse(news); // Reverse the list so that the last article in the file gets shown first.
        } catch (Exception e) {
            logger.error("Failed to load news data", e);
        }
    }
    
    public List<NewsEntry> getNews() {
        return news;
    }
    
    public String getGameServerHost() {
        return hostAddress + ":" + hostPort;
    }
}
