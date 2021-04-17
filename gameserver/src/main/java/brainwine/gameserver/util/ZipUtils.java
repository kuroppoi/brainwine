package brainwine.gameserver.util;

import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class ZipUtils {
    
    public static byte[] deflateBytes(byte[] bytes) {
        byte[] buffer = new byte[bytes.length];
        Deflater deflater = new Deflater();
        deflater.setInput(bytes);
        deflater.finish();
        byte[] output = new byte[deflater.deflate(buffer)];
        deflater.end();
        System.arraycopy(buffer, 0, output, 0, output.length);
        return output;
    }
    
    public static byte[] inflateBytes(byte[] input) throws DataFormatException {
        return inflateBytes(input, Short.MAX_VALUE);
    }
    
    public static byte[] inflateBytes(byte[] input, int bufferSize) throws DataFormatException {
        byte[] buffer = new byte[bufferSize];
        Inflater inflater = new Inflater();
        inflater.setInput(input, 0, input.length);
        byte[] output = new byte[inflater.inflate(buffer)];
        inflater.end();
        System.arraycopy(buffer, 0, output, 0, output.length);
        return output;
    }
}
