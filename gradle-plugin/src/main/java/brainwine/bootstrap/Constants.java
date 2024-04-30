package brainwine.bootstrap;

import java.util.jar.Attributes;

public class Constants {
    
    public static final Attributes.Name BOOT_CLASS_KEY = new Attributes.Name("Dist-Boot-Class");
    public static final Attributes.Name CLASS_PATH_KEY = new Attributes.Name("Dist-Class-Path");
    public static final String LICENSE_PATH = "META-INF/LICENSE";
    public static final String LIBRARY_PATH = "META-INF/libraries";
    public static final Class<?> MAIN_CLASS = Bootstrap.class;
}
