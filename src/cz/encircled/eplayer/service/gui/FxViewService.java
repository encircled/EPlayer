package cz.encircled.eplayer.service.gui;

import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.service.CacheService;
import cz.encircled.eplayer.view.AppView;
import javafx.application.Platform;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Resource;
import java.awt.*;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Encircled on 16/09/2014.
 */
@Resource
public class FxViewService implements ViewService {

    @Resource
    private AppView appView;

    @Resource
    private CacheService cacheService;

    @Override
    public boolean isPlayerState() {
        return false;
    }

    @Override
    public void enterFullScreen() {

    }

    @Override
    public void exitFullScreen() {

    }

    @Override
    public void showPlayer(@NotNull CountDownLatch countDownLatch) {
        runInFxThread(appView::showPlayer, countDownLatch);
    }


    @Override
    public void enableSubtitlesMenu(boolean isEnabled) {

    }

    @Override
    public void showQuickNavi() {
        Collection<MediaType> mediaTypes = cacheService.getCache();
        runInFxThread(() -> appView.showQuickNavi(mediaTypes));
    }

    @Override
    public Window getWindow() {
        return null;
    }

    @Override
    public void initMediaFiltering() {

    }

    @Override
    public void stopMediaFiltering() {

    }

    @Override
    public void openMedia() {

    }

    private void runInFxThread(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            Platform.runLater(runnable);
        }
    }

    private void runInFxThread(Runnable runnable, CountDownLatch countDownLatch) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
            countDownLatch.countDown();
        } else {
            Platform.runLater(() -> {
                runnable.run();
                countDownLatch.countDown();
            });
        }
    }

}
