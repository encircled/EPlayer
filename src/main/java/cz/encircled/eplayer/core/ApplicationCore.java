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
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.player.MediaPlayer;

import java.io.File;

public class ApplicationCore {

    public static final String APP_DOCUMENTS_ROOT = System.getenv("APPDATA") + "\\EPlayer\\";

    public static final String SCREENS_FOLDER = APP_DOCUMENTS_ROOT + "\\frames\\";

    public static final String URL_FILE_PREFIX = "file:\\\\\\";

    private static final Logger log = LogManager.getLogger();

    private MediaService mediaService;

    private CacheService cacheService;

    private ViewService viewService;

    private EventObserver eventObserver;

    private SeriesFinder seriesFinder;

    private FolderScanService folderScanService;

    private FxView fxView;

    public ApplicationCore() {
        cacheService = new JsonCacheService();
        folderScanService = new OnDemandFolderScanner(this);
        eventObserver = new EventObserverImpl();
        IOUtil.createIfMissing(APP_DOCUMENTS_ROOT, true, false);
        IOUtil.createIfMissing(SCREENS_FOLDER, true, false);
        addCloseHook();
    }

    @NotNull
    public static String getScreenshotLocation(@NotNull MediaType mediaType) {
        return SCREENS_FOLDER + mediaType.getName() + mediaType.getSize() + ".png";
    }

    @NotNull
    public static String getScreenshotURL(@NotNull MediaType mediaType) {
        String fileLocation = getScreenshotLocation(mediaType);
        return new File(fileLocation).exists() ? URL_FILE_PREFIX + fileLocation : "";
    }

    public void initFx(MediaPlayer mediaPlayer) {
        mediaService = new VLCMediaService(this, mediaPlayer);
    }

    public void init(FxView fxView) {
        this.fxView = fxView;
        cacheService.init();
        seriesFinder = new SeriesFinder();
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

    public void back() {
        // TODO
        if (fxView.isPlayerScene()) {
            if (fxView.isFullScreen()) {
//                fxView.setFullScreen(false);
                new Thread(mediaService::updateCurrentMediaInCache).start();
            } else
                openQuickNavi();
        }
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
