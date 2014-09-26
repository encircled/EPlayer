package cz.encircled.eplayer.service.gui;

import cz.encircled.eplayer.service.CacheService;
import cz.encircled.eplayer.service.event.EventObserver;
import cz.encircled.eplayer.view.fx.FxTest;
import javafx.application.Platform;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Encircled on 16/09/2014.
 */
@Resource
public class FxViewService implements ViewService {

    @Resource
    private FxTest fxTest;

    @Resource
    private CacheService cacheService;

    @Resource
    private EventObserver eventObserver;

    @PostConstruct
    private void initialize() {
//        eventObserver.listen(Event.ContextInitialized, (event, args) -> {
//            Collection<MediaType> mediaTypes = cacheService.getCache();
//            runInFxThread(() -> fxTest.switchToQuickNavi(mediaTypes));
//        });
    }

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
    public void showPlayer(CountDownLatch countDownLatch) {
        runInFxThread(fxTest::showPlayerScene, countDownLatch);
    }

    @Override
    public void enableSubtitlesMenu(boolean isEnabled) {

    }

    @Override
    public void switchToQuickNavi() {
        runInFxThread(fxTest::showPlayerScene);
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
