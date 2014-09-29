package cz.encircled.eplayer.ioc.runner;

import javafx.application.Platform;

import java.util.concurrent.CountDownLatch;

/**
 * Created by Encircled on 20/09/2014.
 */
public class FxRunner implements ComponentRunner {

    @Override
    public void run(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    runnable.run();
                    countDownLatch.countDown();
                }
            });
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
