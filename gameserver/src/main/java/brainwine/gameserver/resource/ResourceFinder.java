package brainwine.gameserver.resource;

import static brainwine.shared.LogMarkers.SERVER_MARKER;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

/**
 * Helper class for finding the locations of gameserver configuration resources (loot tables, prefabs etc.)
 */
public class ResourceFinder {
    
    private static final Logger logger = LogManager.getLogger();
    
    /**
     * Calls {@link #getResourceUrl(String, boolean)} with {@code overridable true}.
     */
    public static URL getResourceUrl(String name) {
        return getResourceUrl(name, true);
    }
    
    /**
     * @param name The name of the resource.
     * @param overridable If {@code true}, files in the working directory are counted and will take priority.
     * @return The {@link URL} of the specified resource, or {@code null} if it doesn't exist.
     */
    public static URL getResourceUrl(String name, boolean overridable) {
        if(overridable) {
            File file = new File(name);
            
            if(file.exists()) {
                try {
                    return file.toURI().toURL();
                } catch(MalformedURLException e) {
                    logger.error(SERVER_MARKER, "Couldn't get URL for file '{}'", name);
                    return null;
                }
            }
        }
        
        return ResourceFinder.class.getResource(String.format("/%s", name));
    }
    
    /**
     * Calls {@link #getResource(String, boolean)} with {@code overridable true}.
     */
    public static Resource getResource(String name) {
        return getResource(name, true);
    }
    
    /**
     * @param name The name of the resource.
     * @param overridable If {@code true}, files in the working directory are counted and will take priority.
     * @return A {@link Resource} object for the specified resource, or {@code null} if it doesn't exist.
     */
    public static Resource getResource(String name, boolean overridable) {
        URL url = getResourceUrl(name, overridable);
        
        if(url == null) {
            return null;
        }
        
        String simpleName = name;
        String parentDirectoryName = ""; // TODO null default?
        int lastSeparatorIndex = name.lastIndexOf('/');
        
        if(lastSeparatorIndex != -1) {
            simpleName = name.substring(lastSeparatorIndex + 1);
            parentDirectoryName = name.substring(0, lastSeparatorIndex);
        }
        
        return new Resource(name, simpleName, parentDirectoryName, url);
    }
    
    /**
     * Calls {@link #getResources(String, boolean)} with {@code recursive true}.
     */
    public static List<Resource> getResources(String directory) {
        return getResources(directory, true);
    }
    
    /**
     * Calls {@link #getResources(String, boolean, boolean)} with {@code overridable true}.
     */
    public static List<Resource> getResources(String directory, boolean recursive) {
        return getResources(directory, recursive, true);
    }
    
    /**
     * @param directory The name of the directory containing the desired resources.
     * @param recursive If {@code true}, resources in subdirectories will be counted as well.
     * @param overridable If {@code true}, files in the working directory are counted and will take priority.
     * @return A list of {@link #Resource} objects representing the found resources.
     */
    public static List<Resource> getResources(String directory, boolean recursive, boolean overridable) {
        Set<String> resourceNames = getResourceNames(directory, recursive, overridable);
        return resourceNames.stream().map(x -> getResource(x, overridable)).collect(Collectors.toList());
    }
    
    /**
     * Helper function to find resource names in a given resource directory.
     */
    private static Set<String> getResourceNames(String directory, boolean recursive, boolean overridable) {
        Set<String> resourceNames = overridable ? getFileNames(new File(directory), recursive) : new HashSet<>();
        FilterBuilder filter = new FilterBuilder().includePattern(String.format("%s/.*", directory));
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forResource(directory))
                .filterInputsBy(recursive ? filter : filter.excludePattern(String.format("%s/.*/.*", directory)))
                .setScanners(Scanners.Resources));
        reflections.getResources(".*").stream()
                .filter(x -> !resourceNames.contains(x))
                .forEach(resourceNames::add);
        return resourceNames;
    }
    
    /**
     * Helper function to find file names in a given directory.
     */
    private static Set<String> getFileNames(File directory, boolean recursive) {
        Set<String> fileNames = new HashSet<>();
        Queue<File> processQueue = new ArrayDeque<>();
        
        if(directory.isDirectory()) {
            for(File file : directory.listFiles()) {
                processQueue.add(file);
            }
        }
        
        while(!processQueue.isEmpty()) {
            File file = processQueue.poll();
            
            if(file.isDirectory()) {
                if(recursive) {
                    for(File child : file.listFiles()) {
                        processQueue.add(child);
                    }
                }
            } else {
                String name = file.getPath().replace(File.separatorChar, '/'); // TODO will this always work and not do funny things with the path?
                fileNames.add(name);
            }
        }
        
        return fileNames;
    }
    
    /**
     * @deprecated Will be moved to another location later.
     */
    public static String removeFileSuffix(String string) {
        if(!string.contains(".")) {
            return string;
        }
        
        return string.substring(0, string.lastIndexOf('.'));
    }
}
