package brainwine.gui.task;

import static brainwine.shared.LogMarkers.GUI_MARKER;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.SwingWorker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import brainwine.util.SwingUtils;

public class ZipExtractTask extends SwingWorker<Void, Void> {
    
    private static final Logger logger = LogManager.getLogger();
    private final File zipFile;
    private final File outputDirectory;
    private final Consumer<Boolean> callback;
    
    public ZipExtractTask(File zipFile, File outputDirectory, Consumer<Boolean> callback) {
        this.zipFile = zipFile;
        this.outputDirectory = outputDirectory;
        this.callback = callback;
    }
    
    @Override
    protected Void doInBackground() throws Exception {
        byte[] buffer = new byte[1024];
        long totalLength = zipFile.length();
        long extracted = 0;
        
        try(ZipInputStream inputStream = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry = null;
            
            while((entry = inputStream.getNextEntry()) != null) {
                if(entry.isDirectory()) {
                    continue;
                }
                
                File file = new File(outputDirectory, entry.getName());
                file.getParentFile().mkdirs();
                
                try(FileOutputStream outputStream = new FileOutputStream(file)) {
                    int length = 0;
                    
                    while((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                }
                
                extracted += entry.getCompressedSize();
                inputStream.closeEntry();
                setProgress((int)(extracted / (double)totalLength * 100));
            }
        }
        
        return null;
    }
    
    @Override
    protected void done() {        
        try {
            get();
            callback.accept(true);
            return;
        } catch(ExecutionException e) {
            String message = "Could not extract archive.";
            logger.error(GUI_MARKER, message, e.getCause());
            SwingUtils.showExceptionInfo(null, message, e.getCause());
        } catch(CancellationException | InterruptedException e) {
            // Discard silently
        }
        
        callback.accept(false);
    }
}
