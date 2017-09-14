package com.plugin

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import com.android.build.gradle.AppExtension;

/**
 * Created by hp on 2016/4/8.
 */
public class Register implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.logger.error "================自定义插件成功！=========="
//        project.android.registerTransform(new PredexTransform(project))

        def android = project.extensions.findByType(AppExtension)
        android.registerTransform(new PredexTransform(project))

    }
}