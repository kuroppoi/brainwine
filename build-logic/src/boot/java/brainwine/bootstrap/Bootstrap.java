package brainwine.bootstrap;

import static brainwine.bootstrap.Constants.BOOT_CLASS_KEY;
import static brainwine.bootstrap.Constants.JAR_LIBRARY_PATH;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class Bootstrap {
    
    public static void main(String[] args) {
        new Bootstrap().run(args);
    }
    
    private void run(String[] args) {
        String mainClassName = null;
        
        try {
            Enumeration<URL> resources = getClass().getClassLoader().getResources(JarFile.MANIFEST_NAME);
            
            while(resources.hasMoreElements()) {
                try(InputStream inputStream = resources.nextElement().openStream()) {
                    Manifest manifest = new Manifest(inputStream);
                    
                    if(getClass().getName().equals(manifest.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS))) {
                        mainClassName = manifest.getMainAttributes().getValue(BOOT_CLASS_KEY);
                        break;
                    }
                }
            }
        } catch(IOException e) {
            System.err.println("Could not get main class name");
            e.printStackTrace();
            System.exit(-1);
        }
        
        URL[] libraryUrls = null;
        
        try {
            libraryUrls = DirectoryIndex.extractDirectory(JAR_LIBRARY_PATH, new File("libraries"));
        } catch(Exception e) {
            System.err.println("Could not extract library JARs");
            e.printStackTrace();
            System.exit(-1);
        }
        
        URLClassLoader classLoader = new URLClassLoader(libraryUrls, getClass().getClassLoader().getParent());
        Thread.currentThread().setContextClassLoader(classLoader);
        
        try {
            Class<?> mainClass = Class.forName(mainClassName, true, classLoader);
            Method method = mainClass.getMethod("main", String[].class);
            method.invoke(null, (Object)args);
        } catch(ReflectiveOperationException e) {
            System.err.println("Could not invoke entry point");
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
