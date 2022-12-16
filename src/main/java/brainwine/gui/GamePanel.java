package brainwine.gui;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.formdev.flatlaf.extras.components.FlatTextField;

import brainwine.util.DesktopUtils;
import brainwine.util.ProcessResult;
import brainwine.util.RegistryKey;
import brainwine.util.RegistryUtils;
import brainwine.util.SwingUtils;

@SuppressWarnings("serial")
public class GamePanel extends JPanel {
    
    private final FlatTextField gatewayHostField;
    private final FlatTextField apiHostField;
    private final JButton startGameButton;
    private final JButton communityHubButton;
    
    public GamePanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(0, 3, 3, 3));
        
        // Host settings stuff
        gatewayHostField = new FlatTextField();
        gatewayHostField.setPlaceholderText("Example: 127.0.0.1:5001");
        apiHostField = new FlatTextField();
        apiHostField.setPlaceholderText("Example: 127.0.0.1:5003");
        loadHostSettings();
        
        // Start game button
        startGameButton = new JButton("Start Deepworld", UIManager.getIcon("Brainwine.playIcon"));
        startGameButton.addActionListener(event -> startGame());
        
        // Community hub button
        communityHubButton = new JButton("Community Hub");
        communityHubButton.addActionListener(event -> openCommunityHub());
        
        // Layout shenanigans
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Game Settings"));
        formPanel.setPreferredSize(new Dimension(320, 128));
        formPanel.setMaximumSize(new Dimension(320, 128));
        formPanel.add(new JLabel("Gateway Host"), SwingUtils.createConstraints(0, 1, 1, 1, 1, 1));
        formPanel.add(gatewayHostField, SwingUtils.createConstraints(1, 1, 1, 1, 1, 1));
        formPanel.add(new JLabel("API Host"), SwingUtils.createConstraints(0, 2, 1, 1, 1, 1));
        formPanel.add(apiHostField, SwingUtils.createConstraints(1, 2, 1, 1, 1, 1));
        formPanel.add(startGameButton, SwingUtils.createConstraints(0, 3, 1, 1, 1, 1));
        formPanel.add(communityHubButton, SwingUtils.createConstraints(1, 3, 1, 1, 1, 1));
        
        // Silly box
        Box box = new Box(BoxLayout.Y_AXIS);
        box.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        box.add(Box.createVerticalGlue());
        box.add(formPanel);
        box.add(Box.createVerticalGlue());
        add(box);
    }
    
    private void startGame() {
        // Check if steam is installed
        if(!isSteamInstalled()) {
            JOptionPane.showMessageDialog(getRootPane(), "Steam is required for this action.", "Attention", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Check if host fields aren't empty
        String gatewayHost = gatewayHostField.getText();
        String apiHost = apiHostField.getText();
        
        if(gatewayHost.isEmpty() || apiHost.isEmpty()) {
            JOptionPane.showMessageDialog(getRootPane(), "Please fill in all the fields.", "Attention", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Update registry keys
        ProcessResult addGatewayResult = RegistryUtils.add(GuiConstants.DEEPWORLD_PLAYERPREFS, "gateway", gatewayHost);
        ProcessResult addApiResult = RegistryUtils.add(GuiConstants.DEEPWORLD_PLAYERPREFS, "api", apiHost);
        
        if(!addGatewayResult.wasSuccessful() || !addApiResult.wasSuccessful()) {
            JOptionPane.showMessageDialog(getRootPane(), "Couldn't update gateway/api host settings in the registry."
                    + " You may have to do it manually.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        // Check if the game is patched
        if(!isGamePatched()) {
            JOptionPane.showMessageDialog(getRootPane(), 
                    "It appears that the game has not been patched."
                    + " API features such as the zone searcher will likely not work if this is the case."
                    + " To patch the game, please follow the instructions on the GitHub repository.",
                    "Attention", JOptionPane.WARNING_MESSAGE);
        }
        
        // Start the game!
        DesktopUtils.browseUrl(GuiConstants.RUN_GAME_URL);
    }
    
    private void openCommunityHub() {
        // Check if steam is installed
        if(!isSteamInstalled()) {
            JOptionPane.showMessageDialog(getRootPane(), "Steam is required for this action.", "Attention", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Open the community hub!
        DesktopUtils.browseUrl(GuiConstants.COMMUNITY_HUB_URL);
    }
    
    private boolean isSteamInstalled() {
        RegistryKey steamKey = RegistryUtils.getFirstQueryResult(RegistryUtils.query(GuiConstants.STEAM_REGISTRY_LOCATION));
        return steamKey != null;
    }
    
    private boolean isGamePatched() {
        RegistryKey steamPathKey = RegistryUtils.getFirstQueryResult(
                RegistryUtils.query(GuiConstants.STEAM_REGISTRY_LOCATION, "SteamPath"));
        
        if(steamPathKey != null) {
            String assemblyPath = steamPathKey.getValue() + GuiConstants.DEEPWORLD_ASSEMBLY_PATH;
            File file = new File(assemblyPath);
            
            // Won't always be 100% accurate but it does the job.
            if(file.length() == 3916800) {
                return false;
            }
        }
        
        return true;
    }
    
    private void loadHostSettings() {
        // Gateway
        RegistryKey gatewayHostKey = RegistryUtils.getFirstQueryResult(
                RegistryUtils.query(GuiConstants.DEEPWORLD_PLAYERPREFS, "gateway"));
        gatewayHostField.setText(gatewayHostKey == null ? "local" : gatewayHostKey.getValue());
        
        // API
        RegistryKey apiHostKey = RegistryUtils.getFirstQueryResult(
                RegistryUtils.query(GuiConstants.DEEPWORLD_PLAYERPREFS, "api"));
        apiHostField.setText(apiHostKey == null ? "local" : apiHostKey.getValue());
    }
}
