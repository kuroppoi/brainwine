package brainwine.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;

public class DistributionPlugin implements Plugin<Project> {
    
    @Override
    public void apply(Project project) {
        project.getPlugins().apply(JavaPlugin.class);
        project.getTasks().register("dist", DistributionTask.class, task -> {
            task.dependsOn("build");  
        });
    }
}
