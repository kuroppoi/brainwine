package brainwine.patch;

import java.io.IOException;
import java.io.RandomAccessFile;

public class BytePatch {
    
    private final long address;
    private final byte from;
    private final byte to;
    
    public BytePatch(long address, int from, int to) {
        this.address = address;
        this.from = (byte)from;
        this.to = (byte)to;
    }
    
    public void apply(RandomAccessFile file) throws IOException {
        file.seek(address);
        
        if(file.readByte() != to) {
            file.seek(address);
            file.writeByte(to);
        }
    }
    
    public void validate(RandomAccessFile file) throws IOException {
        if(address < 0 || address >= file.length()) {
            throw new IOException(String.format("Address %08X exceeds file range %08X", address, file.length() - 1));
        }
        
        if(address >= 0 && address < file.length()) {
            file.seek(address);
            byte b = file.readByte();
            
            if(b != from && b != to) {
                throw new IOException(String.format("Byte at %08X failed to validate: expected %02X or %02X, got %02X", address, from, to, b));
            }
        }
    }
}
