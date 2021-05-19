package io.ydxlt.app.applifecycle.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import io.ydxlt.app.lifecycle.api.AppLifecycle;

@AutoService(Processor.class)
public class AppLifecycleProcessor extends AbstractProcessor {

    private Filer mFiler;
    private Elements mElements;
    private Types mTypes;
    private Messager mMessager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mFiler = processingEnv.getFiler();
        mElements = processingEnv.getElementUtils();
        mTypes = processingEnv.getTypeUtils();
        mMessager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> appLifecycleElements = roundEnvironment.getElementsAnnotatedWith(AppLifecycle.class);
        if(appLifecycleElements != null && appLifecycleElements.size() > 0){
            processAppLifecycleElement(appLifecycleElements);
        }
        return false;
    }

    private void processAppLifecycleElement(Set<? extends Element> appLifecycleElements) {
        for(Element element : appLifecycleElements){
            if(element.getKind() != ElementKind.CLASS){
                throw new AppLifecycleException("The annotation element "+element+" of @AppLifecycle must be a class");
            }
            if(element.getModifiers().contains(Modifier.ABSTRACT)){
                throw new AppLifecycleException("The annotation element "+element+" of @AppLifecycle must not abstract");
            }
            TypeElement typeElement = (TypeElement) element;
            List<? extends TypeMirror> interfaces = typeElement.getInterfaces();
            if(interfaces.isEmpty()){
                throw new AppLifecycleException("The annotation element "+typeElement+" of @AppLifecycle must implement AppLifecycleCallback");
            }
            boolean implementAppLifecycleCallback = false;
            for(TypeMirror typeMirror : interfaces){
                TypeElement type = (TypeElement) mTypes.asElement(typeMirror);
                if(type.getQualifiedName().toString().equals(AppLifecycleCreator.APP_LIFECYCLE_CALLBACK.canonicalName())){
                    implementAppLifecycleCallback = true;
                    break;
                }
            }
            if(!implementAppLifecycleCallback){
                throw new AppLifecycleException("The annotation element "+typeElement+" of @AppLifecycle must implement AppLifecycleCallback");
            }
            JavaFile javaFile = new AppLifecycleCreator(typeElement,mElements).create();
            try {
                javaFile.writeTo(mFiler);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new HashSet<>();
        annotations.add(AppLifecycle.class.getCanonicalName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
