package brainwine.build;

import static brainwine.bootstrap.Constants.DIRECTORY_INDEX_FILE;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import brainwine.bootstrap.DirectoryIndex;
import brainwine.bootstrap.SHA256;

public class JarBundler implements AutoCloseable {
    
    private final JarOutputStream outputStream;
    
    public JarBundler(OutputStream outputStream, Manifest manifest) throws IOException {
        this.outputStream = new JarOutputStream(outputStream, manifest);
    }
    
    @Override
    public void close() throws IOException {
        outputStream.close();
    }
    
    public void embedDirectory(Collection<File> files, String path) throws IOException {
        DirectoryIndex index = new DirectoryIndex();
        
        // Add files
        for(File file : files) {
            String name = file.getName();
            byte[] bytes = Files.readAllBytes(file.toPath());
            String hash = SHA256.hash(bytes);
            addFile(bytes, String.format("%s/%s", path, file.getName()));
            index.put(name, hash);
        }
        
        // Add index file containing file names and hashes
        addEntry(String.format("%s/%s", path, DIRECTORY_INDEX_FILE), index::write);
    }
    
    public void addFile(File file, String name) throws IOException {
        addFile(Files.readAllBytes(file.toPath()), name);
    }
    
    public void addFile(byte[] bytes, String name) throws IOException {
        addEntry(name, outputStream -> outputStream.write(bytes));
    }
    
    public void addEntry(String name, OutputStreamWriter writer) throws IOException {
        JarEntry entry = new JarEntry(name);
        outputStream.putNextEntry(entry);
        writer.write(outputStream);
        outputStream.closeEntry();
    }
}
