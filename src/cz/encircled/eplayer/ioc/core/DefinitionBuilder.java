package cz.encircled.eplayer.ioc.core;

import cz.encircled.eplayer.ioc.component.ComponentDefinition;
import cz.encircled.eplayer.ioc.component.Scope;

import static cz.encircled.eplayer.ioc.component.DefinitionModifier.PROTOTYPE;
import static cz.encircled.eplayer.ioc.component.DefinitionModifier.SINGLETON;

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
        if (scopeAware.isPrototypeScope(clazz))
            definition.modifiers.put(PROTOTYPE, null);
        else
            definition.modifiers.put(SINGLETON, null);
        return definition;
    }


}
