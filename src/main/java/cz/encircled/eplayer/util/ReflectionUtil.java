package cz.encircled.eplayer.util;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Encircled on 16/09/2014.
 */
public class ReflectionUtil {

    public static <T> T instance(@NotNull Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (@NotNull InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object invokeMethod(@NotNull Method method, Object instance) {
        try {
            method.setAccessible(true);
            return method.invoke(instance);
        } catch (@NotNull IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setField(Object instance, @NotNull Field field, Object value) {
        try {
            field.setAccessible(true);
            field.set(value, instance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
