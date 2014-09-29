package cz.encircled.eplayer.service.gui;

import cz.encircled.eplayer.view.fx.FxView;
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
    private FxView appView;

    @PostConstruct
    private void initialize() {

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
        runInFxThread(appView::showPlayer, countDownLatch);
    }

    @Override
    public void enableSubtitlesMenu(boolean isEnabled) {

    }

    @Override
    public void switchToQuickNavi() {
        runInFxThread(appView::showQuickNavi);
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
