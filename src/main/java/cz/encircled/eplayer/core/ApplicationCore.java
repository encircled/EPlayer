package cz.encircled.eplayer.core;

import cz.encircled.eplayer.common.Constants;
import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.service.*;
import cz.encircled.eplayer.service.event.EventObserver;
import cz.encircled.eplayer.service.event.EventObserverImpl;
import cz.encircled.eplayer.service.gui.FxViewService;
import cz.encircled.eplayer.service.gui.ViewService;
import cz.encircled.eplayer.util.IOUtil;
import cz.encircled.eplayer.view.fx.FxView;
import javafx.application.Application;
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.caprica.vlcj.player.MediaPlayer;

public class ApplicationCore {

    public static final String APP_DOCUMENTS_ROOT = System.getenv("APPDATA") + "\\EPlayer\\";
    private static final Logger log = LogManager.getLogger();
    private MediaService mediaService;

    private JsonCacheService cacheService;

    private ViewService viewService;

    private EventObserver eventObserver;

    private SeriesFinder seriesFinder;

    private FolderScanService folderScanService;

    public ApplicationCore() {
        eventObserver = new EventObserverImpl();
        cacheService = new JsonCacheService();
        folderScanService = new OnDemandFolderScanner(this);
        IOUtil.createIfMissing(APP_DOCUMENTS_ROOT, true, false);
        addCloseHook();
    }

    public static void main(String[] args) {
        Application.launch(FxView.class);
    }

    public void init(MediaPlayer mediaPlayer, FxView fxView) {
        cacheService.init();
        seriesFinder = new SeriesFinder();
        mediaService = new VLCMediaService(this, mediaPlayer);
        viewService = new FxViewService(fxView);
    }

    public void openQuickNavi() {
        new Thread(() -> {
            mediaService.pause();
            viewService.switchToQuickNavi();
            mediaService.updateCurrentMediaInCache();
            mediaService.stop();
            cacheService.save();
        }).start();
    }

    public void playLast() {
        MediaType media = cacheService.getLastByWatchDate();
        if (media != null) {
            mediaService.play(media);
        }
    }

    public void exit() {
        Platform.exit();
        System.exit(Constants.ZERO);
    }

    public SeriesFinder getSeriesFinder() {
        return seriesFinder;
    }

    public MediaService getMediaService() {
        return mediaService;
    }

    public CacheService getCacheService() {
        return cacheService;
    }

    public ViewService getViewService() {
        return viewService;
    }

    public FolderScanService getFolderScanService() {
        return folderScanService;
    }

    public EventObserver getEventObserver() {
        return eventObserver;
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