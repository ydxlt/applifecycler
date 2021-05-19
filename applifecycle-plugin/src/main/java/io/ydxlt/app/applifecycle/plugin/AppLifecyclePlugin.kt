package io.ydxlt.app.applifecycle.plugin

import com.android.build.gradle.AppExtension
import org.apache.commons.compress.utils.IOUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import java.io.File
import java.util.*

private const val WRAPPER_SUFFIX = "_AppLifeCycleWrapper"
private const val APP_LIFECYCLE_CALLBACK = "io/ydxlt/app/lifecycle/api/AppLifecycleCallback"
private const val APP_LIFECYCLES_FILE = "io/ydxlt/app/lifecycle/api/AppLifecycles.class"

/**
 * 修改AppLifecycles的构造方法，添加对应的实例
 */
class AppLifecyclePlugin : BaseTransform(), Plugin<Project> {

    override fun getName(): String = "AppLifecycles"

    private val mAppLifecycleClassNames = mutableListOf<String>()
    private var mAppLifecyclesJar:File? = null

    override fun apply(project: Project) {
        project.extensions.findByType(AppExtension::class.java)?.registerTransform(this)
    }

    override fun transformFile(output: File) {
        val classReader = ClassReader(output.readBytes())
        if (classReader.className.endsWith(WRAPPER_SUFFIX) && classReader.interfaces.contains(
                APP_LIFECYCLE_CALLBACK
            )
        ) {
            mAppLifecycleClassNames.add(classReader.className)
        }
    }

    override fun transformJar(inputJar: File, outputJar: File) {
        JarHandler.create(inputJar,outputJar).filter { _, jarEntity ->
            if(jarEntity.name == APP_LIFECYCLES_FILE){
                mAppLifecyclesJar = outputJar
            }
            jarEntity.name.isClass() && jarEntity.name.endsWith("$WRAPPER_SUFFIX.class")
        }.map { _, inputStream ->
            val byteArray = IOUtils.toByteArray(inputStream)
            val classReader = ClassReader(byteArray)
            if (classReader.className.endsWith(WRAPPER_SUFFIX) && classReader.interfaces.contains(
                    APP_LIFECYCLE_CALLBACK
                )
            ) {
                mAppLifecycleClassNames.add(classReader.className)
            }
            byteArray
        }
    }

    override fun onTransformCompleted() {
        if(mAppLifecyclesJar == null){
            println("错误：没有找到包含${APP_LIFECYCLES_FILE}的jar")
            return
        }
        val targetJar = mAppLifecyclesJar?:return
        // 创建临时jar
        val file = File(targetJar.parent,targetJar.nameWithoutExtension+"_temp.jar")
        if(file.exists()){
            file.delete()
        }
        targetJar.copyTo(file,true)
        JarHandler(file,targetJar).filter { _, jarEntity ->
            jarEntity.name == APP_LIFECYCLES_FILE
        }.doComplete {
            file.delete()
        }.map { _, inputStream ->
            val classReader = ClassReader(
                IOUtils.toByteArray(inputStream)
            )
            val classWriter = ClassWriter(
                classReader,
                ClassWriter.COMPUTE_MAXS
            )
            val cv: ClassVisitor = AppLifecycleVisitor(classWriter,mAppLifecycleClassNames)
            classReader.accept(cv, ClassReader.EXPAND_FRAMES)
            classWriter.toByteArray()
        }
    }
}
