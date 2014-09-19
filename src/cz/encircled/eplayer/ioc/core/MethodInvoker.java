package cz.encircled.eplayer.ioc.core;

import cz.encircled.eplayer.ioc.component.ComponentDefinition;
import cz.encircled.eplayer.util.ReflectionUtil;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

/**
 * Created by encircled on 9/19/14.
 */
public class MethodInvoker {

    /**
     * Parallel call {@link #invokeComponentAnnotatedMethods(cz.encircled.eplayer.ioc.component.ComponentDefinition, Class)} for all <code>definitions</code>
     */
    public void invokeAllAnnotatedMethods(List<ComponentDefinition> definitions, Class<?> annotationClass) {
        definitions.parallelStream().forEach(component -> invokeComponentAnnotatedMethods(component, annotationClass));
    }

    /**
     * Invoke all methods, that have annotation <code>annotationClass</code> on instance from <code>definition</code>
     */
    public void invokeComponentAnnotatedMethods(ComponentDefinition definition, Class<?> annotationClass) {
        Arrays.stream(definition.clazz.getDeclaredMethods()).parallel().forEach(method -> {
            for (Annotation annotation : method.getAnnotations()) {
                if (annotationClass.isInstance(annotation))
                    ReflectionUtil.invokeMethod(method, definition.instance);
            }
        });
    }

}
