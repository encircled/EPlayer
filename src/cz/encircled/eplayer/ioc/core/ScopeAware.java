package cz.encircled.eplayer.ioc.core;

import cz.encircled.eplayer.ioc.component.ComponentDefinition;
import cz.encircled.eplayer.ioc.component.Scope;

import static cz.encircled.eplayer.ioc.component.DefinitionModifier.SINGLETON;

/**
 * Created by encircled on 9/19/14.
 */
public class ScopeAware {

    public boolean isSingletonScope(ComponentDefinition definition) {
        return definition.hasModifier(SINGLETON);
    }

    public boolean isPrototypeScope(Class<?> clazz) {
        Scope scope = clazz.getAnnotation(Scope.class);
        return scope != null && scope.value().equals("prototype");
    }

}
