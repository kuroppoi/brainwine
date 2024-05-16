package brainwine.gui.task;

import static brainwine.shared.LogMarkers.GUI_MARKER;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import javax.swing.SwingWorker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import brainwine.util.SwingUtils;

public class FileDownloadTask extends SwingWorker<File, Void> {
    
    private static final Logger logger = LogManager.getLogger();
    private final String urlString;
    private final Consumer<File> callback;
    
    public FileDownloadTask(String urlString, Consumer<File> callback) {
        this.urlString = urlString;
        this.callback = callback;
    }
    
    @Override
    protected File doInBackground() throws Exception {
        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();
        long totalLength = connection.getContentLengthLong();
        int length = 0;
        long downloaded = 0;
        byte[] buffer = new byte[1024];
        File file = File.createTempFile("download", ".tmp");
        file.deleteOnExit();
        BufferedOutputStream outputStream = null;
        
        try(BufferedInputStream inputStream = new BufferedInputStream(connection.getInputStream())) {
            outputStream = new BufferedOutputStream(new FileOutputStream(file));
            
            while((length = inputStream.read(buffer)) != -1) {
                downloaded += length;
                int progress = (int)(downloaded / (double)totalLength * 100);
                setProgress(progress);
                outputStream.write(buffer, 0, length);
            }
        } finally {
            if(outputStream != null) {
                outputStream.close();
            }
        }
        
        return file;
    }
    
    @Override
    protected void done() {
        File file = null;
        
        try {
            file = get();
        } catch(ExecutionException e) {
            String message = "Could not download file.";
            logger.error(GUI_MARKER, message, e.getCause());
            SwingUtils.showExceptionInfo(null, message, e.getCause());
        } catch(CancellationException | InterruptedException e) {
            // Discard silently
        }
        
        callback.accept(file);
    }
}
