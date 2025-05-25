package brainwine.gui.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.extras.components.FlatTextField;

import brainwine.Main;
import brainwine.ServerStatusListener;
import brainwine.gui.GameLauncher;
import brainwine.gui.GameSettings;
import brainwine.gui.GuiConstants;
import brainwine.util.DesktopUtils;
import brainwine.util.OperatingSystem;
import brainwine.util.SwingUtils;

@SuppressWarnings("serial")
public class GamePanel extends JPanel {
    
    public static final String[] MAC_GAME_VERSIONS = { "2.11.1", "1.13.3" };
    private final Main main;
    private final GameLauncher gameLauncher;
    private JButton serverButton;
    private JButton startGameButton;
    private FlatTextField serverAddressField;
    private FlatTextField gatewayPortField; // Windows only
    private FlatTextField apiPortField; // Windows only
    private JComboBox<String> gameVersionBox; // Mac only
    
    public GamePanel(Main main) {
        this.main = main;
        setLayout(new BorderLayout());
        
        // Create image panel
        ImagePanel imagePanel = new ImagePanel() {
            @Override
            public void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);      
                Graphics2D g2d = (Graphics2D)graphics;
                Color gradientColor = new Color(8, 8, 8, 255);
                Color fadeColor = new Color(8, 8, 8, 0);   
                g2d.setPaint(new GradientPaint(0, 0, gradientColor, 0, 20, fadeColor));
                g2d.fillRect(0, 0, getWidth(), 20);
                g2d.setPaint(new GradientPaint(0, getHeight() - 20, fadeColor, 0, getHeight(), gradientColor));
                g2d.fillRect(0, getHeight() - 20, getWidth(), 20);
            }
        };
        
        // Load background image
        try {
            imagePanel.setImage(ImageIO.read(getClass().getResourceAsStream("/background.jpg")));
        } catch (IllegalArgumentException | IOException e) {
            SwingUtils.showExceptionInfo(this, "Could not load background image.", e);
        }
        
        // Add components
        add(imagePanel);
        add(createLauncherPanel(), BorderLayout.SOUTH); // Game launcher relies on this so do not move!
        
        // Create game launcher
        gameLauncher = OperatingSystem.isWindows() ? new GameLauncher.Windows(this) : new GameLauncher.Mac(this, startGameButton);
        
        // Create server status listener
        main.addServerStatusListener(new ServerStatusListener() {
            @Override
            public void onServerStarting() {
                serverButton.setEnabled(false);
            }

            @Override
            public void onServerStopping() {
                serverButton.setEnabled(false);
            }

            @Override
            public void onServerStarted() {
                SwingUtilities.invokeLater(() -> {
                    serverButton.setText("Stop Server");
                    serverButton.setEnabled(true);
                });
            }

            @Override
            public void onServerStopped() {
                SwingUtilities.invokeLater(() -> {
                    serverButton.setText("Start Server");
                    serverButton.setEnabled(true);
                });
            }
        });
    }
    
    private void openCommunityHub() {
        // Try opening with Steam and open with browser if it fails
        if(!DesktopUtils.browseUrl(GuiConstants.STEAM_COMMUNITY_HUB_URL)) {
            DesktopUtils.browseUrl(GuiConstants.HTTP_COMMUNITY_HUB_URL);
        }
    }
    
    private JPanel createLauncherPanel() {        
        // Start button
        startGameButton = new JButton("Start Deepworld", UIManager.getIcon("Brainwine.playIcon"));
        startGameButton.addActionListener(event -> {
            // Update preferences
            GameSettings.setServerAddress(SwingUtils.getTextFieldValue(serverAddressField));
            
            if(OperatingSystem.isWindows()) {
                try {
                    GameSettings.setGatewayPort(Integer.parseInt(SwingUtils.getTextFieldValue(gatewayPortField)));
                    GameSettings.setApiPort(Integer.parseInt(SwingUtils.getTextFieldValue(apiPortField)));
                } catch(NumberFormatException e) {
                    // Show warning & discard exception silently
                    JOptionPane.showMessageDialog(this, "Server ports must be numerical.", "Attention", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } else {
                GameSettings.setGameVersion((String)gameVersionBox.getSelectedItem());
            }
            
            // Launch the game
            gameLauncher.startGame();
        });
        
        // Server button
        serverButton = new JButton("Start Server", UIManager.getIcon("Brainwine.serverIcon"));
        serverButton.addActionListener(event -> main.toggleServer());
        
        // Community hub button
        JButton communityHubButton = new JButton("Community Hub", UIManager.getIcon("Brainwine.communityIcon"));
        communityHubButton.addActionListener(event -> openCommunityHub());
        
        // Create top button panel
        JPanel topButtonPanel = new JPanel(new GridLayout(1, 2));
        topButtonPanel.add(serverButton);
        topButtonPanel.add(communityHubButton);
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1));
        buttonPanel.add(topButtonPanel);
        buttonPanel.add(startGameButton);
        
        // Create panel
        JPanel panel = new JPanel(new GridLayout(1, 2));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));
        panel.add(OperatingSystem.isWindows() ? createWindowsGameSettingsPanel() : createMacGameSettingsPanel());
        panel.add(buttonPanel);
        return panel;
    }
    
    private JPanel createWindowsGameSettingsPanel() {
        // Create server address field
        serverAddressField = new FlatTextField();
        serverAddressField.setText(GameSettings.getServerAddress());
        serverAddressField.setPlaceholderText("local");
        serverAddressField.setShowClearButton(true);
        
        // Create gateway port field
        gatewayPortField = new FlatTextField();
        gatewayPortField.setText(String.valueOf(GameSettings.getGatewayPort()));
        gatewayPortField.setPlaceholderText("5001");
        gatewayPortField.setShowClearButton(true);
        
        // Create API port field
        apiPortField = new FlatTextField();
        apiPortField.setText(String.valueOf(GameSettings.getApiPort()));
        apiPortField.setPlaceholderText("5003");
        apiPortField.setShowClearButton(true);
        
        // Create panel
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("Server Address"), SwingUtils.createConstraints(0, 0, 1, 1, 0, 1));
        panel.add(serverAddressField, SwingUtils.createConstraints(1, 0, 3, 1));
        panel.add(new JLabel("Gateway Port"), SwingUtils.createConstraints(0, 1));
        panel.add(gatewayPortField, SwingUtils.createConstraints(1, 1, 1, 1, 1, 0));
        panel.add(new JLabel("API Port", JLabel.CENTER), SwingUtils.createConstraints(2, 1));
        panel.add(apiPortField, SwingUtils.createConstraints(3, 1, 1, 1, 1, 0));
        return panel;
    }
    
    private JPanel createMacGameSettingsPanel() {
        // Create server address field
        serverAddressField = new FlatTextField();
        serverAddressField.setText(GameSettings.getServerAddress());
        serverAddressField.setPlaceholderText("http://127.0.0.1:5001");
        serverAddressField.setShowClearButton(true);
        
        // Create game version selector
        gameVersionBox = new JComboBox<>(MAC_GAME_VERSIONS);
        gameVersionBox.setSelectedItem(GameSettings.getGameVersion());
        
        // Create panel
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("Server Address"), SwingUtils.createConstraints(0, 0, 1, 1, 0, 1));
        panel.add(serverAddressField, SwingUtils.createConstraints(1, 0, 1, 1));
        panel.add(new JLabel("Game Version"), SwingUtils.createConstraints(0, 1, 1, 1, 0, 1));
        panel.add(gameVersionBox, SwingUtils.createConstraints(1, 1, 1, 1));
        return panel;
    }
}
