package cz.encircled.eplayer.ioc.core.container;

import cz.encircled.eplayer.ioc.component.ComponentDefinition;
import cz.encircled.eplayer.ioc.core.DefinitionBuilder;
import cz.encircled.eplayer.ioc.core.ScopeAware;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static cz.encircled.eplayer.ioc.component.DefinitionModifier.PROTOTYPE;

/**
 * Created by encircled on 9/19/14.
 */
public abstract class AbstractContainer implements Context {

    private static final Logger log = LogManager.getLogger();

    protected ScopeAware scopeAware;

    protected DefinitionBuilder definitionBuilder;

    protected final Map<String, ComponentDefinition> components = new HashMap<>();

    public AbstractContainer() {
        scopeAware = new ScopeAware();
        definitionBuilder = new DefinitionBuilder(scopeAware);
    }

    @Override
    public <T> T getComponent(Class<T> clazz) {
        return (T) getComponentInternal(getNameForComponent(clazz));
    }

    @Override
    public Object getComponent(String name) {
        return getComponentInternal(name);
    }

    /**
     * Add singleton <code>component</code> to context
     *
     * @param component - component instance
     */
    @Override
    public void addComponent(Object component) {
        ComponentDefinition definition = definitionBuilder.annotationToDefinition(component.getClass());
        definition.instance = component;
        components.put(getNameForComponent(component.getClass()), definition);
    }

    protected Object getComponentInternal(String name) {
        ComponentDefinition definition = components.get(name);
        if (definition == null)
            return null;
        return definition.hasModifier(PROTOTYPE) ? createPrototypeInstance(definition) : definition.instance;
    }


    // TODO
    protected String getNameForComponent(Class<?> clazz) {
        Resource resource = clazz.getAnnotation(Resource.class);
        String name;
        if (resource == null || resource.name().isEmpty()) {
            StringBuilder sb = new StringBuilder(clazz.getInterfaces().length > 0 ? clazz.getInterfaces()[0].getSimpleName() : clazz.getSimpleName());
            sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
            name = sb.toString();
        } else {
            name = resource.name();
        }
        return name;
    }

    protected abstract Object createPrototypeInstance(ComponentDefinition definition);

}
