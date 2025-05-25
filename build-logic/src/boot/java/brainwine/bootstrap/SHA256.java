package brainwine.bootstrap;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256 {
    
    public static String hash(File file) throws IOException {
        return hash(Files.readAllBytes(file.toPath()));
    }
    
    public static String hash(byte[] bytes) {
        MessageDigest digest;
        
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch(NoSuchAlgorithmException e) {
            throw new RuntimeException(e); // This should never happen
        }
        
        byte[] hash = digest.digest(bytes);
        return new BigInteger(1, hash).toString(16).toUpperCase();
    }
}
