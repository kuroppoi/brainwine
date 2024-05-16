package brainwine.gui.view;

import static brainwine.gui.GuiConstants.DEEPWORLD_PLAYERPREFS;
import static brainwine.gui.GuiConstants.GITHUB_REPOSITORY_URL;
import static brainwine.shared.LogMarkers.GUI_MARKER;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.extras.components.FlatTabbedPane;
import com.formdev.flatlaf.extras.components.FlatTabbedPane.TabAlignment;

import brainwine.Main;
import brainwine.gui.GuiPreferences;
import brainwine.gui.component.GamePanel;
import brainwine.gui.component.ServerPanel;
import brainwine.gui.component.SettingsPanel;
import brainwine.util.DesktopUtils;
import brainwine.util.OperatingSystem;
import brainwine.util.ProcessResult;
import brainwine.util.ProcessUtils;
import brainwine.util.RegistryKey;
import brainwine.util.RegistryUtils;
import brainwine.util.SwingUtils;

public class MainView {
    
    private static final Logger logger = LogManager.getLogger();
    private final JFrame frame;
    private final JPanel panel;
    private final FlatTabbedPane tabbedPane;
    
    public MainView(Main main) {
        logger.info(GUI_MARKER, "Creating main view ...");
        
        // Panel
        panel = new JPanel(new BorderLayout());
        
        // Tabs
        tabbedPane = new FlatTabbedPane();
        tabbedPane.setShowContentSeparators(true);
        tabbedPane.setTabAlignment(TabAlignment.leading);
        setTabPlacement(GuiPreferences.getTabPlacement(), false);
        
        if(OperatingSystem.isWindows() || OperatingSystem.isMacOS()) {
            tabbedPane.addTab("Play Game", UIManager.getIcon("Brainwine.playIcon"), new GamePanel(main));
        }
        
        tabbedPane.addTab("Server", UIManager.getIcon("Brainwine.serverIcon"), new ServerPanel(main));
        tabbedPane.addTab("Settings", UIManager.getIcon("Brainwine.settingsIcon"), new SettingsPanel(this));
        panel.add(tabbedPane);
        
        // Menu
        JMenuBar menuBar = new JMenuBar();
        JMenu helpMenu = new JMenu("Help");
        helpMenu.add(SwingUtils.createAction("Clear Account Lock", this::showAccountLockPrompt));
        helpMenu.add(SwingUtils.createAction("GitHub", () -> DesktopUtils.browseUrl(GITHUB_REPOSITORY_URL)));
        menuBar.add(helpMenu);
                
        // Frame
        frame = new JFrame("Brainwine");
        frame.setIconImages(Arrays.asList(
                new ImageIcon(getClass().getResource("/icon16x.png")).getImage(), 
                new ImageIcon(getClass().getResource("/icon32x.png")).getImage(), 
                new ImageIcon(getClass().getResource("/icon64x.png")).getImage()));
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                main.closeApplication();
            }
        });
        frame.setJMenuBar(menuBar);
        frame.setMinimumSize(new Dimension(848, 520));
        frame.setFocusable(true);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    public void setTabPlacement(int tabPlacement) {
        setTabPlacement(tabPlacement, true);
    }
    
    public void setTabPlacement(int tabPlacement, boolean animateChange) {
        if(animateChange) {
            FlatAnimatedLafChange.showSnapshot();
        }
        
        tabbedPane.setTabPlacement(Math.min(4, Math.max(1, tabPlacement)));
        
        if(animateChange) {
            FlatAnimatedLafChange.hideSnapshotWithAnimation();
        }
    }
    
    public int getTabPlacement() {
        return tabbedPane.getTabPlacement();
    }
    
    private void showAccountLockPrompt() {
        int result = JOptionPane.showConfirmDialog(frame, 
                "If you've found yourself in a situation where you are unable to log into an unregistered account"
                + " and can thus not log out either, then you may use this option to forcefully remove the account lock."
                + " If the account is not registered and you log out after removing the lock, you will lose access to it."
                + " Are you sure you want to proceed?", "Are you sure?", JOptionPane.YES_NO_OPTION);
        
        if(result == JOptionPane.YES_OPTION) {
            if(clearAccountLock()) {
                JOptionPane.showMessageDialog(frame, "Account lock removed. Register your account next time.");
            } else {
                JOptionPane.showMessageDialog(frame, "Could not remove account lock.\nEither there is no account lock, or an error has occured.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private boolean clearAccountLock() {
        return OperatingSystem.isWindows() ? clearAccountLockWindows() : OperatingSystem.isMacOS() ? clearAccountLockMacOS() : false;
    }
    
    private boolean clearAccountLockWindows() {
        ProcessResult queryResult = RegistryUtils.query(DEEPWORLD_PLAYERPREFS, "playerLock*");
        
        if(queryResult.wasSuccessful()) {
            RegistryKey key = RegistryUtils.getFirstQueryResult(queryResult);
            
            if(key != null) {
                String name = key.getName();
                ProcessResult deleteResult = RegistryUtils.delete(DEEPWORLD_PLAYERPREFS, name);
                return deleteResult.wasSuccessful();
            } else {
                return false;
            }
        }
        
        return false;
    }
    
    // A bit simpler but it should to the trick just fine.
    private boolean clearAccountLockMacOS() {
        return ProcessUtils.executeCommand("defaults delete com.bytebin.deepworld playerLocked").wasSuccessful();
    }
}
