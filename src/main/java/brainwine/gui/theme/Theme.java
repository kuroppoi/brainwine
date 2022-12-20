package brainwine.gui.theme;

import javax.swing.UIManager.LookAndFeelInfo;

public class Theme {
    
    private final LookAndFeelInfo lafInfo;
    
    public Theme(LookAndFeelInfo lafInfo) {
        this.lafInfo = lafInfo;
    }
    
    @Override
    public String toString() {
        return getName();
    }
    
    public String getName() {
        return lafInfo.getName();
    }
    
    public String getClassName() {
        return lafInfo.getClassName();
    }
}
