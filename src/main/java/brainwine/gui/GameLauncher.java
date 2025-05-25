package brainwine.gui;

import static brainwine.gui.GuiConstants.DEEPWORLD_ASSEMBLY_PATH;
import static brainwine.gui.GuiConstants.DEEPWORLD_PLAYERPREFS;
import static brainwine.gui.GuiConstants.HTTP_STEAM_DOWNLOAD_URL;
import static brainwine.gui.GuiConstants.STEAM_REGISTRY_LOCATION;
import static brainwine.gui.GuiConstants.STEAM_RUN_GAME_URL;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import brainwine.gui.task.FileDownloadTask;
import brainwine.gui.task.ZipExtractTask;
import brainwine.gui.view.ProgressView;
import brainwine.patch.PatchFile;
import brainwine.util.DesktopUtils;
import brainwine.util.ProcessResult;
import brainwine.util.RegistryKey;
import brainwine.util.RegistryUtils;
import brainwine.util.SwingUtils;

public abstract class GameLauncher {
    
    protected final JComponent owner;
    
    public GameLauncher(JComponent owner) {
        this.owner = owner;
    }
    
    public abstract void startGame();
    
    /**
     * Windows game launcher
     */
    public static class Windows extends GameLauncher {

        public Windows(JComponent owner) {
            super(owner);
        }
        
        @Override
        public void startGame() {
            // Show option to download Steam if it is not installed
            if(!isSteamInstalled()) {
                if(JOptionPane.showConfirmDialog(owner, "You need the Steam desktop application to play Deepworld on Windows.\n"
                        + "Would you like to go to the download page?", "Attention", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    DesktopUtils.browseUrl(HTTP_STEAM_DOWNLOAD_URL);
                }
                
                return;
            }
            
            // Update registry keys
            String serverAddress = GameSettings.getServerAddress();
            String gatewayPort = String.format(":%s", GameSettings.getGatewayPort());
            String apiPort = String.format(":%s", GameSettings.getApiPort());
            boolean appendPort = !serverAddress.equals("local");
            ProcessResult addGatewayResult = RegistryUtils.add(DEEPWORLD_PLAYERPREFS, "gateway", String.format("%s%s", serverAddress, appendPort ? gatewayPort : ""));
            ProcessResult addApiResult = RegistryUtils.add(DEEPWORLD_PLAYERPREFS, "api", String.format("%s%s", serverAddress, appendPort ? apiPort : ""));
            
            if(!addGatewayResult.wasSuccessful() || !addApiResult.wasSuccessful()) {
                JOptionPane.showMessageDialog(owner, "Couldn't update gateway/API host settings in the registry."
                        + " You may have to do it manually.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            
            // Check if the game is patched
            if(!serverAddress.equals("local") && !isGamePatched()) {
                JOptionPane.showMessageDialog(owner, 
                        "It appears that the game has not been patched."
                        + " The world search function will likely not work if this is the case."
                        + " To patch the game, please follow the instructions on the GitHub repository.",
                        "Attention", JOptionPane.WARNING_MESSAGE);
            }
            
            // Start the game!
            DesktopUtils.browseUrl(STEAM_RUN_GAME_URL);
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
    
    /**
     * MacOS game launcher
     */
    public static class Mac extends GameLauncher {
        
        private final JButton startButton;
        private SwingWorker<?, ?> currentTask;
        
        public Mac(JComponent owner, JButton startButton) {
            super(owner);
            this.startButton = startButton;
        }
        
        @Override
        public void startGame() {
            String serverAddress = GameSettings.getServerAddress();
            String version = GameSettings.getGameVersion();
            startGame(serverAddress, version);
        }
        
        private void startGame(String serverAddress, String version) {
            File clientDirectory = new File("clients", String.format("v%s", version));
            File applicationFile = new File(clientDirectory, "Deepworld.app");
            File binaryFile = new File(applicationFile, "Contents/MacOS/Deepworld");
            
            // Download game if necessary
            if(!binaryFile.exists()) {
                if(JOptionPane.showConfirmDialog(owner, String.format("Couldn't find game client for Deepworld v%s\nDo you want to download it?", version),
                    "Attention", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                    return;
                }
                
                // The callback will not run if download or extraction fails
                downloadGameClient(version, clientDirectory, () -> startGame(serverAddress, version));
                return;
            }
            
            // chmod +x
            binaryFile.setExecutable(true);
            
            // Patch the game binary
            try {
                if(!patchGameBinary(binaryFile, serverAddress, version)) {
                    return;
                }
            } catch(IOException e) {
                SwingUtils.showExceptionInfo(owner, "Couldn't patch game binary.", e);
                return;
            }
            
            // Launch the application
            try {
                Desktop.getDesktop().open(applicationFile);
            } catch(IOException e) {
                SwingUtils.showExceptionInfo(owner, "Couldn't launch application.", e);
            }
        }
        
        private boolean patchGameBinary(File binaryFile, String serverAddress, String version) throws IOException {
            PatchFile patchFile = null;
            
            // Load patch file
            try(InputStream inputStream = getClass().getResourceAsStream(String.format("/patches/deepworld-%s.patch", version))) {
                patchFile = new PatchFile(inputStream);
            }
            
            try(RandomAccessFile file = new RandomAccessFile(binaryFile, "rw")) {
                serverAddress = (serverAddress.isEmpty() || serverAddress.equals("local")) ? "http://127.0.0.1:5001" : serverAddress;
                byte[] addressBytes = serverAddress.getBytes();
                
                // Check server address length
                if(addressBytes.length > 33) {
                    JOptionPane.showMessageDialog(owner, "Server address may not exceed 33 characters in length.", "Attention", JOptionPane.WARNING_MESSAGE);
                    return false;
                } 
                
                // Apply patches
                patchFile.apply(file, patch -> {
                    patch.setBytes("gateway_host", addressBytes);
                    patch.setInt("gateway_host_strlen", addressBytes.length);
                });
            }
            
            return true;
        }
        
        private void downloadGameClient(String version, File outputDirectory, Runnable callback) {
            ProgressView progressView = new ProgressView(owner, "Downloading...");
            progressView.addCloseListener(() -> {
                if(currentTask != null && !currentTask.isDone()) {
                    currentTask.cancel(true);
                }
            });
            
            startButton.setEnabled(false);
            String url = String.format("https://github.com/kuroppoi/deepworld-binaries/releases/download/MacOS/deepworld-%s.zip", version);
            FileDownloadTask downloadTask = new FileDownloadTask(url, file -> {
                if(file == null) {
                    startButton.setEnabled(true);
                    progressView.dispose();
                    return;
                }
                
                ZipExtractTask extractTask = new ZipExtractTask(file, outputDirectory, success -> {
                    startButton.setEnabled(true);
                    progressView.dispose();
                    
                    if(success) {
                        callback.run();
                    }
                });
                
                progressView.setText("Extracting...");
                currentTask = extractTask;
                extractTask.addPropertyChangeListener(event -> progressView.setProgress(extractTask.getProgress()));
                extractTask.execute();
            });
            
            currentTask = downloadTask;
            downloadTask.addPropertyChangeListener(event -> progressView.setProgress(downloadTask.getProgress()));
            downloadTask.execute();
        }
    }
}
