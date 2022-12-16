package brainwine.gui;

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
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

import com.formdev.flatlaf.extras.FlatInspector;
import com.formdev.flatlaf.extras.components.FlatTabbedPane;
import com.formdev.flatlaf.extras.components.FlatTabbedPane.TabAlignment;

import brainwine.Bootstrap;
import brainwine.util.DesktopUtils;
import brainwine.util.OperatingSystem;
import brainwine.util.ProcessResult;
import brainwine.util.RegistryKey;
import brainwine.util.RegistryUtils;
import brainwine.util.SwingUtils;

public class MainView {
    
    private final JFrame frame;
    private final JPanel panel;
    private final FlatTabbedPane tabbedPane;
    private final ServerPanel serverPanel;
    private final SettingsPanel settingsPanel;
    
    public MainView(Bootstrap bootstrap) {        
        // Panels
        panel = new JPanel(new BorderLayout());
        serverPanel = new ServerPanel(bootstrap);
        settingsPanel = new SettingsPanel();
        
        // Tabs
        tabbedPane = new FlatTabbedPane();
        tabbedPane.setShowContentSeparators(true);
        tabbedPane.setTabPlacement(JTabbedPane.LEFT);
        tabbedPane.setTabAlignment(TabAlignment.leading);
        
        if(OperatingSystem.isWindows()) {
            tabbedPane.addTab("Play Game", UIManager.getIcon("Brainwine.playIcon"), new GamePanel());
        }
        
        tabbedPane.addTab("Server", UIManager.getIcon("Brainwine.serverIcon"), serverPanel);
        tabbedPane.addTab("Settings", UIManager.getIcon("Brainwine.settingsIcon"), settingsPanel);
        panel.add(tabbedPane);
        
        // Menu
        JMenuBar menuBar = new JMenuBar();
        JMenu helpMenu = new JMenu("Help");
        
        if(OperatingSystem.isWindows()) {
            helpMenu.add(SwingUtils.createAction("Clear Account Lock", this::showAccountLockPrompt));
        }
        
        helpMenu.add(SwingUtils.createAction("GitHub", () -> DesktopUtils.browseUrl(GuiConstants.GITHUB_REPOSITORY_URL)));
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
                bootstrap.closeApplication();
            }
        });
        frame.setJMenuBar(menuBar);
        frame.setMinimumSize(new Dimension(848, 480));
        frame.setFocusable(true);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        FlatInspector.install("K"); // TODO
    }
    
    public void enableServerButton() {
        serverPanel.enableServerButton();
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
                JOptionPane.showMessageDialog(frame, "Failed to remove account lock.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private boolean clearAccountLock() {
        ProcessResult queryResult = RegistryUtils.query(GuiConstants.DEEPWORLD_PLAYERPREFS, "playerLock*");
        
        if(queryResult.wasSuccessful()) {
            RegistryKey key = RegistryUtils.getFirstQueryResult(queryResult);
            
            if(key != null) {
                String name = key.getName();
                ProcessResult deleteResult = RegistryUtils.delete(GuiConstants.DEEPWORLD_PLAYERPREFS, name);
                return deleteResult.wasSuccessful();
            } else {
                // Might as well.
                return true;
            }
        }
        
        return false;
    }
}
