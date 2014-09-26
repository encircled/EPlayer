package cz.encircled.eplayer.ioc.core.container;

import org.jetbrains.annotations.Nullable;

/**
 * Created by encircled on 9/19/14.
 */
public interface Context {

    void initializeContext() throws Exception;

    <T> T getComponent(Class<T> clazz);

    @Nullable
    Object getComponent(String name);

    void addComponent(Object component);
}
