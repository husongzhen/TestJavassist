package com.plugin

import com.android.build.api.transform.*
import com.google.common.collect.Sets
import javassist.ClassPool
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

/**
 * 作者：husongzhen on 17/9/14 11:09
 * 邮箱：husongzhen@musikid.com
 */

public class PredexTransform extends Transform {


    Project project;

    PredexTransform(Project project) {
        this.project = project
    }

    @Override
    String getName() {
        return "PredexTransform"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return Sets.immutableEnumSet(QualifiedContent.DefaultContentType.CLASSES)
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return Sets.immutableEnumSet(QualifiedContent.Scope.PROJECT, QualifiedContent.Scope.PROJECT_LOCAL_DEPS,
                QualifiedContent.Scope.SUB_PROJECTS, QualifiedContent.Scope.SUB_PROJECTS_LOCAL_DEPS,
                QualifiedContent.Scope.EXTERNAL_LIBRARIES)
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {
        super.transform(context, inputs, referencedInputs, outputProvider, isIncremental)
        project.logger.error("JavassistTransform start :");
        def startTime = System.currentTimeMillis();
        // Transform的inputs有两种类型，一种是目录，一种是jar包，要分开遍历
        inputs.each { TransformInput input ->
            try {
                input.jarInputs.each {
                    MyInject.injectDir(it.file.getAbsolutePath(), "com", project)
                    String outputFileName = it.name.replace(".jar", "") + '-' + it.file.path.hashCode()
                    def output = outputProvider.getContentLocation(outputFileName, it.contentTypes, it.scopes, Format.JAR)
                    FileUtils.copyFile(it.file, output)
                }
            } catch (Exception e) {
                project.logger.err e.getMessage()
            }
            //对类型为“文件夹”的input进行遍历
            input.directoryInputs.each { DirectoryInput directoryInput ->
                //文件夹里面包含的是我们手写的类以及R.class、BuildConfig.class以及R$XXX.class等
                MyInject.injectDir(directoryInput.file.absolutePath, "com", project)
                // 获取output目录
                def dest = outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes,
                        Format.DIRECTORY)

                // 将input的目录复制到output指定目录
                FileUtils.copyDirectory(directoryInput.file, dest)
            }
        }
        ClassPool.getDefault().clearImportedPackages();
        project.logger.error("JavassistTransform cast :" + (System.currentTimeMillis() - startTime) / 1000 + " secs");
    }
}
