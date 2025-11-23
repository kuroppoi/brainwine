package brainwine.api.config;

import java.beans.ConstructorProperties;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SslConfig {
    
    public static final SslConfig DEFAULT_CONFIG = new SslConfig(false, "./keystore", "password");
    private final boolean enableSsl;
    private final String keyStorePath;
    private final String keyStorePassword;
    
    @ConstructorProperties({"enable_ssl", "keystore_path", "keystore_password"})
    public SslConfig(boolean enableSsl, String keyStorePath, String keyStorePassword) {
        this.enableSsl = enableSsl;
        this.keyStorePath = keyStorePath;
        this.keyStorePassword = keyStorePassword;
    }
    
    @JsonGetter("enable_ssl")
    public boolean isSslEnabled() {
        return enableSsl;
    }
    
    @JsonGetter("keystore_path")
    public String getKeyStorePath() {
        return keyStorePath;
    }
    
    @JsonGetter("keystore_password")
    public String getKeyStorePassword() {
        return keyStorePassword;
    }
}
