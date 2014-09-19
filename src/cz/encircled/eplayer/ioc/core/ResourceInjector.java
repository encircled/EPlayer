package cz.encircled.eplayer.ioc.core;

import cz.encircled.eplayer.ioc.component.ComponentDefinition;
import cz.encircled.eplayer.ioc.core.container.Context;
import cz.encircled.eplayer.util.ReflectionUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Resource;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * Created by encircled on 9/19/14.
 */
public class ResourceInjector {

    private static final Logger log = LogManager.getLogger();

    private Context context;

    public ResourceInjector(Context context) {
        this.context = context;
    }

    public void injectComponentResources(ComponentDefinition definition) {
        for (Field field : definition.clazz.getDeclaredFields()) {
            for (Annotation annotation : field.getAnnotations()) {
                if (annotation instanceof Resource) {
                    String name = field.getName();
                    Object resource = context.getComponent(name);
                    if (definition == null)
                        throw new RuntimeException("Component not found " + name + ", but was requested in " + definition);
                    log.debug("Set to {}#{} definition {}", definition.clazz, name, resource);
                    ReflectionUtil.setField(resource, field, definition.instance);
                }
            }
        }
    }

}
