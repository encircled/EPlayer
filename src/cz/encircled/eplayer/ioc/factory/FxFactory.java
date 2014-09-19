package cz.encircled.eplayer.ioc.factory;

import cz.encircled.eplayer.ioc.component.ComponentFactory;
import cz.encircled.eplayer.util.ReflectionUtil;
import javafx.application.Application;

/**
 * Created by Encircled on 16/09/2014.
 */
public class FxFactory implements ComponentFactory<Application> {

    @Override
    public Application getInstance(Class<? extends Application> clazz) {
        Application app = ReflectionUtil.instance(clazz);
        new Thread(() -> Application.launch(clazz));

        return app;
    }
}
