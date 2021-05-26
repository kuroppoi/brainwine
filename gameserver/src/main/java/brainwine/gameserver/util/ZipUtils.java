package brainwine.gameserver.util;

import java.io.ByteArrayOutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class ZipUtils {
    
    public static byte[] deflateBytes(byte[] bytes) {
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Deflater deflater = new Deflater();
        deflater.setInput(bytes);
        deflater.finish();
        
        while(!deflater.finished()) {
            outputStream.write(buffer, 0, deflater.deflate(buffer));
        }
        
        deflater.end();
        return outputStream.toByteArray();
    }
    
    public static byte[] inflateBytes(byte[] input) throws DataFormatException {
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Inflater inflater = new Inflater();
        inflater.setInput(input);
        
        while(!inflater.finished()) {
            outputStream.write(buffer, 0, inflater.inflate(buffer));
        }
        
        inflater.end();
        return outputStream.toByteArray();
    }
}
