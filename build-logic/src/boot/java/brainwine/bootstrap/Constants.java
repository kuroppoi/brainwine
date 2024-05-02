package brainwine.bootstrap;

import java.util.jar.Attributes;

public class Constants {
    
    public static final Attributes.Name BOOT_CLASS_KEY = new Attributes.Name("Boot-Class");
    public static final String DIRECTORY_INDEX_FILE = "index";
    public static final String JAR_LICENSE_PATH = "META-INF/LICENSE";
    public static final String JAR_LIBRARY_PATH = "META-INF/libraries";
    public static final Class<?> MAIN_CLASS = Bootstrap.class;
}
