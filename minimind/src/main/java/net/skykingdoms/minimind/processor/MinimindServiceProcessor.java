package net.skykingdoms.minimind.processor;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import net.skykingdoms.minimind.MinimindService;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.Set;

@SupportedAnnotationTypes("net.skykingdoms.minimind.MinimindService")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class MinimindServiceProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(MinimindService.class)) {
            if (element instanceof TypeElement) {
                TypeElement typeElement = (TypeElement) element;
                MinimindService annotation = typeElement.getAnnotation(MinimindService.class);
                generateMainClass(typeElement, annotation.name());
            }
        }
        return true;
    }

    private void generateMainClass(TypeElement typeElement, String name) {
        String className = name + "Main";
        MethodSpec mainMethod = MethodSpec.methodBuilder("main")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(String[].class, "args")
                .addStatement("$T service = new $T()", typeElement, typeElement)
                .addStatement("service.start()")
                .build();

        TypeSpec mainClass = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(mainMethod)
                .build();

        JavaFile javaFile = JavaFile.builder(processingEnv.getElementUtils().getPackageOf(typeElement).getQualifiedName().toString(), mainClass)
                .build();

        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}