package io.ydxlt.app.applifecycle.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.ide.common.internal.WaitableExecutor
import com.android.utils.FileUtils
import java.io.File
import java.util.concurrent.Callable

abstract class BaseTransform : Transform() {

    companion object {
        private const val DEBUG = true
    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    final override fun isIncremental(): Boolean {
        return true
    }

    final override fun transform(transformInvocation: TransformInvocation?) {
        val invocation = transformInvocation ?: return
        val executor = WaitableExecutor.useGlobalSharedThreadPool()
        val dirInputs = mutableListOf<DirectoryInput>()
        val jarInputs = mutableListOf<JarInput>()
        invocation?.inputs?.forEach { input ->
            dirInputs.addAll(input.directoryInputs)
            jarInputs.addAll(input.jarInputs)
        }
        dirInputs.forEach(invocation) { input, output ->
            val inputDir = input.file.absolutePath
            val outputDir = output.absolutePath
            if (invocation.isIncremental) {
                input.changedFiles.filter { it.key.isClass() }.forEach { (file, status) ->
                    if (status in arrayOf(Status.CHANGED, Status.ADDED)) {
                        executor.execute {
                            val outputFile = File(file.absolutePath.replace(inputDir, outputDir))
                            FileUtils.deleteIfExists(outputFile)
                            file.copyTo(outputFile)
                            log("transform $outputFile with incremental!")
                            transformFile(outputFile)
                        }
                    }
                }
            } else {
                output.deleteRecursively()
                input.file.copyRecursively(output)
                output.walk().filter { it.isClass() }
                    .forEach {file ->
                        executor.execute {
                            log("transform $file without incremental!")
                            transformFile(file)
                        }
                    }
            }
        }
        jarInputs?.forEach(invocation) { input, output ->
            if(invocation.isIncremental){
                when(input.status){
                    Status.REMOVED -> {
                        FileUtils.deleteIfExists(input.file)
                    }
                    in arrayOf(Status.CHANGED, Status.ADDED) -> {
                        log("transformJar ${input.file} with incremental!")
                        executor.execute(TransformJarTask(input.file,output))
                    }
                }
            } else {
                log("transformJar ${input.file} without incremental!")
                executor.execute(TransformJarTask(input.file,output))
            }
        }
        executor.waitForTasksWithQuickFail<Any>(true)
        onTransformCompleted()
    }

    protected open fun onTransformCompleted(){

    }

    inner class TransformJarTask(private val inputJar:File, private val outputJar:File) : Callable<Any?> {
        override fun call(): Any? {
            FileUtils.deleteIfExists(outputJar)
            transformJar(inputJar, outputJar)
            return null
        }
    }

    /**
     * ??????????????????output??????????????????????????????????????????return
     *
     * @param output ?????????class file
     */
    abstract fun transformFile(output: File)

    /**
     * ?????????inputJar???????????????????????????????????????outputJar
     * @param inputJar ?????????jar??????
     * @param outputJar ?????????jar??????
     */
    abstract fun transformJar(inputJar: File, outputJar: File)

    private fun log(msg: String) {
        if (DEBUG) {
            println(msg)
        }
    }

    private inline fun <T : QualifiedContent> List<T>.forEach(invocation: TransformInvocation, action: (input: T, output: File) -> Unit) {
        for (item in this) {
            if (item is JarInput) {
                action(item, item.getOutputLocation(invocation))
            } else if (item is DirectoryInput) {
                action(item, item.getOutputLocation(invocation))
            }
        }
    }
}

private inline fun QualifiedContent.getOutputLocation(invocation: TransformInvocation): File {
    val format = when (this) {
        is JarInput -> {
            Format.JAR
        }
        is DirectoryInput -> {
            Format.DIRECTORY
        }
        else -> {
            throw AssertionError("Unsupported qualifiedContent of ${this.javaClass}")
        }
    }
    return invocation.outputProvider.getContentLocation(name, contentTypes, scopes, format)
}