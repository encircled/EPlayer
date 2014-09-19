package cz.encircled.eplayer.ioc.core;

import cz.encircled.eplayer.ioc.component.ComponentDefinition;
import cz.encircled.eplayer.ioc.component.ComponentFactory;
import cz.encircled.eplayer.util.ReflectionUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.FileFilter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

import static cz.encircled.eplayer.ioc.component.DefinitionModifier.FACTORY;

/**
 * Created by Encircled on 11/09/2014.
 */
public class Container {

    private static final Logger log = LogManager.getLogger();

    private static final Pattern CLASS_PATTERN = Pattern.compile("^[^\\$]+\\.class$");

    private static final FileFilter FILE_FILTER = pathname -> pathname.isDirectory() || CLASS_PATTERN.matcher(pathname.getName()).matches();
    public static final String DOT = "\\.";
    public static final String CLASS_SEPARATOR = "/";

    private final Map<String, ComponentDefinition> components = new HashMap<>();

    private static Container context;

    public static Container getContext() {
        return context;
    }

    public void addComponent(Object component) {
        ComponentDefinition definition = constructDefinition(component.getClass());
        definition.instance = component;
        components.put(getNameForComponent(component.getClass()), definition);
    }

    private void findComponentClasses() {
        List<Class<?>> componentClasses;
        try {
            componentClasses = getComponentClassesFromPackage("cz.encircled.eplayer");
        } catch (Exception e) {
            throw new RuntimeException("Component classes search failed", e);
        }

        Long start = System.nanoTime();
        componentClasses.forEach(c -> {
            ComponentDefinition definition = constructDefinition(c);
            definition.instance = createInstance(definition);
            components.put(getNameForComponent(c), definition);
        });
        log.debug("Components created in {} ms", (System.nanoTime() - start) / 1000000);
    }

    private ComponentDefinition constructDefinition(Class<?> clazz) {
        return new ComponentDefinition(clazz);
    }

    private List<Class<?>> getComponentClassesFromPackage(String pathToPkg) throws Exception {
        List<Class<?>> componentClasses = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources(pathToPkg.replaceAll(DOT, CLASS_SEPARATOR));

        while (resources.hasMoreElements()) {
            File dir = new File(resources.nextElement().getPath());
            for (File f : dir.listFiles(FILE_FILTER)) {
                if (f.isFile()) {
                    String name = f.getName();
                    Class<?> componentClass = Class.forName(pathToPkg + "." + name.subSequence(0, name.length() - 6));
                    if (componentClass.getAnnotation(Resource.class) != null) {
                        log.debug("New resource annotated class {}", componentClass.getName());
                        componentClasses.add(componentClass);
                    }
                } else {
                    componentClasses.addAll(getComponentClassesFromPackage(pathToPkg + "." + f.getName()));
                }
            }
        }
        return componentClasses;
    }

    public void initializeContext() throws Exception {
        Long start = System.nanoTime();
        Long globalStart = System.nanoTime();
        findComponentClasses();
        context = this;

        components.values().parallelStream().forEach(component -> {
            for (Field field : component.clazz.getDeclaredFields()) {
                for (Annotation annotation : field.getAnnotations()) {
                    if (annotation instanceof Resource) {
                        String name = field.getName();
                        if (!components.containsKey(name))
                            throw new RuntimeException("Component not found " + name + ", but was requested in " + component);
                        log.debug("Set to {}#{} component {}", component.clazz, name, components.get(name));
                        ReflectionUtil.setField(components.get(name).instance, field, component.instance);
                    }
                }
            }
        });
        log.debug("Injection done in {} ms", (System.nanoTime() - start) / 1000000);

        start = System.nanoTime();
        invokeAllAnnotatedMethods(PostConstruct.class);
        log.debug("Init methods done in {} ms", (System.nanoTime() - start) / 1000000);
        log.debug("Context initialized in {} ms", (System.nanoTime() - globalStart) / 1000000);
    }

    private String getNameForComponent(Class<?> clazz) {
        StringBuilder sb = new StringBuilder(clazz.getInterfaces().length > 0 ? clazz.getInterfaces()[0].getSimpleName() : clazz.getSimpleName());
        sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
        return sb.toString();
    }

    private void invokeAllAnnotatedMethods(Class<?> annotationClass) {
        components.values().parallelStream().forEach(component -> Arrays.stream(component.clazz.getDeclaredMethods()).parallel().forEach(method -> {
            for (Annotation annotation : method.getAnnotations()) {
                if (annotationClass.isInstance(annotation))
                    ReflectionUtil.invokeMethod(method, component.instance);
            }
        }));
    }

    private Object createInstance(ComponentDefinition def) {
        if (def.hasModifier(FACTORY)) {
            return ((ComponentFactory) ReflectionUtil.instance((Class<?>) def.getModifierParams(FACTORY))).getInstance(def.clazz);
        } else {
            return ReflectionUtil.instance(def.clazz);
        }
    }

}
