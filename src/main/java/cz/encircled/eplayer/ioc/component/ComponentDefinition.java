package cz.encircled.eplayer.ioc.component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Encircled on 16/09/2014.
 */
public class ComponentDefinition {

    public Class<?> clazz;

    public Set<Class<?>> superClasses;

    public Map<DefinitionModifier, Object> modifiers;

    public Object instance;

    public ComponentDefinition(Class<?> clazz) {
        this.clazz = clazz;
        modifiers = new HashMap<>();
        superClasses = new HashSet<>();
    }

    public boolean hasModifier(DefinitionModifier modifier) {
        return modifiers.containsKey(modifier);
    }

    public Object getModifierParams(DefinitionModifier modifier) {
        return modifiers.get(modifier);
    }

    @Override
    public String toString() {
        return "ComponentDefinition{" +
                "clazz=" + clazz +
                ", instance=" + instance +
                '}';
    }
}
