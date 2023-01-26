package brainwine;

import java.awt.Desktop;
import java.awt.Font;
import java.lang.reflect.InvocationTargetException;

import javax.swing.ImageIcon;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import brainwine.gui.GuiPreferences;
import brainwine.gui.MainView;
import brainwine.gui.theme.ThemeManager;
import brainwine.util.SwingUtils;

public class Bootstrap {

    private static Logger logger = LogManager.getLogger();
    private static boolean disableGui = false;
    private static boolean forceGui = false;
    private ServerThread serverThread;
    private MainView mainView;
    private boolean closeRequested;
    
    public static void main(String[] args) {
        for(String arg : args) {
            if(arg.equalsIgnoreCase("disablegui")) {
                disableGui = true;
            }
            
            if(arg.equalsIgnoreCase("forcegui")) {
                forceGui = true;
            }
        }
        
        new Bootstrap();
    }
    
    public Bootstrap() {
        if(!disableGui && (Desktop.isDesktopSupported() || forceGui)) {
            try {
                createMainView();
            } catch (InvocationTargetException | InterruptedException e) {
                logger.fatal("Could not create main view", e);
                System.exit(1);
            }
        } else {
            startServer();
            
            // Start console listener thread
            ConsoleThread consoleThread = new ConsoleThread();
            consoleThread.addListener(input -> {
                executeCommand(input);
            });
            consoleThread.start();
        }
    }
    
    public void executeCommand(String commandLine) {
        if(isServerRunning()) {
            serverThread.executeCommand(commandLine);
        }
    }
    
    public void closeApplication() {
        if(!closeRequested) {
            closeRequested = true;
            
            if(isServerRunning()) {
                serverThread.stopGracefully();
            } else {
                System.exit(0);
            }
        }
    }
    
    private void createMainView() throws InvocationTargetException, InterruptedException {
        SwingUtilities.invokeAndWait(() -> {
            // Preamble
            ThemeManager.init();
            UIManager.put("Brainwine.playIcon", new ImageIcon(getClass().getResource("/playIcon16x.png")));
            UIManager.put("Brainwine.serverIcon", new ImageIcon(getClass().getResource("/serverIcon16x.png")));
            UIManager.put("Brainwine.settingsIcon", new ImageIcon(getClass().getResource("/settingsIcon16x.png")));
            UIManager.put("Brainwine.communityIcon", new ImageIcon(getClass().getResource("/communityIcon16x.png")));
            UIManager.put("Brainwine.powerIcon", new ImageIcon(getClass().getResource("/powerIcon16x.png")));
            UIManager.put("Brainwine.consoleFont", new Font("Consolas", Font.PLAIN, 12));
            UIManager.put("Spinner.editorAlignment", JTextField.LEFT);
            UIManager.put("TitlePane.unifiedBackground", false);
            UIManager.put("Button.foreground", UIManager.get("MenuBar.foreground"));
            SwingUtils.setDefaultFontSize(Math.min(28, Math.max(10, GuiPreferences.getInt(GuiPreferences.FONT_SIZE_KEY, 14))));
            SwingUtils.setMenuBarEmbedded(GuiPreferences.getBoolean(GuiPreferences.EMBED_MENU_BAR_KEY, true));
            
            // Create view
            mainView = new MainView(this);
        });
    }
    
    public void startServer() {
        if(!isServerRunning()) {
            serverThread = new ServerThread(this);
            serverThread.start();
        }
    }
    
    public void stopServer() {
        if(isServerRunning()) {
            serverThread.stopGracefully();
        }
    }
    
    public void onServerStarted() {
        if(mainView != null) {
            SwingUtilities.invokeLater(mainView::enableServerButton);
        }
    }
    
    public void onServerStopped() {
        if(closeRequested) {
            System.exit(0);
        } else if(mainView != null) {
            SwingUtilities.invokeLater(mainView::enableServerButton);
        }
    }
    
    public boolean isServerRunning() {
        return serverThread != null && serverThread.isRunning();
    }
}
