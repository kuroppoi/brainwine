package brainwine.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PropertyFile {
    
    private static final Logger logger = LogManager.getLogger();
    private final Properties properties = new Properties();
    private final File file;
    
    public PropertyFile(File file) {
        this.file = file;
        
        if(file.exists()) {
            try {
                properties.load(new FileInputStream(file));
            } catch (Exception e) {
                logger.error("Could not load {}", file, e);
                save();
            }
        } else {
            save();
        }
    }
    
    public void save() {
        try {
            properties.store(new FileOutputStream(file), "Here you can change the server connection information.");
        } catch(Exception e) {
            logger.error("Could not save {}", file, e);
        }
    }
    
    public void setProperty(String key, Object value) {
        properties.setProperty(key, "" + value);
    }
    
    public String getString(String key, String def) {
        if(!properties.containsKey(key)) {
            properties.setProperty(key, def);
            save();
            return def;
        }
        
        return properties.getProperty(key);
    }
    
    public int getInt(String key, int def) {
        try {
            return Integer.parseInt(getString(key, "" + def));
        } catch(NumberFormatException e) {
            properties.setProperty(key, "" + def);
            save();
            return def;
        }
    }
    
    public boolean getBoolean(String key, boolean def) {
        return Boolean.parseBoolean(getString(key, "" + def));
    }
}
