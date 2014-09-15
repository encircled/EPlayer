package cz.encircled.eplayer.ioc;

import cz.encircled.eplayer.core.Application;
import cz.encircled.eplayer.core.FileVisitorScanService;
import cz.encircled.eplayer.service.JsonCacheService;
import cz.encircled.eplayer.service.VLCMediaService;
import cz.encircled.eplayer.service.action.ReflectionActionExecutor;
import cz.encircled.eplayer.service.event.EventObserverImpl;
import cz.encircled.eplayer.util.GuiUtil;
import cz.encircled.eplayer.util.Localizations;
import cz.encircled.eplayer.util.Settings;
import cz.encircled.eplayer.view.dnd.DndHandler;
import cz.encircled.eplayer.view.swing.Components;
import cz.encircled.eplayer.view.swing.Frame;
import cz.encircled.eplayer.view.swing.SwingFromGuiViewService;
import cz.encircled.eplayer.view.swing.SwingViewService;
import cz.encircled.eplayer.view.swing.componensts.PlayerControls;
import cz.encircled.eplayer.view.swing.listeners.KeyDispatcher;
import cz.encircled.eplayer.view.swing.menu.MenuBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Encircled on 11/09/2014.
 */
public class Container {

    private static final Logger log = LogManager.getLogger();

    private static final List<Class<?>> componentsDefinition = Arrays.asList(
            EventObserverImpl.class,
            SwingFromGuiViewService.class,
            GuiUtil.class,
            Components.class,
            KeyDispatcher.class,
            Settings.class,
            Localizations.class,
            JsonCacheService.class,
            DndHandler.class,
            ReflectionActionExecutor.class,
            SwingViewService.class,
            FileVisitorScanService.class,
            MenuBuilder.class,
            Frame.class,
            PlayerControls.class,
            VLCMediaService.class,
            Application.class
    );

    private static final Map<String, Object> components = new HashMap<>(componentsDefinition.size());

    public void init() throws Exception {
        Long start = System.nanoTime();
        componentsDefinition.parallelStream().forEach(clazz -> {
                    String name = getNameForComponent(clazz);
                    log.debug("New component " + name + " for class " + clazz.getName());
                    components.put(name, instance(clazz));
                }
        );

        log.debug("Components created in {} ms", (System.nanoTime() - start) / 1000000);

        start = System.nanoTime();
        components.values().parallelStream().forEach(component -> {
            for (Field field : component.getClass().getDeclaredFields()) {
                for (Annotation annotation : field.getAnnotations()) {
                    if (annotation instanceof Resource) {
                        String name = field.getName();
                        if (components.get(name) == null)
                            log.error("Component not found " + name);
                        log.debug("Set to {}#{} component {}", component.getClass().getSimpleName(), name, components.get(name));
                        setField(components.get(name), field, component);
                    }
                }
            }
        });
        log.debug("Injection done in {} ms", (System.nanoTime() - start) / 1000000);

        start = System.nanoTime();
        invokeAllWithAnnotation(PostConstruct.class);
        log.debug("Init methods done in {} ms", (System.nanoTime() - start) / 1000000);
    }

    private String getNameForComponent(Class<?> clazz) {
        StringBuilder sb = new StringBuilder(clazz.getInterfaces().length > 0 ? clazz.getInterfaces()[0].getSimpleName() : clazz.getSimpleName());
        sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
        return sb.toString();
    }

    private void invokeAllWithAnnotation(Class<?> annotationClass) {
        components.values().parallelStream().forEach(component -> Arrays.stream(component.getClass().getDeclaredMethods()).parallel().forEach(method -> {
            for (Annotation annotation : method.getAnnotations()) {
                if (annotationClass.isInstance(annotation))
                    invokeMethod(method, component);
            }
        }));
    }

    private void invokeMethod(Method method, Object instance) {
        try {
            method.setAccessible(true);
            method.invoke(instance);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private void setField(Object instance, Field field, Object value) {
        try {
            field.setAccessible(true);
            field.set(value, instance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Object instance(Class<?> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
