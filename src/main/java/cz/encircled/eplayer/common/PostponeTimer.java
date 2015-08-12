package cz.encircled.eplayer.common;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Encircled on 28/09/2014.
 */
public class PostponeTimer {

    private final Runnable runnable;

    private Timer timer;

    public PostponeTimer(Runnable runnable) {
        this.runnable = runnable;
    }

    public void postpone(long delay) {
        reset(delay);
    }

    public void cancel() {
        if (timer != null)
            timer.cancel();
    }

    private void reset(long delay) {
        cancel();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runnable.run();
            }
        }, delay);
    }

}
