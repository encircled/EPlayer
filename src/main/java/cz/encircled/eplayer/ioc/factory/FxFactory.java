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
        final Object[] instance = {null};
        CountDownLatch countDownLatch = new CountDownLatch(1);
        boolean isFxThread = Platform.isFxApplicationThread();
        log.debug("Fx create {}, is FX thread {}", clazz, isFxThread);
        if (isFxThread) {
            instance[0] = ReflectionUtil.instance(clazz);
        } else {
            Platform.runLater(() -> {
                log.debug("Platform run");
                try {
                    instance[0] = ReflectionUtil.instance(clazz);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                log.debug("{} created ", clazz);
                countDownLatch.countDown();
            });
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                log.error(e);
            }
        }
        return instance[0];
    }

}
