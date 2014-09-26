package cz.encircled.eplayer.ioc.component.annotation;

import cz.encircled.eplayer.ioc.runner.ComponentRunner;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Encircled on 20/09/2014.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Runner {

    Class<? extends ComponentRunner> value();

}
