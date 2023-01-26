package brainwine.gui.theme;

import static brainwine.gui.GuiConstants.GUI_MARKER;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.intellijthemes.FlatAllIJThemes;
import com.formdev.flatlaf.intellijthemes.FlatAllIJThemes.FlatIJLookAndFeelInfo;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialDarkerIJTheme;

import brainwine.gui.GuiPreferences;

public class ThemeManager {
    
    private static final Logger logger = LogManager.getLogger();
    private static final Map<String, Theme> themes = new HashMap<>();
    private static final List<Theme> sortedThemes = new ArrayList<>();
    private static Theme currentTheme;
    
    public static void init() {
        themes.clear();
        sortedThemes.clear();
        logger.info(GUI_MARKER, "Registering themes ...");
        
        // Cuz who wants light themes, amirite?
        for(FlatIJLookAndFeelInfo info : FlatAllIJThemes.INFOS) {
            if(info.isDark()) {
                registerTheme(new Theme(info));
            }
        }
        
        // Set saved or default theme
        setTheme(GuiPreferences.getString(GuiPreferences.THEME_KEY, FlatMaterialDarkerIJTheme.class.getName()), false);
    }
    
    public static void registerTheme(Theme theme) {
        String className = theme.getClassName();
        
        if(themes.containsKey(className)) {
            logger.info(GUI_MARKER, "Attempted to register duplicate theme {} with class {}", theme.getName(), className);
            return;
        }
        
        themes.put(className, theme);
        sortedThemes.add(theme);
    }
    
    public static void setTheme(Class<? extends LookAndFeel> lafClass) {
        setTheme(lafClass, true);
    }
    
    public static void setTheme(Class<? extends LookAndFeel> lafClass, boolean animateChange) {
        setTheme(lafClass.getName(), animateChange);
    }
    
    public static void setTheme(String className) {
        setTheme(className, true);
    }
    
    public static void setTheme(String className, boolean animateChange) {
        Theme theme = getTheme(className);
        
        if(theme != null) {
            setTheme(theme, animateChange);
        }
    }
    
    public static void setTheme(Theme theme) {
        setTheme(theme, true);
    }
    
    public static void setTheme(Theme theme, boolean animateChange) {
        if(animateChange) {
            FlatAnimatedLafChange.showSnapshot();
        }
                
        try {
            UIManager.setLookAndFeel(theme.getClassName());
            currentTheme = theme;
        } catch(ClassNotFoundException | InstantiationException
                | IllegalAccessException
                | UnsupportedLookAndFeelException e) {
            logger.error(GUI_MARKER, "Could not set theme {}", theme.getName(), e);
        }
        
        FlatLaf.updateUI();
        
        if(animateChange) {
            FlatAnimatedLafChange.hideSnapshotWithAnimation();
        }
    }
    
    public static Theme getTheme(Class<? extends LookAndFeel> lafClass) {
        return getTheme(lafClass.getName());
    }
    
    public static Theme getTheme(String className) {
        return themes.get(className);
    }
    
    public static Theme getCurrentTheme() {
        return currentTheme;
    }
    
    public static List<Theme> getThemes() {
        return Collections.unmodifiableList(sortedThemes);
    }
}
