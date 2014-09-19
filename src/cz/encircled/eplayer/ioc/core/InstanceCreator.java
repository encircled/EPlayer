package cz.encircled.eplayer.ioc.core;

import cz.encircled.eplayer.ioc.component.ComponentDefinition;
import cz.encircled.eplayer.ioc.component.ComponentFactory;
import cz.encircled.eplayer.util.ReflectionUtil;

import static cz.encircled.eplayer.ioc.component.DefinitionModifier.FACTORY;

/**
 * Created by encircled on 9/19/14.
 */
public class InstanceCreator {

    public Object createInstance(ComponentDefinition definition) {
        return (definition.hasModifier(FACTORY)) ? createInstanceFromFactory(definition) : ReflectionUtil.instance(definition.clazz);
    }

    private Object createInstanceFromFactory(ComponentDefinition definition) {
        return ((ComponentFactory) ReflectionUtil.instance((Class<?>) definition.getModifierParams(FACTORY))).getInstance(definition.clazz);
    }

}
