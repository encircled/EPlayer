package cz.encircled.eplayer.core;

import cz.encircled.elight.core.annotation.Component;
import cz.encircled.elight.core.annotation.Wired;
import cz.encircled.eplayer.service.CacheService;
import cz.encircled.eplayer.service.MediaService;
import cz.encircled.eplayer.util.IOUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// TODO Youtube tab and filtering
@Component
public class Application {

    private static final Logger log = LogManager.getLogger();

    public static final String APP_DOCUMENTS_ROOT = System.getenv("APPDATA") + "\\EPlayer\\";

    @Wired
    private CacheService cacheService;

    @Wired
    private MediaService mediaService;

    public Application() {
        IOUtil.createIfMissing(APP_DOCUMENTS_ROOT, true, false);
        addCloseHook();
    }

    private void addCloseHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                log.debug("Close hook start");
                mediaService.updateCurrentMediaInCache();
                cacheService.save();
                mediaService.releasePlayer();
                log.debug("Close hook finished");
            }
        });
        log.trace("Close hook added");
    }

}
