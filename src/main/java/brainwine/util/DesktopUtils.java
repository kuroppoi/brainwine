package brainwine.util;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class DesktopUtils {
    
    public static boolean browseUrl(String url) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        
        if(desktop != null && desktop.isSupported(Action.BROWSE)) {
            try {
                URI uri = new URI(url);
                desktop.browse(uri);
                return true;
            } catch(URISyntaxException | IOException e) {
                // TODO log this somewhere
            }
        }
        
        return false;
    }
}
