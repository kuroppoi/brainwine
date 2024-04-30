package brainwine.gradle;

import static brainwine.bootstrap.Constants.BOOT_CLASS_KEY;
import static brainwine.bootstrap.Constants.CLASS_PATH_KEY;
import static brainwine.bootstrap.Constants.LIBRARY_PATH;
import static brainwine.bootstrap.Constants.LICENSE_PATH;
import static brainwine.bootstrap.Constants.MAIN_CLASS;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.internal.file.collections.FileTreeAdapter;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.bundling.Jar;

public abstract class DistributionTask extends DefaultTask {
        
    @Input
    public abstract Property<String> getMainClass();
    
    @Input
    @Optional
    public abstract Property<String> getArchiveFileName();
    
    @Input
    @Optional
    public abstract RegularFileProperty getLicenseFile();
    
    @TaskAction
    public void createDistributionArchive() throws IOException {
        Configuration config = getProject().getConfigurations().getByName("runtimeClasspath");
        Configuration bootstrapConfig = getProject().getConfigurations().create("bootstrapClasspath");
        getProject().getDependencies().add("bootstrapClasspath", ":gradle-plugin");
        Jar jarTask = (Jar)getProject().getTasks().getByName("jar");
        String archiveFileName = getArchiveFileName().getOrElse(jarTask.getArchiveFileName().get());
        File outputDirectory = new File(getProject().getBuildDir(), "dist");
        outputDirectory.mkdirs();
        File outputFile = new File(outputDirectory, archiveFileName);
        
        // Fetch libraries
        List<File> classpath = new ArrayList<>();
        config.getResolvedConfiguration().getResolvedArtifacts().forEach(artifact -> classpath.add(artifact.getFile()));
        jarTask.getOutputs().getFiles().forEach(classpath::add);
        classpath.sort((a, b) -> a.getName().compareTo(b.getName())); // Guarantee file order
                
        // Create jar manifest
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, MAIN_CLASS.getName());
        manifest.getMainAttributes().put(BOOT_CLASS_KEY, getMainClass().get());
        manifest.getMainAttributes().put(CLASS_PATH_KEY, String.join(";", classpath.stream().map(File::getName).collect(Collectors.toList())));
        manifest.getMainAttributes().putValue("Multi-Release", "true");
        
        // Create jar file
        try(JarOutputStream outputStream = new JarOutputStream(new FileOutputStream(outputFile))) {
            // Add manifest
            addJarManifest(outputStream, manifest);
            
            // Add libraries
            for(File file : classpath) {
                addFileToJar(outputStream, file, String.format("%s/%s", LIBRARY_PATH, file.getName()));
            }
            
            // Add boot code
            for(ResolvedArtifact artifact : bootstrapConfig.getResolvedConfiguration().getResolvedArtifacts()) {
                FileTreeAdapter tree = (FileTreeAdapter)getProject().zipTree(artifact.getFile()).matching(
                        filterable -> filterable.include(String.format("%s/**", MAIN_CLASS.getPackage().getName().replace('.', '/'))));
                
                for(Entry<String, File> entry : tree.getAsMap().entrySet()) {
                    addFileToJar(outputStream, entry.getValue(), entry.getKey());
                }
            }
            
            // Add license
            RegularFileProperty licenseFile = getLicenseFile();
            
            if(licenseFile.isPresent()) {
                addFileToJar(outputStream, licenseFile.get().getAsFile(), LICENSE_PATH);
            }
        }
    }
    
    private void addJarManifest(JarOutputStream outputStream, Manifest manifest) throws IOException {
        JarEntry entry = new JarEntry(JarFile.MANIFEST_NAME);
        entry.setTime(0);
        outputStream.putNextEntry(entry);
        manifest.write(outputStream);
        outputStream.closeEntry();
    }
    
    private void addFileToJar(JarOutputStream outputStream, File file, String targetPath) throws IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        JarEntry entry = new JarEntry(targetPath);
        entry.setTime(0);
        entry.setSize(bytes.length);
        outputStream.putNextEntry(entry);
        outputStream.write(bytes);
        outputStream.closeEntry();
    }
}
