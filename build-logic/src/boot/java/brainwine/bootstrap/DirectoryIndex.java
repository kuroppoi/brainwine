package brainwine.bootstrap;

import static brainwine.bootstrap.Constants.DIRECTORY_INDEX_FILE;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class DirectoryIndex {
    
    private final Map<String, String> map = new HashMap<>();
    
    @Override
    public int hashCode() {
        return map.hashCode();
    }
    
    @Override
    public boolean equals(Object object) {
        return map.equals(object);
    }
    
    public static URL[] extractDirectory(String path, File outputDirectory) throws IOException {
        DirectoryIndex index = new DirectoryIndex();
        outputDirectory.mkdirs();
        
        // Read index file
        try(InputStream inputStream = DirectoryIndex.class.getResourceAsStream(String.format("/%s/%s", path, DIRECTORY_INDEX_FILE))) {
            index.read(inputStream);
        }
        
        URL[] urls = new URL[index.size()];
        int loopIndex = 0;
        
        // Extract files
        for(String name : index.getNames()) {            
            try(InputStream inputStream = DirectoryIndex.class.getResourceAsStream(String.format("/%s/%s", path, name))) {
                File outputFile = new File(outputDirectory, name);
                urls[loopIndex++] = outputFile.toURI().toURL();
                
                // Skip file if it already exists and the hashes match
                if(outputFile.exists() && index.getHash(name).equals(SHA256.hash(outputFile))) {
                    continue;
                }
                
                Files.copy(inputStream, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
        
        return urls;
    }
    
    public void read(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        reader.lines().forEach(line -> {
            String[] segments = line.split("\t");
            
            if(segments.length == 2) {
                map.put(segments[0], segments[1]);
            }
        });
    }
    
    public void write(OutputStream outputStream) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        
        for(String name : map.keySet()) {
            writer.write(String.format("%s\t%s\n", name, map.get(name)));
        }
        
        writer.flush();
    }
    
    public void put(String name, String hash) {
        map.put(name, hash);
    }
    
    public String remove(String name) {
        return map.remove(name);
    }
    
    public String getHash(String name) {
        return map.get(name);
    }
    
    public Set<String> getNames() {
        return map.keySet();
    }
    
    public Collection<String> getHashes() {
        return map.values();
    }
    
    public Set<Entry<String, String>> getEntries() {
        return map.entrySet();
    }
    
    public int size() {
        return map.size();
    }
}
