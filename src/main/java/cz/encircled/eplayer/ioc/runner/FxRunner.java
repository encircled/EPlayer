package cz.encircled.eplayer.ioc.runner;

import javafx.application.Platform;

import javax.annotation.Resource;

/**
 * Created by Encircled on 20/09/2014.
 */
@Resource
public class FxRunner implements ComponentRunner {

    @Override
    public void run(Runnable runnable) {
        Platform.runLater(runnable);
    }

}
