package brainwine.patch;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DynamicPatch {
    
    private final Map<Long, byte[]> data = new HashMap<>();
    private final PatchFile patchFile;
    
    protected DynamicPatch(PatchFile patchFile) {
        this.patchFile = patchFile;
    }
    
    protected void apply(RandomAccessFile file) throws IOException {
        for(long address : data.keySet()) {
            byte[] bytes = data.get(address);
            byte[] original = new byte[bytes.length];
            file.seek(address);
            file.read(original);
            
            if(!Arrays.equals(bytes, original)) {
                file.seek(address);
                file.write(bytes);
            }
        }
    }
    
    public void setBytes(String name, byte[] bytes) {
        long address = patchFile.getAddress(name);
        
        if(address != -1) {
            data.put(address, bytes);
        }
    }
    
    public void setInt(String name, int value) {
        setBytes(name, new byte[] {
                (byte)(value),
                (byte)(value >> 8),
                (byte)(value >> 16),
                (byte)(value >> 24)});
    }
}
