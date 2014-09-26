package cz.encircled.eplayer.ioc.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by encircled on 9/19/14.
 */
public class ClassLookUp {

    private static final Logger log = LogManager.getLogger();

    private static final Pattern CLASS_PATTERN = Pattern.compile("^[^\\$]+\\.class$");

    private static final FileFilter FILE_FILTER = pathname -> pathname.isDirectory() || CLASS_PATTERN.matcher(pathname.getName()).matches();

    public static final String DOT = "\\.";

    public static final String CLASS_SEPARATOR = "/";

    /**
     * Look-up for components in all packages starting from <code>rootPackage</code>
     */
    public List<Class<?>> findComponentClasses(String rootPackage) {
        List<Class<?>> componentClasses;
        try {
            componentClasses = getComponentClassesFromPackage(rootPackage);
        } catch (Exception e) {
            throw new RuntimeException("Component classes search failed", e);
        }
        return componentClasses;
    }

    /**
     * Find all resource-annotated classes non-abstract in <code>pathToPkg</code>
     */
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
                    if (componentClass.getAnnotation(Resource.class) != null && !Modifier.isAbstract(componentClass.getModifiers())) {
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

}
