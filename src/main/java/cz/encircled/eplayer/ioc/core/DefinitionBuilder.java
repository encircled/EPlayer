package cz.encircled.eplayer.ioc.core;

import cz.encircled.eplayer.ioc.component.ComponentDefinition;
import cz.encircled.eplayer.ioc.component.annotation.Factory;
import cz.encircled.eplayer.ioc.component.annotation.Runner;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;

import static cz.encircled.eplayer.ioc.component.DefinitionModifier.*;

/**
 * Created by encircled on 9/19/14.
 */
public class DefinitionBuilder {

    private ScopeAware scopeAware;

    public DefinitionBuilder(ScopeAware scopeAware) {
        this.scopeAware = scopeAware;
    }

    public ComponentDefinition build(Class<?> clazz) {
        ComponentDefinition definition = new ComponentDefinition(clazz);
        definition.modifiers.put(scopeAware.isPrototypeScope(clazz) ? PROTOTYPE : SINGLETON, null);

        Factory factoryAnnotation = definition.clazz.getAnnotation(Factory.class);
        if (factoryAnnotation != null) {
            definition.modifiers.put(FACTORY, factoryAnnotation.value());
        }

        Runner runnerAnnotation = definition.clazz.getAnnotation(Runner.class);
        if (runnerAnnotation != null) {
            definition.modifiers.put(RUNNER, runnerAnnotation.value());
        }

        definition.superClasses = findSuperClasses(definition.clazz);

        return definition;
    }

    private Set<Class<?>> findSuperClasses(Class<?> clazz) {
        Set<Class<?>> result = new HashSet<>();
        Class<?> s = clazz.getSuperclass();
        if (s.getAnnotation(Resource.class) != null) {
            result.add(s);
            result.addAll(findSuperClasses(s));
        }
        return result;
    }

}
