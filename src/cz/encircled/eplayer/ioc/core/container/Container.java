package cz.encircled.eplayer.ioc.core.container;

import cz.encircled.eplayer.ioc.component.ComponentDefinition;
import cz.encircled.eplayer.ioc.core.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

import static cz.encircled.eplayer.ioc.component.DefinitionModifier.PROTOTYPE;
import static cz.encircled.eplayer.ioc.component.DefinitionModifier.SINGLETON;

/**
 * Created by Encircled on 11/09/2014.
 */
public class Container extends AbstractContext implements Context {

    private static final Logger log = LogManager.getLogger();

    public static final String ROOT_PACKAGE = "cz.encircled.eplayer";

    private ResourceInjector resourceInjector;

    private MethodInvoker methodInvoker;

    private InstanceCreator instanceCreator;

    public Container() {
        addComponent(this);
        resourceInjector = new ResourceInjector(this);
        methodInvoker = new MethodInvoker();
        instanceCreator = new InstanceCreator();
    }

    @Override
    public void initializeContext() throws Exception {
        Long globalStart = System.nanoTime();
        List<Class<?>> classes = new ClassLookUp().findComponentClasses(ROOT_PACKAGE);
        constructComponentDefinitions(classes);
        List<ComponentDefinition> singletons = components.values().stream().filter(definition -> definition.hasModifier(SINGLETON)).collect(Collectors.toList());

        Long start = System.nanoTime();
        singletons.parallelStream().forEach(resourceInjector::injectComponentResources);
        log.debug("Injection done in {} ms", (System.nanoTime() - start) / 1000000);

        start = System.nanoTime();
        methodInvoker.invokeAllAnnotatedMethods(singletons, PostConstruct.class);
        log.debug("PostConstruct methods invoked in {} ms", (System.nanoTime() - start) / 1000000);
        log.debug("Context initialized in {} ms", (System.nanoTime() - globalStart) / 1000000);
    }

    @Override
    protected Object createPrototypeInstance(ComponentDefinition definition) {
        log.debug("Create prototype component instance {}", definition);
        Object instance = instanceCreator.createInstance(definition);
        definition.instance = instance;
        resourceInjector.injectComponentResources(definition);
        methodInvoker.invokeComponentAnnotatedMethods(definition, PostConstruct.class);
        return instance;
    }

    public static void main(String[] args) {
        try {
            new Container().initializeContext();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void constructComponentDefinitions(List<Class<?>> componentClasses) {
        Long start = System.nanoTime();
        componentClasses.forEach(c -> {
            ComponentDefinition definition = definitionBuilder.build(c);
            if (scopeAware.isSingletonScope(definition))
                definition.instance = instanceCreator.createInstance(definition);
            components.put(getNameForComponent(c), definition);
        });
        log.debug("Components created in {} ms", (System.nanoTime() - start) / 1000000);

    }

}
