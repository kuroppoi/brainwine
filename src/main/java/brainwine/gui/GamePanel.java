package brainwine.gui;

import static brainwine.gui.GuiConstants.COMMUNITY_HUB_URL;
import static brainwine.gui.GuiConstants.DEEPWORLD_ASSEMBLY_PATH;
import static brainwine.gui.GuiConstants.DEEPWORLD_PLAYERPREFS;
import static brainwine.gui.GuiConstants.RUN_GAME_URL;
import static brainwine.gui.GuiConstants.STEAM_REGISTRY_LOCATION;

import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

import brainwine.util.DesktopUtils;
import brainwine.util.ProcessResult;
import brainwine.util.RegistryKey;
import brainwine.util.RegistryUtils;
import brainwine.util.SwingUtils;

@SuppressWarnings("serial")
public class GamePanel extends JPanel {
    
    private final JButton startGameButton;
    private final JButton communityHubButton;
    
    public GamePanel(MainView mainView) {
        setLayout(new GridBagLayout());
        
        // Host settings button
        JButton hostSettingsButton = new JButton("Host Settings", UIManager.getIcon("Brainwine.settingsIcon"));
        hostSettingsButton.addActionListener(event -> mainView.showHostSettings());
        
        // Community hub button
        communityHubButton = new JButton("Community Hub", UIManager.getIcon("Brainwine.communityIcon"));
        communityHubButton.addActionListener(event -> openCommunityHub());
        
        // Start game button
        startGameButton = new JButton("Start Deepworld", UIManager.getIcon("Brainwine.playIcon"));
        startGameButton.addActionListener(event -> startGame());
        
        // Button panel
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        JPanel topPanel = new JPanel(new GridLayout(1, 2));
        topPanel.add(hostSettingsButton);
        topPanel.add(communityHubButton);
        buttonPanel.add(topPanel, SwingUtils.createConstraints(0, 0));
        buttonPanel.add(startGameButton, SwingUtils.createConstraints(0, 1, 2, 1));
        add(buttonPanel);
    }
    
    private void startGame() {
        // Check if steam is installed
        if(!isSteamInstalled()) {
            JOptionPane.showMessageDialog(getRootPane(), "Steam is required for this action.", "Attention", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Update registry keys
        String gatewayHost = GuiPreferences.getString(GuiPreferences.GATEWAY_HOST_KEY, "local");
        String apiHost = GuiPreferences.getString(GuiPreferences.API_HOST_KEY, "local");
        ProcessResult addGatewayResult = RegistryUtils.add(DEEPWORLD_PLAYERPREFS, "gateway", gatewayHost.isEmpty() ? "local" : gatewayHost);
        ProcessResult addApiResult = RegistryUtils.add(DEEPWORLD_PLAYERPREFS, "api", apiHost.isEmpty() ? "local" : apiHost);
        
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
        DesktopUtils.browseUrl(RUN_GAME_URL);
    }
    
    private void openCommunityHub() {
        // Check if steam is installed
        if(!isSteamInstalled()) {
            JOptionPane.showMessageDialog(getRootPane(), "Steam is required for this action.", "Attention", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Open the community hub!
        DesktopUtils.browseUrl(COMMUNITY_HUB_URL);
    }
    
    private boolean isSteamInstalled() {
        RegistryKey steamKey = RegistryUtils.getFirstQueryResult(RegistryUtils.query(STEAM_REGISTRY_LOCATION));
        return steamKey != null;
    }
    
    private boolean isGamePatched() {
        RegistryKey steamPathKey = RegistryUtils.getFirstQueryResult(
                RegistryUtils.query(STEAM_REGISTRY_LOCATION, "SteamPath"));
        
        if(steamPathKey != null) {
            String assemblyPath = steamPathKey.getValue() + DEEPWORLD_ASSEMBLY_PATH;
            File file = new File(assemblyPath);
            
            // Won't always be 100% accurate but it does the job.
            if(file.length() == 3916800) {
                return false;
            }
        }
        
        return true;
    }
}
