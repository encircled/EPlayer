package cz.encircled.eplayer.ioc.factory;

import cz.encircled.eplayer.util.ReflectionUtil;
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CountDownLatch;

/**
 * Created by Encircled on 16/09/2014.
 */
public class FxFactory implements ComponentFactory {

    private Logger log = LogManager.getLogger();

    @Override
    public Object getInstance(Class<?> clazz) {
        return Platform.isFxApplicationThread() ? ReflectionUtil.instance(clazz) : createInFxThread(clazz);
    }

    private Object createInFxThread(Class<?> clazz) {
        final Object[] instance = {null};
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                instance[0] = ReflectionUtil.instance(clazz);
            } catch (Exception e) {
                e.printStackTrace();
            }
            log.debug("{} created in FX factory and FX thread", clazz);
            countDownLatch.countDown();
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error(e);
        }
        return instance[0];
    }

}
