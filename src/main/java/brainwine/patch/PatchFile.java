package brainwine.patch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PatchFile {
    
    private final Map<String, Long> addresses = new HashMap<>();
    private final List<BytePatch> bytePatches = new ArrayList<>();
    private long binarySize;
    
    public PatchFile(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        List<String> lines = reader.lines().collect(Collectors.toList());
        
        for(String line : lines) {
            parseLine(line);
        }
    }
    
    private void parseLine(String line) throws IOException {
        // Remove excessive whitespace
        line = line.trim().replaceAll(" +", " ");
        
        // Ignore empty lines and comments
        if(line.isEmpty() || line.startsWith("#")) {
            return;
        }
        
        String[] entry = line.split(" ", 2);
        
        // Throw exception if entry is just a key with no value
        if(entry.length == 1) {
            throw new IOException("Entry must be a key followed by a value");
        }
        
        String key = entry[0];
        String value = entry[1];
        
        // Process value based on key
        switch(key) {
            case "binary_name":
                break;
            case "binary_size":
                binarySize = Long.decode(entry[1]);
                break;
            case "location":
                String[] segments = value.split(" ");
                
                if(segments.length != 2) {
                    throw new IOException("Location must be a name followed by an address");
                }
                
                addresses.put(segments[0], Long.decode(segments[1]));
                break;
            case "byte_patch":
                segments = value.split(" ");
                
                if(segments.length != 3) {
                    throw new IOException("Byte patch must be an address followed by the expected and target bytes");
                }
                
                bytePatches.add(new BytePatch(Long.decode(segments[0]), Integer.decode(segments[1]), Integer.decode(segments[2])));
                break;
            default:
                throw new IOException(String.format("Unknown key: %s", key));
        }
    }
    
    public void apply(RandomAccessFile file, Consumer<DynamicPatch> propertySetter) throws IOException {
        // Validate binary info
        if(file.length() != binarySize) {
            throw new IOException(String.format("Binary size doesn't match: got %08X, expected %08X", file.length(), binarySize));
        }
        
        // Validate byte patches
        for(BytePatch patch : bytePatches) {
            patch.validate(file);
        }
        
        // Apply byte patches
        for(BytePatch patch : bytePatches) {
            patch.apply(file);
        }
        
        // Apply dynamic patches
        DynamicPatch patch = new DynamicPatch(this);
        propertySetter.accept(patch);
        patch.apply(file);
    }
    
    public long getAddress(String name) {
        return addresses.getOrDefault(name, -1L);
    }
}
