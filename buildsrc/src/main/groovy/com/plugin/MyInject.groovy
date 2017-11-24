package com.plugin

import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import javassist.NotFoundException
import javassist.bytecode.DuplicateMemberException
import org.gradle.api.Project

import java.lang.annotation.Annotation

public class MyInject {

    private final static ClassPool pool = ClassPool.getDefault();

    public
    static void injectDir(String path, String packageName, Project project) throws NotFoundException {
        pool.appendClassPath(path);
        //project.android.bootClasspath 加入android.jar，否则找不到android相关的所有类
        pool.appendClassPath(project.android.bootClasspath[0].toString());
        Utils.importBaseClass(pool);
        File dir = new File(path);
        if (dir.isDirectory()) {
            dir.eachFileRecurse { File file ->
                String filePath = file.absolutePath//确保当前文件是class文件，并且不是系统自动生成的class文件
                if (filePath.endsWith(".class") && !filePath.contains('R$') && !filePath.contains('$')//代理类
                        && !filePath.contains('R.class') && !filePath.contains("BuildConfig.class")) {
                    // 判断当前目录是否是在我们的应用包里面
                    int index = filePath.indexOf(packageName);
                    boolean isMyPackage = index != -1;
                    if (isMyPackage) {
                        String className = Utils.getClassName(index, filePath);
                        transFile(className, path, packageName, project)
                    }
                }
            }
        }
    }


    static void transFile(String className, String path, String packageName, Project project) {
        CtClass c = pool.getCtClass(className)
//                        是否解冻
        if (c.isFrozen()) {
//                            没有就解冻
            c.defrost()
        }
        BusInfo mBusInfo = new BusInfo()
        mBusInfo.setProject(project)
        mBusInfo.setClazz(c)
        if (c.getName().endsWith("Activity")
                || c.getSuperclass().getName().endsWith("Activity")) {
            mBusInfo.setIsActivity(true)
        }
        boolean isAnnotationByBus = false;
        //getDeclaredMethods获取自己申明的方法，c.getMethods()会把所有父类的方法都加上
        for (CtMethod ctmethod : c.getDeclaredMethods()) {
            isAnnotationByBus = transMethod(ctmethod, mBusInfo, project, c, isAnnotationByBus)
        }

        if (((mBusInfo.BusRegisterMethod != null && mBusInfo.BusUnRegisterMethod == null
                || mBusInfo.BusRegisterMethod == null && mBusInfo.BusUnRegisterMethod != null)))
            assert false: Utils.getBusErr()
        if (mBusInfo != null && isAnnotationByBus) {
            try {
                BusHelper.intBus(mBusInfo, path)
            } catch (DuplicateMemberException e) {
            }
        }
        c.detach()//用完一定记得要卸载，否则pool里的永远是旧的代码
        mBusInfo.project.logger.error(mBusInfo.clazz.name)
    }

    private
    static boolean transMethod(CtMethod ctmethod, BusInfo mBusInfo, Project project, CtClass c, boolean isAnnotationByBus) {
        String methodName = Utils.getSimpleName(ctmethod);
//                           create
        if (BusHelper.ON_CREATE.contains(methodName)) {
            mBusInfo.setOnCreateMethod(ctmethod)
        }

//                            destory
        if (BusHelper.ON_DESTROY.contains(methodName)) {
            mBusInfo.setOnDestroyMethod(ctmethod)
        }

//                          method
        for (Annotation mAnnotation : ctmethod.getAnnotations()) {
            project.logger.error(mAnnotation.getMetaPropertyValues().toString())
            if (mAnnotation.annotationType().canonicalName.equals(BusHelper.OkBusRegisterAnnotation)) {
                mBusInfo.setBusRegisterMethod(ctmethod)
            }
            if (mAnnotation.annotationType().canonicalName.equals(BusHelper.OkBusUnRegisterAnnotation)) {
                mBusInfo.setBusUnRegisterMethod(ctmethod)
            }
            if (mAnnotation.annotationType().canonicalName.equals(BusHelper.OkBusAnnotation)) {
                project.logger.info " method:" + c.getName() + " -" + ctmethod.getName()
                mBusInfo.methods.add(ctmethod)
                mBusInfo.annotations.add(mAnnotation)
                if (!isAnnotationByBus)
                    isAnnotationByBus = true
            }
        }


        if (methodName.equals("getEvent2Message")) {
            ctmethod.insertBefore("return \"husongzhen\";")
        }



        isAnnotationByBus
    }

}