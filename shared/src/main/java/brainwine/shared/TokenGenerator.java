package brainwine.shared;

import java.security.SecureRandom;
import java.util.function.Function;

/**
 * Utility for generating alphanumeric tokens.
 */
public class TokenGenerator {
    
    public static final String CHARTABLE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
    public static final int MAX_ATTEMPTS = 100;
    private static final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * Attempts to generate a unique token by testing for duplicates with the caller-specified function.
     * If no unique token is generated within 100 attempts, the function gives up and returns {@code null} instead.
     */
    public static String generateToken(int size, Function<String, Boolean> dupCheck) {
        int attempts = MAX_ATTEMPTS;
        
        while(attempts > 0) {
            String token = generateToken(size);
            
            if(!dupCheck.apply(token)) {
                return token;
            }
            
            attempts--;
        }
        
        return null;
    }
    
    /**
     * Securely generates a token of the specified size.
     */
    public static String generateToken(int size) {
        char[] chars = new char[size];
        
        for(int i = 0; i < size; i++) {
            chars[i] = CHARTABLE.charAt(secureRandom.nextInt(CHARTABLE.length()));
        }
        
        return new String(chars);
    }
}
