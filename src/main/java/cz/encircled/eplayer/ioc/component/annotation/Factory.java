package cz.encircled.eplayer.ioc.component.annotation;

import cz.encircled.eplayer.ioc.factory.ComponentFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Encircled on 19/09/2014.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Factory {

    Class<? extends ComponentFactory> value();

}
