package cz.encircled.eplayer.ioc.component;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

/**
 * Created by Encircled on 16/09/2014.
 */
public class ComponentDefinition {

    public Class<?> clazz;

    public Map<DefinitionModifier, Object> modifiers;

    public Object instance;

    public ComponentDefinition(Class<?> clazz) {
        this.clazz = clazz;
        modifiers = Collections.emptyMap();
    }

    public ComponentDefinition(@NotNull Class<?> clazz, @NotNull Map<DefinitionModifier, Object> modifiers) {
        this.clazz = clazz;
        this.modifiers = modifiers;
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
