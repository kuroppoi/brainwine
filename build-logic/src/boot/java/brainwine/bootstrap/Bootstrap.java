package brainwine.bootstrap;

import static brainwine.bootstrap.Constants.BOOT_CLASS_KEY;
import static brainwine.bootstrap.Constants.CLASS_PATH_KEY;
import static brainwine.bootstrap.Constants.LIBRARY_PATH;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class Bootstrap {
    
    public static void main(String[] args) {
        new Bootstrap().run(args);
    }
    
    private void run(String[] args) {
        Attributes attributes = null;
        
        try {
            Enumeration<URL> resources = getClass().getClassLoader().getResources(JarFile.MANIFEST_NAME);
            
            while(resources.hasMoreElements()) {
                try(InputStream inputStream = resources.nextElement().openStream()) {
                    Manifest manifest = new Manifest(inputStream);                    
                    if(getClass().getName().equals(manifest.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS))) {
                        attributes = manifest.getMainAttributes();
                        break;
                    }
                }
            }
        } catch(IOException e) {
            System.err.println("Could not load manifest file");
            e.printStackTrace();
            System.exit(-1);
        }
        
        String[] libraryNames = attributes.getValue(CLASS_PATH_KEY).split(";");
        URL[] libraryUrls = new URL[libraryNames.length];
        
        try {            
            for(int i = 0; i < libraryNames.length; i++) {
                String libraryName = libraryNames[i];
                
                try(InputStream inputStream = getClass().getResourceAsStream(String.format("/%s/%s", LIBRARY_PATH, libraryName))) {
                    File outputFile = new File("libraries", libraryName);
                    libraryUrls[i] = outputFile.toURI().toURL();
                    
                    if(outputFile.exists()) {
                        continue;
                    }
                    
                    outputFile.getParentFile().mkdirs();
                    Files.copy(inputStream, outputFile.toPath());
                }
            }
        } catch(Exception e) {
            System.err.println("Could not extract library JARs");
            e.printStackTrace();
            System.exit(-1);
        }
        
        URLClassLoader classLoader = new URLClassLoader(libraryUrls, getClass().getClassLoader().getParent());
        Thread.currentThread().setContextClassLoader(classLoader);
        
        try {
            Class<?> mainClass = Class.forName(attributes.getValue(BOOT_CLASS_KEY), true, classLoader);
            Method method = mainClass.getMethod("main", String[].class);
            method.invoke(null, (Object)args);
        } catch(ReflectiveOperationException e) {
            System.err.println("Could not invoke entry point");
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
