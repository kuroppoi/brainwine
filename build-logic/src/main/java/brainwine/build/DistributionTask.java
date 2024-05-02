package brainwine.build;

import static brainwine.bootstrap.Constants.BOOT_CLASS_KEY;
import static brainwine.bootstrap.Constants.JAR_LIBRARY_PATH;
import static brainwine.bootstrap.Constants.JAR_LICENSE_PATH;
import static brainwine.bootstrap.Constants.MAIN_CLASS;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileTree;
import org.gradle.api.initialization.IncludedBuild;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.bundling.Jar;

public abstract class DistributionTask extends DefaultTask {
    
    private final FileTree bootCodeTree;
    private File outputDirectory;
    
    @Input
    public abstract Property<String> getMainClass();
    
    @Input
    @Optional
    public abstract Property<String> getArchiveFileName();
    
    @InputFile
    @Optional
    public abstract Property<File> getLicenseFile();
    
    @Inject
    public DistributionTask(Gradle gradle) {
        IncludedBuild build = gradle.getIncludedBuilds().stream().filter(x -> x.getName().equals("build-logic")).findFirst().get();
        bootCodeTree = getProject().fileTree(new File(build.getProjectDir(), "build/classes/java/boot"));
        outputDirectory = new File(getProject().getBuildDir(), "dist");
    }
    
    @TaskAction
    public void createDistributionArchive() throws IOException {
        Configuration config = getProject().getConfigurations().getByName("runtimeClasspath");
        Jar jarTask = (Jar)getProject().getTasks().getByName("jar");
        String archiveFileName = getArchiveFileName().getOrElse(jarTask.getArchiveFileName().get());
        File outputDirectory = new File(getProject().getBuildDir(), "dist");
        outputDirectory.mkdirs();
        File outputFile = new File(outputDirectory, archiveFileName);
        
        // Fetch libraries
        List<File> libraryFiles = new ArrayList<>();
        config.getResolvedConfiguration().getResolvedArtifacts().forEach(artifact -> libraryFiles.add(artifact.getFile()));
        jarTask.getOutputs().getFiles().forEach(libraryFiles::add);
        
        // Create jar manifest
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, MAIN_CLASS.getName());
        manifest.getMainAttributes().put(BOOT_CLASS_KEY, getMainClass().get());
        manifest.getMainAttributes().putValue("Multi-Release", "true");
        
        // Create jar file
        try(JarBundler bundler = new JarBundler(new FileOutputStream(outputFile), manifest)) {
            // Add boot code
            bootCodeTree.visit(details -> {
                if(!details.isDirectory()) {
                    try {
                       bundler.addFile(details.getFile(), details.getPath());
                    } catch(IOException e) {
                        throw new GradleException(e.getMessage(), e);
                    }
                }
            });
            
            // Add libraries
            bundler.embedDirectory(libraryFiles, JAR_LIBRARY_PATH);
            
            // Add license
            if(getLicenseFile().isPresent()) {
                bundler.addFile(getLicenseFile().get(), JAR_LICENSE_PATH);
            }
        }
    }
    
    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
    
    @OutputDirectory
    public File getOutputDirectory() {
        return outputDirectory;
    }
}
