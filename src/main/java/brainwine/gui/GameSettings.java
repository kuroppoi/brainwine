package brainwine.gui;

import java.util.prefs.Preferences;

import brainwine.gui.component.GamePanel;
import brainwine.util.OperatingSystem;

public class GameSettings {
    
    // Keys
    public static final String GAME_VERSION_KEY = "gameVersion";
    public static final String SERVER_ADDRESS_KEY = "serverAddress";
    public static final String GATEWAY_PORT_KEY = "gatewayPort";
    public static final String API_PORT_KEY = "apiPort";
    
    // Defaults
    public static final String GAME_VERSION_DEFAULT = GamePanel.MAC_GAME_VERSIONS[0];
    public static final String SERVER_ADDRESS_DEFAULT = OperatingSystem.isWindows() ? "local" : "http://127.0.0.1:5001";
    public static final int GATEWAY_PORT_DEFAULT = 5001;
    public static final int API_PORT_DEFAULT = 5003;
    
    private static Preferences preferences = Preferences.userRoot().node(GameSettings.class.getName());
    
    public static void resetToDefaults() {
        setGameVersion(GAME_VERSION_DEFAULT);
        setServerAddress(SERVER_ADDRESS_DEFAULT);
        setGatewayPort(GATEWAY_PORT_DEFAULT);
        setApiPort(API_PORT_DEFAULT);
    }
    
    public static void setGameVersion(String value) {
        preferences.put(GAME_VERSION_KEY, value);
    }
    
    public static String getGameVersion() {
        return preferences.get(GAME_VERSION_KEY, GAME_VERSION_DEFAULT);
    }
    
    public static void setServerAddress(String value) {
        preferences.put(SERVER_ADDRESS_KEY, value);
    }
    
    public static String getServerAddress() {
        return preferences.get(SERVER_ADDRESS_KEY, SERVER_ADDRESS_DEFAULT);
    }
    
    public static void setGatewayPort(int value) {
        preferences.putInt(GATEWAY_PORT_KEY, value);
    }
    
    public static int getGatewayPort() {
        return preferences.getInt(GATEWAY_PORT_KEY, GATEWAY_PORT_DEFAULT);
    }
    
    public static void setApiPort(int value) {
        preferences.putInt(API_PORT_KEY, value);
    }
    
    public static int getApiPort() {
        return preferences.getInt(API_PORT_KEY, API_PORT_DEFAULT);
    }
}
