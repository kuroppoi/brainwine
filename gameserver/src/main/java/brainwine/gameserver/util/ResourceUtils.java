package brainwine.gameserver.util;

import java.io.File;
import java.nio.file.Files;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

public class ResourceUtils {
    
    private static final Logger logger = LogManager.getLogger();
    
    public static void copyDefaults(String path) {
        copyDefaults(path, false);
    }
    
    public static void copyDefaults(String path, boolean force) {
        try {
            File file = new File(path);
            
            if(!file.exists() || force) {
                Reflections reflections = new Reflections(new ConfigurationBuilder()
                        .setUrls(ClasspathHelper.forResource("defaults"))
                        .setInputsFilter(x -> x.matches(String.format("defaults/%s.*", path)))
                        .setScanners(Scanners.Resources));
                Set<String> fileNames = reflections.getResources(".*");
                
                for(String fileName : fileNames) {
                    File output = new File(fileName.substring(9));
                    File parent = output.getAbsoluteFile().getParentFile();
                    
                    if(parent != null) {
                        parent.mkdirs();
                    }
                    
                    try {
                        Files.copy(ResourceUtils.class.getResourceAsStream(String.format("/%s", fileName)), output.toPath());
                    } catch (Exception e) {
                        logger.error("Couldn't copy resource '{}'", fileName, e);
                    }
                }
            }
        } catch(Exception e) {
            logger.error("Couldn't copy defaults '{}'", path, e);
        }
    }
    
    public static String removeFileSuffix(String string) {
        if(!string.contains(".")) {
            return string;
        }
        
        return string.substring(0, string.lastIndexOf('.'));
    }
}
