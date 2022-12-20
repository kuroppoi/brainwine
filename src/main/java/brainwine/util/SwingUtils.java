package brainwine.util;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLaf;

public class SwingUtils {
    
    @SuppressWarnings("serial")
    public static Action createAction(String name, Icon icon, Runnable handler) {
        AbstractAction action = new AbstractAction(name, icon) {
            @Override
            public void actionPerformed(ActionEvent event) {
                handler.run();
            }
        };
        
        if(icon != null) {
            action.putValue(Action.SHORT_DESCRIPTION, name);
        }
        
        return action;
    }
    
    public static Action createAction(String name, Runnable handler) {
        return createAction(name, null, handler);
    }
    
    public static GridBagConstraints createConstraints(int x, int y) {
        return createConstraints(x, y, 1, 1);
    }
    
    public static GridBagConstraints createConstraints(int x, int y, int width, int height) {
        return createConstraints(x, y, width, height, 1, 1);
    }
    
    public static GridBagConstraints createConstraints(int x, int y, int width, int height, double weightX, double weightY) {
        return createConstraints(x, y, width, height, weightX, weightY, 8, 8);
    }
    
    public static GridBagConstraints createConstraints(int x, int y, int width, int height, double weightX, double weightY, 
            int paddingX, int paddingY) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = width;
        constraints.gridheight = height;
        constraints.weightx = weightX;
        constraints.weighty = weightY;
        constraints.ipadx = paddingX;
        constraints.ipady = paddingY;
        return constraints;
    }
    
    public static void setMenuBarEmbedded(boolean flag) {
        UIManager.put("TitlePane.menuBarEmbedded", flag);
        FlatLaf.updateUI();
    }
    
    public static boolean isMenuBarEmbedded() {
        return UIManager.getBoolean("TitlePane.menuBarEmbedded");
    }
    
    public static void setDefaultFontSize(int size) {
        UIManager.put("defaultFont", UIManager.getFont("defaultFont").deriveFont((float)size));
        UIManager.put("Brainwine.consoleFont", UIManager.getFont("Brainwine.consoleFont").deriveFont((float)size));
        FlatLaf.updateUI();
    }
    
    public static int getDefaultFontSize() {
        return UIManager.getFont("defaultFont").getSize();
    }
}
