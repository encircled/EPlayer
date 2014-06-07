package cz.encircled.eplayer.app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Encircled on 7/06/2014.
 */
public class ShutdownManager {

    private static final Logger log = LogManager.getLogger();

    private static final String SD_CMD_COMMAND = "shutdown";

    private static final String SD_CMD_TIME = " /t ";

    public static final String SD_CMD_HIBERNATE = " /h ";

    public static final String SD_CMD_SHUTDOWN = " /s";

    private static final String SD_CMD_CANCEL = "shutdown -a";

    private Timer hibernateTimer;

    /**
     * Call shutdown api
     */
    public void shutdown(final Long minutes, final String param) {
        new Thread(() -> {
            try {
                internalShutdown(minutes, param);
            } catch (IOException e) {
                log.error("Hibernate timer failed", e);
            }
        }).start();
    }

    private void internalShutdown(@Nullable Long time, String param) throws IOException {
        StringBuilder s = new StringBuilder(SD_CMD_COMMAND);
        if(time != null)
            s.append(SD_CMD_TIME).append(time * 60);
        s.append(param);
        log.debug("Shutdown {} in {} minutes. {}", param, time, s.toString());
        Runtime.getRuntime().exec(s.toString());
    }

    public void hibernate(final int time){
        new Thread(() -> {
            hibernateTimer = new java.util.Timer();
            hibernateTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    shutdown(null, SD_CMD_HIBERNATE);
                }
            }, time * 60000);
        }).start();
    }

    public void cancelShutdown(){
        try {
            Runtime.getRuntime().exec(SD_CMD_CANCEL);
            if(hibernateTimer != null)
                hibernateTimer.cancel();
        } catch (IOException e) {
            log.error("Cancel shutdown failed to execute", e);
        }
    }

}
