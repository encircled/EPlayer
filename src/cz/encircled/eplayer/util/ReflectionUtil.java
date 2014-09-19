package cz.encircled.eplayer.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Encircled on 16/09/2014.
 */
public class ReflectionUtil {

    public static <T> T instance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void invokeMethod(Method method, Object instance) {
        try {
            method.setAccessible(true);
            method.invoke(instance);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setField(Object instance, Field field, Object value) {
        try {
            field.setAccessible(true);
            field.set(value, instance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
