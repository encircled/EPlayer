package cz.encircled.eplayer.service.gui;

import cz.encircled.eplayer.view.fx.FxView;
import javafx.application.Platform;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CountDownLatch;

/**
 * @author Encircled on 16/09/2014.
 */
public class FxViewService implements ViewService {

    private FxView fxView;

    public FxViewService(FxView fxView) {
        this.fxView = fxView;
    }

    @Override
    public void showPlayer(@NotNull CountDownLatch countDownLatch) {
        runInFxThread(fxView::showPlayer, countDownLatch);
    }

    @Override
    public void enableSubtitlesMenu(boolean isEnabled) {

    }

    @Override
    public void switchToQuickNavi() {
        runInFxThread(fxView::showQuickNavi);
    }

    private void runInFxThread(@NotNull Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            Platform.runLater(runnable);
        }
    }

    private void runInFxThread(@NotNull Runnable runnable, @NotNull CountDownLatch countDownLatch) {
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
