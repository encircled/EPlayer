package cz.encircled.eplayer.service.gui;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Administrator on 9.6.2014.
 */
public interface ViewService {

    boolean isPlayerState();

    void enterFullScreen();

    void exitFullScreen();

    void showPlayer(@NotNull CountDownLatch countDownLatch);

    // TODO
    void enableSubtitlesMenu(boolean isEnabled);

    void showQuickNavi();

    Window getWindow();

    void initMediaFiltering();

    void stopMediaFiltering();

    void openMedia();

}
