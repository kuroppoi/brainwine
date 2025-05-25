package brainwine.gui;

import java.util.prefs.Preferences;

import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialDarkerIJTheme;

public class GuiPreferences {
        
    // Keys
    public static final String THEME_KEY = "theme";
    public static final String TAB_PLACEMENT_KEY = "tabPlacement";
    public static final String FONT_SIZE_KEY = "fontSize";
    
    // Defaults
    public static final String THEME_DEFAULT = FlatMaterialDarkerIJTheme.class.getName();
    public static final int TAB_PLACEMENT_DEFAULT = 1;
    public static final int FONT_SIZE_DEFAULT = 16;
    
    private static Preferences preferences = Preferences.userRoot().node(GuiPreferences.class.getName());
    
    public static void resetToDefaults() {
        setTheme(THEME_DEFAULT);
        setTabPlacement(TAB_PLACEMENT_DEFAULT);
        setFontSize(FONT_SIZE_DEFAULT);
    }
    
    public static void setTheme(String value) {
        preferences.put(THEME_KEY, value);
    }
    
    public static String getTheme() {
        return preferences.get(THEME_KEY, THEME_DEFAULT);
    }
    
    public static void setTabPlacement(int value) {
        preferences.putInt(TAB_PLACEMENT_KEY, value);
    }
    
    public static int getTabPlacement() {
        return preferences.getInt(TAB_PLACEMENT_KEY, TAB_PLACEMENT_DEFAULT);
    }
    
    public static void setFontSize(int value) {
        preferences.putInt(FONT_SIZE_KEY, value);
    }
    
    public static int getFontSize() {
        return preferences.getInt(FONT_SIZE_KEY, FONT_SIZE_DEFAULT);
    }
}
