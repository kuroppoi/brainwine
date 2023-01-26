package brainwine.gui;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class GuiPreferences {
    
    public static final String ROOT_KEY = "brainwine";
    public static final String THEME_KEY = "theme";
    public static final String TAB_PLACEMENT_KEY = "tabPlacement";
    public static final String FONT_SIZE_KEY = "fontSize";
    public static final String EMBED_MENU_BAR_KEY = "embedMenuBar";
    public static final String GATEWAY_HOST_KEY = "gatewayHost";
    public static final String API_HOST_KEY = "apiHost";
    private static Preferences preferences;
    
    public static Preferences get() {
        if(preferences == null) {
            preferences = Preferences.userRoot().node(ROOT_KEY);
        }
        
        return preferences;
    }
    
    public static void setString(String key, String value) {
        get().put(key, value);
    }
    
    public static String getString(String key, String def) {
        return get().get(key, def);
    }
    
    public static void setBoolean(String key, boolean value) {
        get().putBoolean(key, value);
    }
    
    public static boolean getBoolean(String key, boolean def) {
        return get().getBoolean(key, def);
    }
    
    public static void setInt(String key, int value) {
        get().putInt(key, value);
    }
    
    public static int getInt(String key, int def) {
        return get().getInt(key, def);
    }
    
    public static void setFloat(String key, float value) {
        get().putFloat(key, value);
    }
    
    public static float getFloat(String key, float def) {
        return get().getFloat(key, def);
    }
    
    public static void setLong(String key, long value) {
        get().putLong(key, value);
    }
    
    public static long getLong(String key, long def) {
        return get().getLong(key, def);
    }
    
    public static void setDouble(String key, double value) {
        get().putDouble(key, value);
    }
    
    public static double getDouble(String key, double def) {
        return get().getDouble(key, def);
    }
    
    public static void setByteArray(String key, byte[] value) {
        get().putByteArray(key, value);
    }
    
    public static byte[] getByteArray(String key, byte[] def) {
        return get().getByteArray(key, def);
    }
    
    public static void clear() throws BackingStoreException {
        if(preferences != null) {
            preferences.removeNode();
            preferences = null;
        }
    }
}
