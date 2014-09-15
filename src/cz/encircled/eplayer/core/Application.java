package cz.encircled.eplayer.core;

import cz.encircled.eplayer.ioc.Container;
import cz.encircled.eplayer.service.CacheService;
import cz.encircled.eplayer.service.MediaService;
import cz.encircled.eplayer.service.gui.ViewService;
import cz.encircled.eplayer.util.IOUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

// TODO Youtube tab and filtering

public class Application {

    private static final Logger log = LogManager.getLogger();

    public static final String APP_DOCUMENTS_ROOT = System.getenv("APPDATA") + "\\EPlayer\\";

    @Resource
    private CacheService cacheService;

    @Resource
    private MediaService mediaService;

    @Resource
    private ViewService viewService;

    public Application() {
        log.trace("App init");
        IOUtil.createIfMissing(APP_DOCUMENTS_ROOT, true, false);
    }

    @PostConstruct
    private void start() {
        viewService.showQuickNavi();
        addCloseHook();
    }

    private void addCloseHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                mediaService.updateCurrentMediaInCache();
                mediaService.releasePlayer();
                cacheService.save();
            }
        });
        log.trace("Close hook added");
    }

    public static void main(final String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        try {
            new Container().init();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
