package io.ydxlt.app.applifecycle.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import io.ydxlt.app.lifecycle.api.AppLifecycle;
import io.ydxlt.app.lifecycle.api.AppLifecycleCallback;

/**
 * 生成对应的AppLifecycle实现类的wrapper类
 */
public class AppLifecycleCreator {

    public static final ClassName APP_LIFECYCLE_CALLBACK = ClassName.get(AppLifecycleCallback.class);
    public static final String WRAPPER_SUFFIX = "_AppLifeCycleWrapper";

    private TypeElement typeElement;
    private AppLifecycle appLifecycle;
    private String packageName;
    private String wrapperClassName;

    public AppLifecycleCreator(TypeElement typeElement, Elements elements) {
        this.typeElement = typeElement;
        this.appLifecycle = typeElement.getAnnotation(AppLifecycle.class);
        this.packageName = elements.getPackageOf(typeElement).getQualifiedName().toString();
        this.wrapperClassName = typeElement.getSimpleName() + WRAPPER_SUFFIX;
    }

    public JavaFile create() {
        return JavaFile.builder(packageName, TypeSpec.classBuilder(wrapperClassName)
                .addSuperinterface(APP_LIFECYCLE_CALLBACK)
                .addModifiers(Modifier.PUBLIC,Modifier.FINAL)
                .addField(FieldSpec.builder(APP_LIFECYCLE_CALLBACK,"mCallback",Modifier.PRIVATE)
                        .initializer("new $T()",typeElement.asType())
                        .build())
                .addMethod(lifecycleMethod("onCreate"))
                .addMethod(lifecycleMethod("onTerminate"))
                .addMethod(lifecycleMethod("onLowMemory"))
                .addMethod(lifecycleMethod("onTrimMemory"))
                .addMethod(priorityMethod())
                .build())
                .build();
    }

    private MethodSpec priorityMethod() {
        CodeBlock statement = CodeBlock.builder()
                .addStatement("int priority = mCallback.getPriority()")
                .beginControlFlow("if(priority != 0)")
                .addStatement("return priority")
                .endControlFlow()
                .addStatement("return $L", appLifecycle.priority())
                .build();
        return MethodSpec.methodBuilder("getPriority")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.INT)
                .addCode(statement)
                .build();
    }

    private MethodSpec lifecycleMethod(String name) {
        return MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addStatement("mCallback.$L()",name)
                .returns(TypeName.VOID)
                .build();
    }
}
